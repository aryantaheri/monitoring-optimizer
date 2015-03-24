package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.apache.commons.collections15.map.HashedMap;

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

        MonitoringSwitchStats switchStats = getMonitoringSwitchStats(flows);
        MonitoringHostStats hostStats = getMonitoringHostStats(flows);
        MonitoringSwitchHostStats switchHostStats = getMonitoringSwitchHostStats(flows);
        TrafficFlowStats flowStats = new TrafficFlowStats(flows);

        MonitoringStats stats = new MonitoringStats(switchStats, hostStats, switchHostStats, flowStats);
        return stats;
    }


    public static MonitoringSwitchStats getMonitoringSwitchStats(List<TrafficFlow> flows) {
        Map<Switch, Integer> monitoringSwitchUsage = new HashedMap<Switch, Integer>();
        int onPathMonitoringSwitches = 0;

        List<WeightedLink> path;
        boolean isOnPath = false;
        Switch monitoringSwitch = null;
        for (TrafficFlow trafficFlow : flows) {
            monitoringSwitch = trafficFlow.getMonitoringSwitch();
            path = trafficFlow.getPath();
            isOnPath = TopologyManager.getInstance().isSwitchOnPath(path, monitoringSwitch);

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

    public static MonitoringSwitchHostStats getMonitoringSwitchHostStats(List<TrafficFlow> flows) {
        Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap = new HashedMap<Switch, Set<MonitoringHost>>();

        for (TrafficFlow trafficFlow : flows) {
            addToMap(monitoringSwitchHostMap, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
        }
        MonitoringSwitchHostStats stats = new MonitoringSwitchHostStats(monitoringSwitchHostMap);
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


    public static String toDisplayString(MonitoringBalance monitoringBalance) {
        List<TrafficFlow> flows = monitoringBalance.getTrafficFlows();
        Collections.sort(flows, new Comparator<TrafficFlow>() {

            @Override
            public int compare(TrafficFlow f1, TrafficFlow f2) {
                return (f1.getSrcIp().toString() + f1.getDstIp().toString()).
                        compareToIgnoreCase((f2.getSrcIp().toString() + f2.getDstIp().toString()));
            }
        });
        StringBuilder displayString = new StringBuilder();
        Map<Node, Integer> monitoringSwitchUsage = new HashedMap<Node, Integer>();
        Map<Node, Integer> monitoringHostUsage = new HashedMap<Node, Integer>();
        int onPathMonitoringSwitches = 0;
        int distanceSum = 0;

        Switch monitoringSwitch;
        MonitoringHost monitoringHost;
        List<WeightedLink> path;
        boolean isOnPath = false;
        int distance = 0;
        displayString.append("Score: ").append(monitoringBalance.getScore()).append('\n');
        for (TrafficFlow flow : flows) {
            monitoringSwitch = flow.getMonitoringSwitch();
            monitoringHost = flow.getMonitoringHost();
            path = flow.getPath();
            isOnPath = TopologyManager.getInstance().isSwitchOnPath(path, monitoringSwitch);
            distance = TopologyManager.getInstance().getRandomShortestPath(flow.getMonitoringSwitch(), flow.getMonitoringHost(), 4).size();


            if (isOnPath) onPathMonitoringSwitches++;
            distanceSum += distance;
            //            incrementMap(monitoringSwitchUsage, monitoringSwitch);
            //            incrementMap(monitoringHostUsage, monitoringHost);


            displayString.append(" ").append(flow).append(" -> ")
            .append(monitoringSwitch).append(" ")
            .append(monitoringHost).append(" ")
            .append(" SwitchOnPath: ").append(isOnPath)
            .append(" Distance: ~").append(distance)
            .append('\n');
        }

        //        displayString.append(" #MonitoringSwitches: ").append(monitoringSwitchUsage.size())
        //        .append("\n   MonitoringSwitchesLayer: ")
        //        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.CORE)).append("-Core ")
        //        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.CORE))
        //        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.AGGREGATION)).append("-Aggr ")
        //        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.AGGREGATION))
        //        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.EDGE)).append("-Edge ")
        //        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.EDGE))
        //        .append("\n #OnPathMonitoringSwitches: ").append(onPathMonitoringSwitches)
        //        .append("\n #Flows: ").append(flows.size())
        //        .append("\n #MonitoringHosts: ").append(monitoringHostUsage.size())
        //        .append("\n Mean SwitchHost distance: ").append(distanceSum/flows.size())
        //
        //        ;

        displayString.append("\n Detailed Switch Stat: ");
        for (Entry<Node, Integer> swEntry : monitoringSwitchUsage.entrySet()) {
            displayString.append("\n    ")
            .append(swEntry.getKey()).append(" ")
            .append(((Switch)swEntry.getKey()).getType()).append(" reuse: ").append(swEntry.getValue()) ;
        }
        return displayString.toString();
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
