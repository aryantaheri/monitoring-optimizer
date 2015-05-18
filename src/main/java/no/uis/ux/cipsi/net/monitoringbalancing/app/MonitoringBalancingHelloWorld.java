package no.uis.ux.cipsi.net.monitoringbalancing.app;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.persistence.MonitoringBalancingGenerator;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.apache.commons.collections15.map.HashedMap;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

public class MonitoringBalancingHelloWorld {

    public static void main(String[] args) {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(
                "monitoringbalancing/solver/monitoringBalancingSolverConfig.xml");
        Solver solver = solverFactory.buildSolver();

        // Load a problem
        boolean includeMonitoringHostAsTrafficEndpoint = false;
        Configs configs = Configs.getDefaultConfigs(4);
        configs.putConfig(ConfigName.TOPOLOGY_KPORT, "4");

        configs.putConfig(ConfigName.FLOW_INTER_POD_PROB, "0.01");
        configs.putConfig(ConfigName.FLOW_INTRA_POD_PROB, "0.01");
        configs.putConfig(ConfigName.FLOW_INTRA_EDGE_PROB, "0.1");

        MonitoringBalance unsolvedMonitoringBalance = new MonitoringBalancingGenerator().createMonitoringBalance(configs, includeMonitoringHostAsTrafficEndpoint);

        // Solve the problem
        solver.solve(unsolvedMonitoringBalance);
        MonitoringBalance solvedMonitoringBalance = (MonitoringBalance) solver.getBestSolution();

        // Display the result
        System.out.println("\nSolved monitoringBalance :\n"
                + toDisplayString(solvedMonitoringBalance));
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
            isOnPath = TopologyManager.isSwitchOnPath(monitoringBalance.getTopology(), path, monitoringSwitch);
            distance = TopologyManager.getRandomShortestPath(monitoringBalance.getTopology(),
                    monitoringBalance.getConfigs(),
                    flow.getMonitoringSwitch(),
                    flow.getMonitoringHost(), 4).size();


            if (isOnPath) onPathMonitoringSwitches++;
            distanceSum += distance;
            incrementMap(monitoringSwitchUsage, monitoringSwitch);
            incrementMap(monitoringHostUsage, monitoringHost);


            displayString.append(" ").append(flow).append(" -> ")
            .append(monitoringSwitch).append(" ")
            .append(monitoringHost).append(" ")
            .append(" SwitchOnPath: ").append(isOnPath)
            .append(" Distance: ~").append(distance)
            .append('\n');
        }

        displayString.append(" #MonitoringSwitches: ").append(monitoringSwitchUsage.size())
        .append("\n   MonitoringSwitchesLayer: ")
        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.CORE)).append("-Core ")
        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.CORE))
        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.AGGREGATION)).append("-Aggr ")
        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.AGGREGATION))
        .append("\n     ").append(getSwitchCountInLayer(monitoringSwitchUsage, TYPE.EDGE)).append("-Edge ")
        .append("reuse: ").append(getSwitchLayerUsage(monitoringSwitchUsage, TYPE.EDGE))
        .append("\n #OnPathMonitoringSwitches: ").append(onPathMonitoringSwitches)
        .append("\n #Flows: ").append(flows.size())
        .append("\n #MonitoringHosts: ").append(monitoringHostUsage.size())
        .append("\n Mean SwitchHost distance: ").append(distanceSum/flows.size())

        ;

        displayString.append("\n Detailed Switch Stat: ");
        for (Entry<Node, Integer> swEntry : monitoringSwitchUsage.entrySet()) {
            displayString.append("\n    ")
            .append(swEntry.getKey()).append(" ")
            .append(((Switch)swEntry.getKey()).getType()).append(" reuse: ").append(swEntry.getValue()) ;
        }
        return displayString.toString();
    }

    private static int getSwitchCountInLayer(Map<Node, Integer> map, TYPE type) {
        int layerSw = 0;
        for (Entry<Node, Integer> swEntry : map.entrySet()) {
            if (((Switch) swEntry.getKey()).getType() == type){
                layerSw++;
            }
        }
        return layerSw;
    }

    private static int getSwitchLayerUsage(Map<Node, Integer> map, TYPE type) {
        int layerReuse = 0;
        for (Entry<Node, Integer> swEntry : map.entrySet()) {
            if (((Switch) swEntry.getKey()).getType() == type){
                layerReuse += swEntry.getValue();
            }
        }
        return layerReuse;
    }

    private static Map<Node, Integer> incrementMap(Map<Node, Integer> map, Node node) {
        Integer usage = map.get(node);
        if(usage == null){
            usage = new Integer(0);
        }
        usage++;
        map.put(node, usage);
        return map;
    }
}
