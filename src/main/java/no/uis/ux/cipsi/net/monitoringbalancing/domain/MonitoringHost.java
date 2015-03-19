package no.uis.ux.cipsi.net.monitoringbalancing.domain;

/*
 * Monitoring Hosts (Service Node)
 */
public class MonitoringHost extends Host {

    private double cpuPower;   // GHz
    private double nicSpeed;   // Gbps
    private double memory;     // GB
    private double storage;    // GB
    private double cost;       // $pm



    private static final double DEFAULT_HOST_COST = 100;

    public MonitoringHost(String id) {
        super(id);
        this.id = id;
        this.cost = DEFAULT_HOST_COST;
    }

    public double getCpuPower() {
        return cpuPower;
    }
    public void setCpuPower(double cpuPower) {
        this.cpuPower = cpuPower;
    }
    public double getNicSpeed() {
        return nicSpeed;
    }
    public void setNicSpeed(double nicSpeed) {
        this.nicSpeed = nicSpeed;
    }
    public double getMemory() {
        return memory;
    }
    public void setMemory(double memory) {
        this.memory = memory;
    }
    public double getStorage() {
        return storage;
    }
    public void setStorage(double storage) {
        this.storage = storage;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public double getCost() {
        return cost;
    }

    public double getMultiplicand() {
        return cpuPower * nicSpeed * memory * storage;
    }



    //    @Override
    //    public int hashCode() {
    //        final int prime = 31;
    //        int result = 1;
    //        long temp;
    //        temp = Double.doubleToLongBits(cost);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(cpuPower);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(memory);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(nicSpeed);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        temp = Double.doubleToLongBits(storage);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
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
    //        if (!(obj instanceof MonitoringHost)) {
    //            return false;
    //        }
    //        MonitoringHost other = (MonitoringHost) obj;
    //        if (Double.doubleToLongBits(cost) != Double
    //                .doubleToLongBits(other.cost)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(cpuPower) != Double
    //                .doubleToLongBits(other.cpuPower)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(memory) != Double
    //                .doubleToLongBits(other.memory)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(nicSpeed) != Double
    //                .doubleToLongBits(other.nicSpeed)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(storage) != Double
    //                .doubleToLongBits(other.storage)) {
    //            return false;
    //        }
    //        return true;
    //    }

    @Override
    public String toString() {
        return id;
    }
}
