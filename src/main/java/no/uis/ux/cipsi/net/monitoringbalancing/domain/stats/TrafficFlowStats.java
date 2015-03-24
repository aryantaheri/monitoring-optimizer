package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TrafficFlowStats {

    List<TrafficFlow> flows = null;
    DescriptiveStatistics rateStats;

    public TrafficFlowStats(List<TrafficFlow> flows) {
        this.flows = flows;
        initStats();
    }

    private void initStats() {
        rateStats = new DescriptiveStatistics();
        for (TrafficFlow trafficFlow : flows) {
            rateStats.addValue(trafficFlow.getRate());
        }
    }

    public String getFactsString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n TrafficFlowFacts: ");
        builder.append("\n     Rate[mean/min/max/std]:")
        .append(rateStats.getMean()).append("/")
        .append(rateStats.getMin()).append("/")
        .append(rateStats.getMax()).append("/")
        .append(rateStats.getStandardDeviation());

        return builder.toString();
    }

    @Override
    public String toString() {
        return "\n #TrafficFlows: " + flows.size();
    }
}
