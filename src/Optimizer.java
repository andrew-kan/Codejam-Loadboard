import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Optimizer {

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

    static double findDifference(String start_date, String end_date) {
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


    public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException {
        double speed = 55.0; // MPH
        double fuelCost = 0.40; // $/mile

        double startLat = 27.961307;
        double startLon = -82.4493;
        String startTime = "2022-02-04 08:00:00";
        String endTime = "2022-02-06 15:00:00";
        //System.out.println(findDifference(startTime, endTime));
        // speed*distance < findDifference time

        // for trips in file
        // find trips with origins within 2 lat, 2 lon within time (deadhead)
        // get profit (gain - fuelcost)
        // find most profitable trip
        // go on trip - add to array
        // change time
        // repeat

        JSONParser parser = new JSONParser();
        JSONArray a = (JSONArray) parser.parse(new FileReader("123Loadboard_CodeJam_2022_dataset.json"));
        for (Object o : a) {
            JSONObject trip = (JSONObject) o;
            long id = (long) trip.get("load_id");
            double orgLat = (double) trip.get("origin_latitude");
            double orgLon = (double) trip.get("origin_longitude");
            double destLat = (double) trip.get("destination_latitude");
            double destLon = (double) trip.get("destination_longitude");
            String pickupTime = (String) trip.get("pickup_date_time");
            long amount = (long) trip.get("amount");
            if (Math.abs(orgLat-startLat) < 2 && Math.abs(orgLon-startLon) < 2) {
                double deadDist = calc_distance(startLat, startLon, orgLat, orgLon);
                double loadDist = calc_distance(orgLat, orgLon, destLat, destLon);
                double profit = amount - loadDist*fuelCost - deadDist*fuelCost;

                if (profit > 0) {
                    System.out.println("ID: "+id+"\tProfit: $" + profit);
                }
            }
        }

    }


}
