package no.uis.ux.cipsi.net.monitoringbalancing.domain;

public class Switch extends Node {

    private double fabricCapacity;
    private double forwardingCapacity;
    private int tunnels;
    private double initCost;
    private double perFlowReuseCostRatio;
    private boolean supportsMirroring;
    private TYPE type;

    public static final double DEFAULT_SWITCH_FABRIC_CAPACITY = 176*Math.pow(10, 9);      // 176G bps
    public static final double DEFAULT_SWITCH_FORWARDING_CAPACITY = 132*Math.pow(10, 6);       // 132M pps
    public static double DEFAULT_SWITCH_COST = 10;
    public static double DEFAULT_SWITCH_PERFLOW_REUSE_COST_RATIO = 0.05;

    public enum TYPE {
        CORE, AGGREGATION, EDGE
    }


    //    public Switch(String id, int fabricCapacity, int forwardingCapacity, double initCost, boolean supportsMirroring, TYPE type) {
    //        this.id = id;
    //        this.fabricCapacity = fabricCapacity;
    //        this.forwardingCapacity = forwardingCapacity;
    //        this.initCost = initCost;
    //        this.supportsMirroring = supportsMirroring;
    //        this.type = type;
    //    }

    public Switch(String id, boolean supportsMirroring, TYPE type, double initCost, double perflowReuseCostRatio) {
        this.id = id;
        this.fabricCapacity = DEFAULT_SWITCH_FABRIC_CAPACITY;
        this.forwardingCapacity = DEFAULT_SWITCH_FORWARDING_CAPACITY;
        this.initCost = initCost;
        this.perFlowReuseCostRatio = perflowReuseCostRatio;
        this.supportsMirroring = supportsMirroring;
        this.type = type;
    }


    public double getInitCost() {
        return initCost;
    }

    public double getPerFlowReuseCost() {
        return initCost*perFlowReuseCostRatio;
    }

    public double getPerFlowReuseCostRatio() {
        return perFlowReuseCostRatio;
    }

    public double getFabricCapacity() {
        return fabricCapacity;
    }

    public double getForwardingCapacity() {
        return forwardingCapacity;
    }

    public TYPE getType() {
        return type;
    }

    //    public static void setDefaultInitCost(double cost){
    //        DEFAULT_SWITCH_COST = cost;
    //    }
    //
    //    public static double getDefaultInitCost() {
    //        return DEFAULT_SWITCH_COST;
    //    }
    //
    //    public static void setDefaultPerFlowReuseCostRatio(double ratio) {
    //        DEFAULT_SWITCH_PERFLOW_REUSE_COST_RATIO = ratio;
    //    }


    @Override
    public String toString() {
        return id;
    }
}
