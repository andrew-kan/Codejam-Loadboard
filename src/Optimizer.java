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



    JSONParser jsonParser = new JSONParser();

        try (
    FileReader reader = new FileReader("employees.json"))
    {
        //Read JSON file
        Object obj = jsonParser.parse(reader);

        JSONArray employeeList = (JSONArray) obj;
        System.out.println(employeeList);

        //Iterate over employee array
        employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }


    public static void main(String[] args) {
        double speed = 55.0; // MPH
        double fuelCost = 0.40; // $/mile

        double startLat = 27.961307;
        double startLon = -82.4493;
        String startTime = "2022-02-04 08:00:00";
        String endTime = "2022-02-06 15:00:00";
        System.out.println(findDifference(startTime, endTime));
        // speed*distance < findDifference time

        // for trips in file
        // find trips with origins within 2 lat, 2 lon within time (deadhead)
        // get profit (gain - fuelcost)
        // find most profitable trip
        // go on trip - add to array
        // change time
        // repeat

        //double d = calc_distance(startLat, startLon, startLat+2, startLon+2);
        //System.out.println(d*speed*fuelCost);
    }


}
