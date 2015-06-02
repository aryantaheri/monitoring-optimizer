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
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class MonitoringSwitchHostStats {

    private static Logger log = LoggerFactory.getLogger(MonitoringSwitchHostStats.class);

    Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap;
    Map<Switch, Map<MonitoringHost, Integer>> switchHostDistanceMap;
    Map<MonitoringHost, Map<Switch, Integer>> hostSwitchDistanceMap;
    List<List<WeightedLink>> monitoringPaths;
    //    Yen<Node,WeightedLink> algo;
    Graph<Node,WeightedLink> topology;
    Configs configs;
    List<TrafficFlow> flows;
    int nullMonitoringPaths = 0;

    public MonitoringSwitchHostStats(Graph<Node,WeightedLink> topology, Configs configs, Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap, List<TrafficFlow> flows) {
        this.monitoringSwitchHostMap = monitoringSwitchHostMap;
        this.monitoringPaths = new ArrayList<List<WeightedLink>>();
        this.flows = flows;
        this.topology = topology;
        this.configs = configs;

        initDistanceMaps();
    }

    private void initDistanceMaps() {
        switchHostDistanceMap = new HashMap<Switch, Map<MonitoringHost,Integer>>();
        hostSwitchDistanceMap = new HashMap<MonitoringHost, Map<Switch,Integer>>();

        for (TrafficFlow flow : flows) {
            Switch sw = flow.getMonitoringSwitch();
            MonitoringHost host = flow.getMonitoringHost();
            List<WeightedLink> monPath = flow.getMonitoringSwitchHostPath();
            if (monPath == null) {
                //FIXME This is wrong
                log.error("Monitoring Path is NULL flow={}", flow);
                monPath = TopologyManager.getDeterministicShortestPath(topology, configs, sw, host, 4, flow);
                flow.setMonitoringSwitchHostPath(monPath);

                nullMonitoringPaths++;
            }
            addToMap(hostSwitchDistanceMap, host, sw, monPath.size());
            addToMap(switchHostDistanceMap, sw, host, monPath.size());
            monitoringPaths.add(monPath);
        }
    }

    private void addToMap(Map<Switch, Map<MonitoringHost, Integer>> map, Switch sw, MonitoringHost host, int distance) {
        Map<MonitoringHost, Integer> hostMap = map.get(sw);
        if (hostMap == null) {
            hostMap = new HashMap<MonitoringHost, Integer>();
        }
        hostMap.put(host, distance);
        map.put(sw, hostMap);
    }

    private void addToMap(Map<MonitoringHost, Map<Switch, Integer>> map, MonitoringHost host, Switch sw, int distance) {
        Map<Switch, Integer> swMap = map.get(host);
        if (swMap == null) {
            swMap = new HashMap<Switch, Integer>();
        }
        swMap.put(sw, distance);
        map.put(host, swMap);
    }

    private String getSwitchMapString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n Switch-Host Mapping:");
        builder.append("\n NullPaths:").append(nullMonitoringPaths);

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
        builder.append("\n NullPaths:").append(nullMonitoringPaths);

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
        if (sw == null) return stats;

        for (MonitoringHost host : hosts) {
            if (host == null) continue;
            //            List<WeightedLink> path = TopologyManager.getRandomShortestPath(algo, sw, host, 4);
            Integer d = switchHostDistanceMap.get(sw).get(host);
            if (d != null) stats.addValue(d);
            // TODO: move it to a dedicated method, here for efficiency
            //            monitoringPaths.add(path);
        }
        return stats;
    }

    private DescriptiveStatistics getDistanceStats(MonitoringHost host, Set<Switch> switches) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        if (host == null) return stats;

        for (Switch sw : switches) {
            if (sw == null) continue;
            //            List<WeightedLink> path = TopologyManager.getRandomShortestPath(algo, host, sw, 4);
            Integer d = hostSwitchDistanceMap.get(host).get(sw);
            if (d != null) stats.addValue(d);
        }
        return stats;
    }

    public DescriptiveStatistics getSwitchHostDistanceStats(){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Map<MonitoringHost, Integer> hostMap : switchHostDistanceMap.values()) {
            for (Integer distance : hostMap.values()) {
                if (distance != null) stats.addValue(distance);
            }
        }
        return stats;
    }

    public DescriptiveStatistics getHostSwitchDistanceStats(){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Map<Switch, Integer> swMap : hostSwitchDistanceMap.values()) {
            for (Integer distance : swMap.values()) {
                if (distance != null) stats.addValue(distance);
            }
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
