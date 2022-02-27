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

public class Optimizer {
    static double speed = 55.0; // MPH
    static double fuelCost = 0.40; // $/mile
    static JSONArray loads;

    /**
     * Geodesic distance formula (Haversine formula). Adapted from Chris Veness.
     */
    public static double calc_distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // metres
        double φ1 = lat1 * Math.PI/180; // φ, λ in radians
        double φ2 = lat2 * Math.PI/180;
        double Δφ = (lat2-lat1) * Math.PI/180;
        double Δλ = (lon2-lon1) * Math.PI/180;
        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // in metres
        return d / 1609.34; // in miles
    }

    public static double findTimeDiff(String start_date, String end_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);
            long difference_In_Time = d2.getTime() - d1.getTime();
            double diff = (difference_In_Time / (1000.0*60*60));
            return diff;    // difference in hours
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String addTime(String start, double hours) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse(start);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.HOUR, (int) hours);
        cal.add(Calendar.MINUTE, (int) hours%1 * 60);
        cal.add(Calendar.SECOND, (int) hours%1%60 * 60);
        String newTime = sdf.format(cal.getTime());
        return newTime;
    }

    public static ArrayList<Load> findLoads(double startLat, double startLon, String startTime, String maxTime) throws ParseException {
        // Parse load data file
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
            long amount = (long) trip.get("amount");

            // Consider only loads in nearby area
            if (Math.abs(orgLat - startLat) < 2 && Math.abs(orgLon - startLon) < 2) {
                // Consider only profitable loads
                double deadDist = calc_distance(startLat, startLon, orgLat, orgLon);
                double loadDist = calc_distance(orgLat, orgLon, destLat, destLon);
                double profit = amount - loadDist * fuelCost - deadDist * fuelCost;
                if (profit > 0) {
                    // Check if truck can make pickup time and if time not too far away
                    double timeDiffPickup = findTimeDiff(startTime, pickupTime);
                    double timeDiffEnd = findTimeDiff(pickupTime, maxTime);
                    if (timeDiffPickup >= deadDist/speed && timeDiffEnd >= loadDist/speed) {// && timeDiffPickup <= 12) {
                        String endTime = addTime(startTime, loadDist/speed);
                        Load l = new Load(o, id, profit, orgLat, orgLon, destLat, destLon, pickupTime, endTime);
                        possibleLoads.add(l);   // add to list of possible loads
                    }
                }
            }
        }
        return possibleLoads;
    }

    public static void getOptimalRoute(LinkedList<Load> routing, double startLat, double startLon, String startTime, String maxTime) throws IOException, org.json.simple.parser.ParseException, ParseException {
        //LinkedList<Load> routing = null;
        ArrayList<Load> loads = findLoads(startLat, startLon, startTime, maxTime);
        if (loads.size() > 0) {
            Load maxProf = loads.get(0);
            for (Load l : loads) {
                if (l.getProfit() > maxProf.getProfit()) {
                    maxProf = l;
                }
            }
            routing.add(maxProf);
            getOptimalRoute(routing, maxProf.getDestLat(), maxProf.getDestLon(), maxProf.getEndTime(), maxTime);
        }
        //return routing;
    }

    public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray in = (JSONArray) parser.parse(new FileReader("123Loadboard_CodeJam_2022_input_sample.json"));
        loads = (JSONArray) parser.parse(new FileReader("123Loadboard_CodeJam_2022_dataset.json"));
        JSONArray tripList = new JSONArray();
        for (Object o : in) {        // for each input driver, get its data
            JSONObject driver = (JSONObject) o;
            //JSONObject driver = (JSONObject) in.get(4);
            long id = (long) driver.get("input_trip_id");
            double startLat = (double) driver.get("start_latitude");
            double startLon = (double) driver.get("start_longitude");
            String startTime = (String) driver.get("start_time");
            String maxTime = (String) driver.get("max_destination_time");
            LinkedList<Load> routing = new LinkedList<>();
            getOptimalRoute(routing, startLat, startLon, startTime, maxTime);

            JSONObject trip = new JSONObject();
            trip.put("input_trip_id", id);
            double[] loadIDs = new double[routing.size()];
            if (routing.size() >= 1) {
                //LinkedList<Load> routing = getOptimalRoute(startLat, startLon, startTime, maxTime);
                double totalProfit = 0;
                for (Load l : routing) {
                    totalProfit += l.getProfit();
                    System.out.print(l.getID() + ", ");
                }
                System.out.println("$" + totalProfit);
            } else {
                System.out.println("No routes");
            }
            trip.put("load_ids", loadIDs);
            tripList.add(trip);
        }


        //Write JSON file
        try (FileWriter file = new FileWriter("output.json")) {
            file.write(tripList.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
