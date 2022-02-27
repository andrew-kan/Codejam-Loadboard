/**
 * Load class holds information about origin, destination, and times.
 */
public class Load {
    private final long id;
    private final double profit;
    private final double weight;
    private final Object o;
    private final double orgLat;
    private final double orgLon;
    private final double destLat;
    private final double destLon;
    private final String pickupTime;
    private final String endTime;


    public Load(Object o, long id, double profit, double weight, double orgLat, double orgLon, double destLat, double destLon, String pickupTime, String endTime) {
        this.o = o;
        this.id = id;
        this.profit = profit;
        this.weight = weight;
        this.orgLat = orgLat;
        this.orgLon = orgLon;
        this.destLat = destLat;
        this.destLon = destLon;
        this.pickupTime = pickupTime;
        this.endTime = endTime;
    }

    public Object getO() { return o; }
    public long getID() { return id; }
    public double getProfit() { return profit; }
    public double getWeight() { return weight; }
    public double getOrgLat() { return orgLat; }
    public double getOrgLon() { return orgLon; }
    public double getDestLat() { return destLat; }
    public double getDestLon() { return destLon; }
    public String getPickupTime() { return pickupTime; }
    public String getEndTime() { return endTime; }

}
