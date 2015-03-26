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



    private static double DEFAULT_HOST_COST = 100;

    public MonitoringHost(String id, double cost) {
        super(id);
        this.id = id;
        this.cost = cost;
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

    //    public static void setDefaultCost(double cost) {
    //        DEFAULT_HOST_COST = cost;
    //    }
    //
    //    public static double getDefaultCost() {
    //        return DEFAULT_HOST_COST;
    //    }


    @Override
    public String toString() {
        return id;
    }
}
