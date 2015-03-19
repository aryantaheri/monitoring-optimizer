package no.uis.ux.cipsi.net.monitoringbalancing.domain;

public class WeightedLink {

    String id;
    double speed;  // bps
    int utilization;    // Link utilization %
    double cost;   //
    double podSensitivity = 1;

    public static final double DEFAULT_LINK_SPEED = Math.pow(10, 9); // 1Gbps
    public static final int DEFAULT_LINK_UTILIZATION = 30;
    public static final int DEFAULT_LINK_COST = 10;

    public WeightedLink(String id) {
        this.id = id;
        this.speed = DEFAULT_LINK_SPEED;
        this.utilization = DEFAULT_LINK_UTILIZATION;
        this.cost = DEFAULT_LINK_COST;
    }


    public double getSpeed() {
        return speed;
    }

    /**
     * SwitchCost = (speed/flowRate)*distance*podSensitivity*LinksCost
     * LinkCost = SwitchCost/((speed/flowRate)*podSensitivity)
     * @return
     */
    public double getMonitorServiceCost(double flowRate) {
        int switchCost = Switch.getDefaultSwitchCost();
        cost = switchCost/((speed/flowRate)*podSensitivity);
        return cost;
    }

    /**
     * Used for shortest path calculation
     * @return
     */
    public int getWeight() {
        return 1;
    }

    @Override
    public String toString() {
        return id;
    }
}
