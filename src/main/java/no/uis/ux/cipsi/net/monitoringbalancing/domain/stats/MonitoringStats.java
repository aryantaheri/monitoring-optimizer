package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;

public class MonitoringStats {

    HardSoftBigDecimalScore score;
    MonitoringSwitchStats switchStats;
    MonitoringHostStats hostStats;
    MonitoringSwitchHostStats switchHostStats;
    TrafficFlowStats flowStats;

    public MonitoringStats(HardSoftBigDecimalScore score,
            MonitoringSwitchStats monitoringSwitchStats,
            MonitoringHostStats monitoringHostStats,
            MonitoringSwitchHostStats monitoringSwitchHostStats,
            TrafficFlowStats trafficFlowStats) {
        this.score = score;
        this.switchStats = monitoringSwitchStats;
        this.hostStats = monitoringHostStats;
        this.switchHostStats = monitoringSwitchHostStats;
        this.flowStats = trafficFlowStats;
    }

    public HardSoftBigDecimalScore getScore() {
        return score;
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
