import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Solves challenge by 123Loadboard. Made for Code.Jam 2022.
 * Generates an optimal truck route based on available loads and profit, factoring deadhead and fuel.
 * @author Andrew Kan
 */
public class Optimizer {
    private static final double speed = 55.0; // MPH
    private static final double fuelCost = 0.40; // $/mile
    static JSONArray loads;

    /**
     * Geodesic distance formula between two world coordinates (Haversine formula). Adapted from Chris Veness.
     * @param lat1 starting latitude
     * @param lon1 starting longitude
     * @param lat2 ending latitude
     * @param lon2 ending longitude
     * @return distance in miles (double)
     */
    public static double calc_distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // metres
        double p1 = lat1 * Math.PI/180; // p, l in radians
        double p2 = lat2 * Math.PI/180;
        double dP = (lat2-lat1) * Math.PI/180;
        double dL = (lon2-lon1) * Math.PI/180;
        double a = Math.sin(dP/2) * Math.sin(dP/2) +
                Math.cos(p1) * Math.cos(p2) * Math.sin(dL/2) * Math.sin(dL/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // in metres
        return d / 1609.34; // in miles
    }

    /**
     * Returns difference in start and end times in hours
     * @param start_date String in yyyy-MM-dd HH:mm:ss format
     * @param end_date String in yyyy-MM-dd HH:mm:ss format
     * @return time difference in hours (double)
     */
    public static double findTimeDiff(String start_date, String end_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);
            long difference_In_Time = d2.getTime() - d1.getTime();  // in milliseconds
            return (difference_In_Time / (1000.0*60*60));    // difference in hours
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Adds an amount of hours to time-date string. Outputs to formatted string.
     * @param start time-date String in yyyy-MM-dd HH:mm:ss format
     * @param hours number of hours to add
     * @return String in yyyy-MM-dd HH:mm:ss format
     * @throws ParseException
     */
    public static String addTime(String start, double hours) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse(start);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.HOUR, (int) hours);
        cal.add(Calendar.MINUTE, (int) Math.ceil((hours-(int)hours) * 60));
        return sdf.format(cal.getTime());   // formatted new time
    }

    /**
     * Returns possible loads at an origin given time constraints.
     * @param startLat starting latitude
     * @param startLon starting longitude
     * @param startTime formatted string representing starting time at origin
     * @param maxTime formatted string for maximum destination time
     * @return ArrayList of possible loads
     */
    public static ArrayList<Load> findLoads(double startLat, double startLon, String startTime, String maxTime) throws ParseException {
        // Parse data file of available loads
        ArrayList<Load> possibleLoads = new ArrayList<>();
        for (Object o : loads) {        // for each available load, get its data
            JSONObject trip = (JSONObject) o;
            long id = (long) trip.get("load_id");
            double orgLat = (double) trip.get("origin_latitude");
            double orgLon = (double) trip.get("origin_longitude");
            double destLat = (double) trip.get("destination_latitude");
            double destLon = (double) trip.get("destination_longitude");
            String pickupTime = (String) trip.get("pickup_date_time");
            pickupTime = pickupTime.substring(0, 10) + " " + pickupTime.substring(11, 19);    // fixes UTC time format in JSON
            long amount = (long) trip.get("amount");    // money gained

            // Consider only loads in nearby area (checks within 2deg lat/lon)
            if (Math.abs(orgLat - startLat) < 2 && Math.abs(orgLon - startLon) < 2) {
                // Consider only profitable loads factoring deadhead
                double deadDist = calc_distance(startLat, startLon, orgLat, orgLon);    // deadhead from start to load origin
                double loadDist = calc_distance(orgLat, orgLon, destLat, destLon);      // distance while carrying load
                double profit = amount - loadDist * fuelCost - deadDist * fuelCost;     // factors gain and costs from fuel and deadhead
                if (profit > 0) {   // profitable loads
                    // Check if truck can make pickup time and if time not too far away
                    double timeDiffPickup = findTimeDiff(startTime, pickupTime);
                    double timeDiffEnd = findTimeDiff(pickupTime, maxTime);         // used to check pickupTime before max destination time
                    if (timeDiffPickup >= deadDist/speed && timeDiffEnd >= loadDist/speed && timeDiffPickup <= 6) {
                        String endTime = addTime(pickupTime, loadDist/speed);     // time when reaches destination
                        double weight = profit / findTimeDiff(startTime,endTime);       // weighting = profit/timeSpent
                        Load l = new Load(o, id, profit, weight, orgLat, orgLon, destLat, destLon, pickupTime, endTime);
                        possibleLoads.add(l);   // add to list of possible loads
                    }
                }
            }
        }
        return possibleLoads;
    }

    /**
     * Generates optimal routing from an origin with time constraints. Uses maximum weight at each node using Greedy Algorithm and recursion.
     * @param routing Linkedlist holding routing
     * @param startLat origin latitude
     * @param startLon origin longitude
     * @param startTime formatted string for start time at origin
     * @param maxTime formatted string for maximum destination time
     * @throws ParseException
     */
    public static void getOptimalRoute(LinkedList<Load> routing, double startLat, double startLon, String startTime, String maxTime) throws ParseException {
        // Finds possible loads available at starting location
        ArrayList<Load> loads = findLoads(startLat, startLon, startTime, maxTime);
        // Select max profit weight available and add to routing
        if (loads.size() > 0) {
            Load maxProf = loads.get(0);
            for (Load l : loads) {
                if (l.getWeight() > maxProf.getWeight()) {        // want the best weights at each node
                //if (l.getProfit() > maxProf.getProfit()) {       // if want the best profits at each node
                    boolean contains = false;   // check if load already on routing
                    for (Load r : routing) {
                        if (r.equals(l)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        maxProf = l;
                    }
                }
            }
            routing.add(maxProf);   // add max profit load to routing
            getOptimalRoute(routing, maxProf.getDestLat(), maxProf.getDestLon(), maxProf.getEndTime(), maxTime);    // keep adding loads to routing if possible
        }
    }

    /**
     * Gets optimal routing for driver and outputs routing with loadIDs.
     * @param driver JSONObject with driver info
     * @return JSONObject with tripID and routing with loadIds
     * @throws ParseException
     */
    public static JSONObject createRouting(JSONObject driver) throws ParseException {
        // Get driver info
        long id = (long) driver.get("input_trip_id");
        double startLat = (double) driver.get("start_latitude");
        double startLon = (double) driver.get("start_longitude");
        String startTime = (String) driver.get("start_time");
        String maxTime = (String) driver.get("max_destination_time");

        // Create routing in linked list
        LinkedList<Load> routing = new LinkedList<>();
        getOptimalRoute(routing, startLat, startLon, startTime, maxTime);

        // Add routing to output JSONObject
        JSONObject trip = new JSONObject();
        trip.put("input_trip_id", id);
        ArrayList<Long> loadIDs = new ArrayList<>();
        if (routing.size() >= 1) {
            //double totalProfit = 0;
            for (Load load : routing) {
                loadIDs.add(load.getID());      // add load IDs in routing to list
                //totalProfit += load.getProfit();
            }
            //System.out.println("$" + totalProfit);
        }
        trip.put("load_ids", loadIDs);
        //System.out.println(loadIDs);
        return trip;    // output JSONObject with trip id and loads
    }

    /**
     * Runner method. Handles parsing and writing of JSON files. Creates routing for each driver's trip.
     */
    public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray in = (JSONArray) parser.parse(new FileReader("123Loadboard_CodeJam_2022_input_final_s400.json"));
        loads = (JSONArray) parser.parse(new FileReader("123Loadboard_CodeJam_2022_dataset.json"));
        JSONArray tripList = new JSONArray();

        // For each input driver, create routing
        for (Object o : in) {
            JSONObject driver = (JSONObject) o;
            JSONObject trip = createRouting(driver);
            tripList.add(trip);
        }

        // Write JSON file
        try (FileWriter file = new FileWriter("output_s400.json")) {
            file.write(tripList.toJSONString());
            file.flush();
            System.out.println("Trip optimization successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
