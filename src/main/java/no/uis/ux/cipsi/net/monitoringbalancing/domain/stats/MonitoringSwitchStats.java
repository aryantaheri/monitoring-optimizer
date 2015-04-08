package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


public class MonitoringSwitchStats {

    int monitoringSwitchesNum = 0;
    int onPathMonitoringSwitches = 0;
    int flowNum = 0;

    int coreSwMonNum = 0;
    int aggrSwMonNum = 0;
    int edgeSwMonNum = 0;

    int coreSwMonReuse = 0;
    int aggrSwMonReuse = 0;
    int edgeSwMonReuse = 0;

    Map<Switch, Integer> monitoringSwitchUsage = null;
    DescriptiveStatistics initCostStats;
    DescriptiveStatistics perFlowReuseCostRatioStats;


    public MonitoringSwitchStats(
            int onPathMonitoringSwitches,
            int flowNum,
            Map<Switch, Integer> monitoringSwitchUsage ) {
        this.onPathMonitoringSwitches = onPathMonitoringSwitches;
        this.flowNum = flowNum;
        this.monitoringSwitchUsage = new TreeMap<Switch, Integer>(new NodeComparator());
        this.monitoringSwitchUsage.putAll(monitoringSwitchUsage);

        this.coreSwMonNum = getSwitchCountInLayer(TYPE.CORE);
        this.aggrSwMonNum = getSwitchCountInLayer(TYPE.AGGREGATION);
        this.edgeSwMonNum = getSwitchCountInLayer(TYPE.EDGE);

        this.coreSwMonReuse = getSwitchReuseInLayer(TYPE.CORE);
        this.aggrSwMonReuse = getSwitchReuseInLayer(TYPE.AGGREGATION);
        this.edgeSwMonReuse = getSwitchReuseInLayer(TYPE.EDGE);

        this.monitoringSwitchesNum = monitoringSwitchUsage.size();
        initStats();
    }

    private void initStats(){
        initCostStats = new DescriptiveStatistics();
        perFlowReuseCostRatioStats = new DescriptiveStatistics();
        for (Switch sw : monitoringSwitchUsage.keySet()) {
            initCostStats.addValue(sw.getInitCost());
            perFlowReuseCostRatioStats.addValue(sw.getPerFlowReuseCostRatio());
        }
    }

    public DescriptiveStatistics getInitCostStats() {
        return initCostStats;
    }

    public DescriptiveStatistics getPerFlowReuseCostRatioStats() {
        return perFlowReuseCostRatioStats;
    }

    public int getMonitoringSwitchesNum() {
        return monitoringSwitchesNum;
    }

    public int getSwitchCountInLayer(TYPE type) {
        int layerSw = 0;
        for (Entry<Switch, Integer> swEntry : monitoringSwitchUsage.entrySet()) {
            if (swEntry.getKey().getType() == type){
                layerSw++;
            }
        }
        return layerSw;
    }

    public int getSwitchReuseInLayer(TYPE type) {
        int layerReuse = 0;
        for (Entry<Switch, Integer> swEntry : monitoringSwitchUsage.entrySet()) {
            if (swEntry.getKey().getType() == type){
                layerReuse += swEntry.getValue();
            }
        }
        return layerReuse;
    }

    public String getFactsString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n MonitoringSwitchesFacts: ");
        builder.append("\n     InitCosts[mean/min/max/std]:")
        .append(initCostStats.getMean()).append("/")
        .append(initCostStats.getMin()).append("/")
        .append(initCostStats.getMax()).append("/")
        .append(initCostStats.getStandardDeviation());

        builder.append("\n     PerFlowCostRatio[mean/min/max/std]:")
        .append(perFlowReuseCostRatioStats.getMean()).append("/")
        .append(perFlowReuseCostRatioStats.getMin()).append("/")
        .append(perFlowReuseCostRatioStats.getMax()).append("/")
        .append(perFlowReuseCostRatioStats.getStandardDeviation());
        return builder.toString();
    }

    public String getDetailedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(toString());
        builder.append("\n Monitoring Switch Details: ");
        for (Entry<Switch, Integer> swEntry : monitoringSwitchUsage.entrySet()) {
            builder.append("\n    ")
            .append(swEntry.getKey()).append(" ")
            .append(swEntry.getKey().getType()).append(" reuse: ").append(swEntry.getValue()) ;
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n #MonitoringSwitches: ").append(monitoringSwitchesNum)
        .append("\n   MonitoringSwitchesLayer: ")
        .append("\n     ").append(coreSwMonNum).append("-Core ").append("reuse: ").append(coreSwMonReuse)
        .append("\n     ").append(aggrSwMonNum).append("-Aggr ").append("reuse: ").append(aggrSwMonReuse)
        .append("\n     ").append(edgeSwMonNum).append("-Edge ").append("reuse: ").append(edgeSwMonReuse)
        .append("\n #FlowsWithOnPathMonitoringSwitches: ").append(onPathMonitoringSwitches).append("/").append(flowNum);

        return builder.toString();
    }
}
