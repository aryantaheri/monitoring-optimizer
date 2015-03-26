package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.apache.commons.collections15.map.HashedMap;

import edu.uci.ics.jung.graph.Graph;

public class MonitoringStatsManager {

    public static MonitoringStats getStats(MonitoringBalance monitoringSolution) {
        List<TrafficFlow> flows = new ArrayList<TrafficFlow>(monitoringSolution.getTrafficFlows());
        Collections.sort(flows, new Comparator<TrafficFlow>() {

            @Override
            public int compare(TrafficFlow f1, TrafficFlow f2) {
                return (f1.getSrcIp().toString() + f1.getDstIp().toString()).
                        compareToIgnoreCase((f2.getSrcIp().toString() + f2.getDstIp().toString()));
            }
        });

        MonitoringSwitchStats switchStats = getMonitoringSwitchStats(monitoringSolution.getTopology(), flows);
        MonitoringHostStats hostStats = getMonitoringHostStats(flows);
        MonitoringSwitchHostStats switchHostStats = getMonitoringSwitchHostStats(monitoringSolution.getAlgo(), flows);
        TrafficFlowStats flowStats = new TrafficFlowStats(flows);

        MonitoringStats stats = new MonitoringStats(switchStats, hostStats, switchHostStats, flowStats);
        return stats;
    }


    public static MonitoringSwitchStats getMonitoringSwitchStats(Graph<Node,WeightedLink> topology, List<TrafficFlow> flows) {
        Map<Switch, Integer> monitoringSwitchUsage = new HashedMap<Switch, Integer>();
        int onPathMonitoringSwitches = 0;

        List<WeightedLink> path;
        boolean isOnPath = false;
        Switch monitoringSwitch = null;
        for (TrafficFlow trafficFlow : flows) {
            monitoringSwitch = trafficFlow.getMonitoringSwitch();
            path = trafficFlow.getPath();
            isOnPath = TopologyManager.isSwitchOnPath(topology, path, monitoringSwitch);

            if(isOnPath) onPathMonitoringSwitches++;
            incrementMap(monitoringSwitchUsage, monitoringSwitch);
        }


        MonitoringSwitchStats stats = new MonitoringSwitchStats(
                onPathMonitoringSwitches,
                flows.size(),
                monitoringSwitchUsage);

        return stats;
    }

    public static MonitoringHostStats getMonitoringHostStats(List<TrafficFlow> flows){
        Map<MonitoringHost, Integer> monitoringHostUsage = new HashedMap<MonitoringHost, Integer>();

        MonitoringHost monitoringHost = null;
        for (TrafficFlow trafficFlow : flows) {
            monitoringHost = trafficFlow.getMonitoringHost();
            incrementMap(monitoringHostUsage, monitoringHost);
        }

        MonitoringHostStats stats = new MonitoringHostStats(flows.size(), monitoringHostUsage);
        return stats;
    }

    public static MonitoringSwitchHostStats getMonitoringSwitchHostStats(Yen<Node, WeightedLink> algo, List<TrafficFlow> flows) {
        Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap = new HashedMap<Switch, Set<MonitoringHost>>();

        for (TrafficFlow trafficFlow : flows) {
            addToMap(monitoringSwitchHostMap, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
        }
        MonitoringSwitchHostStats stats = new MonitoringSwitchHostStats(algo, monitoringSwitchHostMap);
        return stats;
    }


    private static void addToMap(
            Map<Switch, Set<MonitoringHost>> map,
            Switch monitoringSwitch, MonitoringHost monitoringHost) {

        Set<MonitoringHost> hosts = map.get(monitoringSwitch);
        if (hosts == null){
            hosts = new HashSet<MonitoringHost>();
        }
        hosts.add(monitoringHost);
        map.put(monitoringSwitch, hosts);
    }


    private static Map<Switch, Integer> incrementMap(Map<Switch , Integer> map, Switch node) {
        Integer usage = map.get(node);
        if(usage == null){
            usage = new Integer(0);
        }
        usage++;
        map.put(node, usage);
        return map;
    }

    private static Map<MonitoringHost, Integer> incrementMap(Map<MonitoringHost, Integer> map, MonitoringHost node) {
        Integer usage = map.get(node);
        if(usage == null){
            usage = new Integer(0);
        }
        usage++;
        map.put(node, usage);
        return map;
    }

}
