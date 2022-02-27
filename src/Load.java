/**
 * Load record holds information about origin, destination, and times.
 */
public record Load(Object o, long id, double profit, double weight, double orgLat, double orgLon,
                   double destLat, double destLon, String pickupTime, String endTime) {

    public Object getO() {return o;}
    public long getID() {return id;}
    public double getProfit() {return profit;}
    public double getWeight() {return weight;}
    public double getOrgLat() {return orgLat;}
    public double getOrgLon() {return orgLon;}
    public double getDestLat() {return destLat;}
    public double getDestLon() {return destLon;}
    public String getPickupTime() {return pickupTime;}
    public String getEndTime() {return endTime;}

}
