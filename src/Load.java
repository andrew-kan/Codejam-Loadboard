import java.util.Objects;

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

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        Load load = (Load) o1;
        return id == load.id && o.equals(load.o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(o, id);
    }

    // Would also hold routing linkedlist and total profit before this load.
}
