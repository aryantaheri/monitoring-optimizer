package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

public class MonitoringStats {

    MonitoringSwitchStats switchStats;
    MonitoringHostStats hostStats;
    MonitoringSwitchHostStats switchHostStats;
    TrafficFlowStats flowStats;

    public MonitoringStats(MonitoringSwitchStats monitoringSwitchStats,
            MonitoringHostStats monitoringHostStats,
            MonitoringSwitchHostStats monitoringSwitchHostStats,
            TrafficFlowStats trafficFlowStats) {
        this.switchStats = monitoringSwitchStats;
        this.hostStats = monitoringHostStats;
        this.switchHostStats = monitoringSwitchHostStats;
        this.flowStats = trafficFlowStats;
    }

    public MonitoringSwitchStats getSwitchStats() {
        return switchStats;
    }

    public MonitoringHostStats getHostStats() {
        return hostStats;
    }

    public MonitoringSwitchHostStats getSwitchHostStats() {
        return switchHostStats;
    }

    public TrafficFlowStats getFlowStats() {
        return flowStats;
    }



}
