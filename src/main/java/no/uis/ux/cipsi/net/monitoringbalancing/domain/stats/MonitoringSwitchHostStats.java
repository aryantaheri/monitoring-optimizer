package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class MonitoringSwitchHostStats {

    Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap;
    List<List<WeightedLink>> monitoringPaths;

    public MonitoringSwitchHostStats(
            Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap) {
        this.monitoringSwitchHostMap = monitoringSwitchHostMap;
        this.monitoringPaths = new ArrayList<List<WeightedLink>>();
    }

    private String getSwitchMapString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n Switch-Host Mapping:");
        for (Entry<Switch, Set<MonitoringHost>> switchEntry : monitoringSwitchHostMap.entrySet()) {
            DescriptiveStatistics s = getDistanceStats(switchEntry.getKey(), switchEntry.getValue());
            builder.append("\n  ").append(switchEntry.getKey()).append(" -> ").append(switchEntry.getValue())
            .append(" Distance[mean/min/max/std]: ")
            .append(s.getMean()).append("/")
            .append(s.getMin()).append("/")
            .append(s.getMax()).append("/")
            .append(s.getStandardDeviation())
            ;
        }
        return builder.toString();
    }

    private String getHostMapString() {
        Map<MonitoringHost, Set<Switch>> hostSwitchMap = getHostSwitchMap();
        StringBuilder builder = new StringBuilder();
        builder.append("\n Host-Switch Mapping:");
        for (Entry<MonitoringHost, Set<Switch>> hostEntry : hostSwitchMap.entrySet()) {
            DescriptiveStatistics s = getDistanceStats(hostEntry.getKey(), hostEntry.getValue());
            builder.append("\n  ").append(hostEntry.getKey()).append(" -> ").append(hostEntry.getValue())
            .append(" Distance[mean/min/max/std]: ")
            .append(s.getMean()).append("/")
            .append(s.getMin()).append("/")
            .append(s.getMax()).append("/")
            .append(s.getStandardDeviation())
            ;
        }
        return builder.toString();
    }

    public String getLinkFactsString() {
        DescriptiveStatistics s = new DescriptiveStatistics();
        for (List<WeightedLink> path : monitoringPaths) {
            for (WeightedLink weightedLink : path) {
                s.addValue(weightedLink.getPodSensitivity());
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\n Link Facts: ")
        .append("\n    PodSensitivity[mean/min/max/std]: ")
        .append(s.getMean()).append("/")
        .append(s.getMin()).append("/")
        .append(s.getMax()).append("/")
        .append(s.getStandardDeviation());
        return builder.toString();
    }

    private Map<MonitoringHost, Set<Switch>> getHostSwitchMap() {
        Map<MonitoringHost, Set<Switch>> hostSwitchMap = new HashMap<MonitoringHost, Set<Switch>>();

        for (Entry<Switch, Set<MonitoringHost>> switchEntry : monitoringSwitchHostMap.entrySet()) {
            for (MonitoringHost host : switchEntry.getValue()) {
                Set<Switch> switches = hostSwitchMap.get(host);
                if (switches == null){
                    switches = new HashSet<Switch>();
                }
                switches.add(switchEntry.getKey());
                hostSwitchMap.put(host, switches);
            }
        }
        return hostSwitchMap;
    }

    private DescriptiveStatistics getDistanceStats(Switch sw, Set<MonitoringHost> hosts) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (MonitoringHost host : hosts) {
            List<WeightedLink> path = TopologyManager.getInstance().getRandomShortestPath(sw, host, 4);
            stats.addValue(path.size());
            // TODO: move it to a dedicated method, here for efficiency
            monitoringPaths.add(path);
        }
        return stats;
    }

    private DescriptiveStatistics getDistanceStats(MonitoringHost host, Set<Switch> switches) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Switch sw : switches) {
            List<WeightedLink> path = TopologyManager.getInstance().getRandomShortestPath(host, sw, 4);
            stats.addValue(path.size());
        }
        return stats;
    }

    private String getSwitchHostMapString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getSwitchMapString())
        .append(getHostMapString());
        return builder.toString();
    }

    @Override
    public String toString() {
        return getSwitchHostMapString();
    }
}
