package no.uis.ux.cipsi.net.monitoringbalancing.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;
import no.uis.ux.cipsi.net.monitoringbalancing.util.TrafficFlowDistributionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class MonitoringBalancingGenerator {
    private static Logger logger = LoggerFactory.getLogger(MonitoringBalancingGenerator.class);

    private static TreeMap<Host, List<Host>> trafficFlowDistribution = new TreeMap<Host, List<Host>>();

    public static void main(String[] args) {
        boolean includeMonitoringHostAsTrafficEndpoint = false;
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.FLOW_INTER_POD_PROB, "0.001");
        cnf.putConfig(ConfigName.FLOW_INTRA_POD_PROB, "0.01");
        cnf.putConfig(ConfigName.FLOW_INTRA_EDGE_PROB, "0.1");
        String[] ks = {"4","8","16","24", "32", "48", "64"};
        MonitoringBalancingGenerator generator = new MonitoringBalancingGenerator();
        for (String k : ks) {
            cnf.putConfig(ConfigName.TOPOLOGY_KPORT, k);
            estimateTrafficFlows(cnf, includeMonitoringHostAsTrafficEndpoint);
            System.out.println("--------------------------");
        }
        //        MonitoringBalance balance = generator.createMonitoringBalance(cnf, includeMonitoringHostAsTrafficEndpoint);
        //        Node n1 = topo.getVertices().toArray(new Node[topo.getVertices().size()])[0];
        //        Node n2 = topo.getVertices().toArray(new Node[topo.getVertices().size()])[1];
        //        logger.debug("n1={}", n1);
        //        logger.debug("n2={}", n2);
        //        List<List<WeightedLink>> paths = TopologyManager.getKShortestPaths(balance.getAlgo(), n1, n2, 4);
        //        logger.debug("paths={}", paths);
        //        List<List<WeightedLink>> paths2 = TopologyManager.getKShortestPaths(balance.getAlgo(), n1, n2, 4);
        //        logger.debug("paths={}", paths2);
    }

    private static void estimateTrafficFlows(Configs configs, boolean includeMonitoringHostAsTrafficEndpoint) {
        Graph<Node, WeightedLink> topology = TopologyManager.buildTopology(configs);
        logger.debug("createMonitoringBalance: topology #vertices={} #edges={}", topology.getVertexCount(), topology.getEdgeCount());
        Yen<Node, WeightedLink> algo = TopologyManager.buildShortestPathAlgo(topology);

        List<Switch> monitoringSwitches = TopologyManager.getMonitoringSwitches(topology);
        List<MonitoringHost> monitoringHosts = TopologyManager.getMonitoringHosts(topology);
        List<Host> hosts = TopologyManager.getHosts(topology, includeMonitoringHostAsTrafficEndpoint);
        //        logger.debug("monitoringSwitches[{}] {}", monitoringSwitches.size(), monitoringSwitches);
        //        logger.debug("monitoringHosts[{}] {}", monitoringHosts.size(), monitoringHosts);
        //        logger.debug("Hosts[{}] {}", hosts.size(), hosts);

        double rate = Double.valueOf(configs.getConfig(ConfigName.FLOW_RATE));
        List<Host> trafficEndpointHosts = TopologyManager.getHosts(topology, includeMonitoringHostAsTrafficEndpoint);
        int tempId = 0;
        int should = 0;
        int shouldNot = 0;
        for (Host srcHost : trafficEndpointHosts) {
            for (Host dstHost : trafficEndpointHosts) {
                if (srcHost.equals(dstHost)) continue;
                if (!shouldGenerate(srcHost, dstHost, configs)) {
                    shouldNot++;
                    continue;
                }
                should++;
                tempId++;
                //                logger.debug("estimateTrafficFlows: generated={} notGenerated={} id={} src={} dst={}", should, shouldNot, tempId, srcHost, dstHost);
            }
        }
        logger.debug("estimateTrafficFlows: configs={} ", configs);
        logger.debug("host={} monHost={} sw={} generated-flows={} not-generated-flows={}", hosts.size(), monitoringHosts.size(), monitoringSwitches.size(), should, shouldNot);
    }

    public MonitoringBalance createMonitoringBalance(Configs configs, boolean includeMonitoringHostAsTrafficEndpoint) {
        logger.info("Configuration: {}", configs);
        Graph<Node, WeightedLink> topology = TopologyManager.buildTopology(configs);
        logger.debug("createMonitoringBalance: topology #vertices={} #edges={}", topology.getVertexCount(), topology.getEdgeCount());
        Yen<Node, WeightedLink> algo = TopologyManager.buildShortestPathAlgo(topology);

        List<Switch> monitoringSwitches = TopologyManager.getMonitoringSwitches(topology);
        List<MonitoringHost> monitoringHosts = TopologyManager.getMonitoringHosts(topology);
        List<TrafficFlow> trafficFlows = generateTrafficFlows(topology, algo, configs, includeMonitoringHostAsTrafficEndpoint);
        if (!TrafficFlowDistributionUtil.distributionExists(configs)){
            TrafficFlowDistributionUtil.addFlows(configs, trafficFlows);
        }
        logger.debug("monitoringSwitches[{}] {}", monitoringSwitches.size(), monitoringSwitches);
        logger.debug("monitoringHosts[{}] {}", monitoringHosts.size(), monitoringHosts);
        logger.debug("flows[{}]", trafficFlows.size());
        for (TrafficFlow trafficFlow : trafficFlows) {
            logger.trace("flow {}", trafficFlow);
        }
        List<Host> hosts = TopologyManager.getHosts(topology, includeMonitoringHostAsTrafficEndpoint);
        logger.info("Hosts[{}] {}", hosts.size(), hosts);

        MonitoringBalance monitoringBalance = new MonitoringBalance(topology, algo, monitoringSwitches, monitoringHosts, trafficFlows);

        return monitoringBalance;
    }

    private List<TrafficFlow> generateTrafficFlows(Graph<Node,WeightedLink> topology, Yen<Node,WeightedLink> algo, Configs configs, boolean includeMonitoringHostAsTrafficEndpoint) {
        List<TrafficFlow> flows = new ArrayList<TrafficFlow>();
        double rate = Double.valueOf(configs.getConfig(ConfigName.FLOW_RATE));
        List<Host> trafficEndpointHosts = TopologyManager.getHosts(topology, includeMonitoringHostAsTrafficEndpoint);
        int tempId = 0;
        int should = 0;
        int shouldNot = 0;
        for (Host srcHost : trafficEndpointHosts) {
            for (Host dstHost : trafficEndpointHosts) {
                if (srcHost.equals(dstHost)) continue;
                if (!shouldGenerate(srcHost, dstHost, configs)) {
                    shouldNot++;
                    continue;
                }
                should++;
                TrafficFlow flow = generateTrafficFlow(topology, algo, srcHost, dstHost, rate);
                tempId++;
                logger.debug("generateTrafficFlows: generated={} notGenerated={} id={} flow={}", should, shouldNot, tempId, flow);
                flows.add(flow);
            }
        }
        logger.debug("generateTrafficFlows: generated-flows={} not-generated-flows={}", should, shouldNot);
        return flows;
    }

    private TrafficFlow generateTrafficFlow(Graph<Node,WeightedLink> topology, Yen<Node,WeightedLink> algo, Host srcHost, Host dstHost, double rate) {
        // FIXME This is not good, but #=4 makes it too slow for k=48.
        // Store paths and lookup them.
        //        int shortestPathsNum = 1;
        int shortestPathsNum = getShortestPathsLimit(srcHost, dstHost);
        List<WeightedLink> path = TopologyManager.getRandomShortestPath(algo, srcHost, dstHost, shortestPathsNum);
        //        List<WeightedLink> path = TopologyManager.getShortestPath(algo, srcHost, dstHost);
        List<Switch> onPathMonitoringSwitches = TopologyManager.getSwitchesOnPath(topology, path);
        TrafficFlow flow = new TrafficFlow(srcHost, dstHost,
                getHostAddress(srcHost),
                getHostAddress(dstHost),
                (short) 80, (short) 80,
                (short) 22, rate, path, onPathMonitoringSwitches);
        return flow;
    }

    private static Random random = new Random();
    private static boolean shouldGenerate(Host srcHost, Host dstHost, Configs configs) {
        Boolean exist = TrafficFlowDistributionUtil.flowExists(srcHost, dstHost, configs);
        if (exist != null) return exist;

        boolean generate = false;
        double p = random.nextDouble();
        if (srcHost.getPodIndex() == dstHost.getPodIndex()) {

            if (srcHost.getEdgeIndex() == dstHost.getEdgeIndex()) {
                // same edge
                if (p < Double.valueOf(configs.getConfig(ConfigName.FLOW_INTRA_EDGE_PROB))){
                    generate = true;
                }
            } else {
                // same pod different edge
                if (p < Double.valueOf(configs.getConfig(ConfigName.FLOW_INTRA_POD_PROB))){
                    generate = true;
                }
            }
        } else {
            // different pods
            if (p < Double.valueOf(configs.getConfig(ConfigName.FLOW_INTER_POD_PROB))){
                generate = true;
            }
        }
        return generate;
    }

    private int getShortestPathsLimit(Host src, Host dst) {
        if (src.getPodIndex() == dst.getPodIndex()) {
            if (src.getEdgeIndex() == dst.getEdgeIndex()){
                // same edge
                return 1;
            } else {
                //same pod, different edges
                return 2;
            }
        } else {
            // different pods
            return 4;
        }
    }

    private InetAddress getHostAddress(Host host) {
        try {
            int higher = host.getHostIndex() / 256;
            int lower = host.getHostIndex() % 256;
            return InetAddress.getByName(host.getPodIndex()+"."+host.getEdgeIndex()+"."+higher+ "."+lower);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

}
