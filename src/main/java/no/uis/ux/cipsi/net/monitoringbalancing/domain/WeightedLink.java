package no.uis.ux.cipsi.net.monitoringbalancing.domain;

import java.io.Serializable;

public class WeightedLink implements Serializable{

    String id;
    double speed;  // bps
    int utilization;    // Link utilization %
    double cost;   //
    double podSensitivity;

    public static final double DEFAULT_LINK_SPEED = Math.pow(10, 9); // 1Gbps
    public static final int DEFAULT_LINK_UTILIZATION = 30;
    public static final int DEFAULT_LINK_COST = 10;
    public static double DEFAULT_POD_SENSITIVITY = 1;

    public WeightedLink(String id) {
        this.id = id;
        this.speed = DEFAULT_LINK_SPEED;
        this.utilization = DEFAULT_LINK_UTILIZATION;
        this.cost = DEFAULT_LINK_COST;
        this.podSensitivity = DEFAULT_POD_SENSITIVITY;
    }


    public double getSpeed() {
        return speed;
    }

    public double getPodSensitivity() {
        return podSensitivity;
    }
    /**
     * SwitchCost = (speed/flowRate)*distance*podSensitivity*LinksCost
     * LinkCost = SwitchCost/((speed/flowRate)*podSensitivity)
     * @return
     */
    public double getMonitorServiceCost(double flowRate) {
        double switchCost = Switch.getDefaultInitCost();
        cost = switchCost/((speed/flowRate)*podSensitivity);
        return cost;
    }

    public static void setDefaultPodSensitivity(double value) {
        DEFAULT_POD_SENSITIVITY = value;
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
