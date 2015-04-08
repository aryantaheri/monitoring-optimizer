package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class MonitoringHostStats {

    int flowNum = 0;
    Map<MonitoringHost, Integer> monitoringHostUsage = null;
    DescriptiveStatistics costStats;
    DescriptiveStatistics reuseStats;

    public MonitoringHostStats(int flowNum,
            Map<MonitoringHost, Integer> monitoringHostUsage) {

        this.flowNum = flowNum;
        this.monitoringHostUsage = new TreeMap<MonitoringHost, Integer>(new NodeComparator());
        this.monitoringHostUsage.putAll(monitoringHostUsage);
        initStats();
    }

    private void initStats() {
        costStats = new DescriptiveStatistics();
        reuseStats = new DescriptiveStatistics();
        for (MonitoringHost host : monitoringHostUsage.keySet()) {
            costStats.addValue(host.getCost());
            reuseStats.addValue(monitoringHostUsage.get(host));
        }
    }

    public int getMonitoringHostNum() {
        return monitoringHostUsage.size();
    }

    public DescriptiveStatistics getReuseStats() {
        return reuseStats;
    }

    private Integer getHostUsage(MonitoringHost host) {
        return monitoringHostUsage.get(host);
    }


    public String getFactsString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n MonitoringHostsFacts: ");
        builder.append("\n     Costs[mean/min/max/std]:")
        .append(costStats.getMean()).append("/")
        .append(costStats.getMin()).append("/")
        .append(costStats.getMax()).append("/")
        .append(costStats.getStandardDeviation());

        return builder.toString();
    }

    public String getDetailedString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n #MonitoringHosts: ").append(monitoringHostUsage.size());
        builder.append("\n Monitoring Hosts Details: ");
        for (Entry<MonitoringHost, Integer> hostEntry : monitoringHostUsage.entrySet()) {
            builder.append("\n  ").append(hostEntry.getKey()).append(" reuse: ").append(hostEntry.getValue());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n #MonitoringHosts: ").append(monitoringHostUsage.size());

        return builder.toString();
    }

}
