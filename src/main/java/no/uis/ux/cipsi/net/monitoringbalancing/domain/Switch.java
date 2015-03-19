package no.uis.ux.cipsi.net.monitoringbalancing.domain;

public class Switch extends Node {

    private double fabricCapacity;
    private double forwardingCapacity;
    private int tunnels;
    private double initCost;
    private boolean supportsMirroring;
    private TYPE type;

    public static final double DEFAULT_SWITCH_FABRIC_CAPACITY = 176*Math.pow(10, 9);      // 176G bps
    public static final double DEFAULT_SWITCH_FORWARDING_CAPACITY = 132*Math.pow(10, 6);       // 132M pps
    public static final int DEFAULT_SWITCH_COST = 10;
    public static final double DEFAULT_SWITCH_PERFLOW_REUSE_COST_RATIO = 0.05;

    public enum TYPE {
        CORE, AGGREGATION, EDGE
    }


    public Switch(String id, int fabricCapacity, int forwardingCapacity, double initCost, boolean supportsMirroring, TYPE type) {
        this.id = id;
        this.fabricCapacity = fabricCapacity;
        this.forwardingCapacity = forwardingCapacity;
        this.initCost = initCost;
        this.supportsMirroring = supportsMirroring;
        this.type = type;
    }

    public Switch(String id, boolean supportsMirroring, TYPE type) {
        this.id = id;
        this.fabricCapacity = DEFAULT_SWITCH_FABRIC_CAPACITY;
        this.forwardingCapacity = DEFAULT_SWITCH_FORWARDING_CAPACITY;
        this.initCost = DEFAULT_SWITCH_COST;
        this.supportsMirroring = supportsMirroring;
        this.type = type;
    }

    public static int getDefaultSwitchCost() {
        return DEFAULT_SWITCH_COST;
    }

    public double getInitCost() {
        return initCost;
    }

    public double getPerFlowReuseCost() {
        return initCost*DEFAULT_SWITCH_PERFLOW_REUSE_COST_RATIO;
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

    //    @Override
    //    public int hashCode() {
    //        final int prime = 31;
    //        int result = 1;
    //        long temp;
    //        temp = Double.doubleToLongBits(fabricCapacity);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(forwardingCapacity);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(initCost);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        result = prime * result + (supportsMirroring ? 1231 : 1237);
    //        result = prime * result + tunnels;
    //        result = prime * result + ((type == null) ? 0 : type.hashCode());
    //        return result;
    //    }
    //
    //    @Override
    //    public boolean equals(Object obj) {
    //        if (this == obj) {
    //            return true;
    //        }
    //        if (!super.equals(obj)) {
    //            return false;
    //        }
    //        if (!(obj instanceof Switch)) {
    //            return false;
    //        }
    //        Switch other = (Switch) obj;
    //        if (Double.doubleToLongBits(fabricCapacity) != Double
    //                .doubleToLongBits(other.fabricCapacity)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(forwardingCapacity) != Double
    //                .doubleToLongBits(other.forwardingCapacity)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(initCost) != Double
    //                .doubleToLongBits(other.initCost)) {
    //            return false;
    //        }
    //        if (supportsMirroring != other.supportsMirroring) {
    //            return false;
    //        }
    //        if (tunnels != other.tunnels) {
    //            return false;
    //        }
    //        if (type != other.type) {
    //            return false;
    //        }
    //        return true;
    //    }

    @Override
    public String toString() {
        return id;
    }
}
