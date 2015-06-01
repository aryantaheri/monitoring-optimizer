package no.uis.ux.cipsi.net.monitoringbalancing.domain;

import java.io.Serializable;

public class WeightedLink implements Serializable{

    String id;
    double speed;  // bps
    int utilization;    // Link utilization %
    double cost;   //
    double podSensitivity;
    double switchCost;

    public static final double DEFAULT_LINK_SPEED = Math.pow(10, 9); // 1Gbps
    public static final int DEFAULT_LINK_UTILIZATION = 0;
    public static final int DEFAULT_LINK_COST = 10;
    public static double DEFAULT_POD_SENSITIVITY = 1;

    public WeightedLink(String id, double podSensitivity, double switchCost) {
        this.id = id;
        this.speed = DEFAULT_LINK_SPEED;
        this.utilization = DEFAULT_LINK_UTILIZATION;
        this.cost = DEFAULT_LINK_COST;
        this.podSensitivity = podSensitivity;
        this.switchCost = switchCost;
    }

    public WeightedLink(String id, double podSensitivity, double switchCost, int utilization) {
        this.id = id;
        this.speed = DEFAULT_LINK_SPEED;
        this.utilization = utilization;
        this.cost = DEFAULT_LINK_COST;
        this.podSensitivity = podSensitivity;
        this.switchCost = switchCost;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPodSensitivity() {
        return podSensitivity;
    }

    /**
     *
     * @return The percentage of the link utilization
     */
    public int getUtilizationPercentage() {
        return utilization;
    }

    /**
     * The utilization*speed/100
     * @return The amount of bandwidth used on this link.
     */
    public double getUsage() {
        return speed*utilization/100;
    }

    /**
     * SwitchCost = (speed/flowRate)*distance*podSensitivity*LinksCost
     * LinkCost = SwitchCost/((speed/flowRate)*podSensitivity)
     * @return
     */
    public double getMonitorServiceCost(double flowRate) {
        cost = switchCost/((speed/flowRate)*podSensitivity);
        return cost;
    }

    //    public static void setDefaultPodSensitivity(double value) {
    //        DEFAULT_POD_SENSITIVITY = value;
    //    }
    /**
     * Used for shortest path calculation
     * @return
     */
    public int getWeight() {
        return 1;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof WeightedLink)) {
            return false;
        }
        WeightedLink other = (WeightedLink) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return id;
    }
}
