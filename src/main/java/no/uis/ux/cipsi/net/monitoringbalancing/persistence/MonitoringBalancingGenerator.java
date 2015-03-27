package no.uis.ux.cipsi.net.monitoringbalancing.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class MonitoringBalancingGenerator {
    private static Logger logger = LoggerFactory.getLogger(MonitoringBalancingGenerator.class);

    private static double DEFAULT_FLOW_RATE = Math.pow(10, 8);//100Mbps 10^(2+6)
    public static void main(String[] args) {
        //        boolean includeMonitoringHostAsTrafficEndpoint = false;
        //        int kPort = 4;
        //        new MonitoringBalancingGenerator().createMonitoringBalance(kPort, includeMonitoringHostAsTrafficEndpoint);
    }

    public MonitoringBalance createMonitoringBalance(Configs configs, boolean includeMonitoringHostAsTrafficEndpoint) {
        Graph<Node, WeightedLink> topology = TopologyManager.buildTopology(configs);
        Yen<Node, WeightedLink> algo = TopologyManager.buildShortestPathAlgo(topology);

        List<Switch> monitoringSwitches = TopologyManager.getMonitoringSwitches(topology);
        List<MonitoringHost> monitoringHosts = TopologyManager.getMonitoringHosts(topology);
        List<TrafficFlow> trafficFlows = generateTrafficFlows(topology, algo, configs, includeMonitoringHostAsTrafficEndpoint);
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
        for (Host srcHost : trafficEndpointHosts) {
            for (Host dstHost : trafficEndpointHosts) {
                if (srcHost.equals(dstHost)) continue;
                TrafficFlow flow = generateTrafficFlow(topology, algo, srcHost, dstHost, rate);
                flows.add(flow);
            }
        }
        return flows;
    }

    private TrafficFlow generateTrafficFlow(Graph<Node,WeightedLink> topology, Yen<Node,WeightedLink> algo, Host srcHost, Host dstHost, double rate) {
        int shortestPathsNum = getShortestPathsLimit(srcHost, dstHost);

        List<WeightedLink> path = TopologyManager.getRandomShortestPath(algo, srcHost, dstHost, shortestPathsNum);
        List<Switch> onPathMonitoringSwitches = TopologyManager.getSwitchesOnPath(topology, path);
        TrafficFlow flow = new TrafficFlow(srcHost, dstHost,
                getHostAddress(srcHost),
                getHostAddress(dstHost),
                (short) 80, (short) 80,
                (short) 22, rate, path, onPathMonitoringSwitches);
        return flow;
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

    public static void setDefaultFlowRate(double rate) {
        DEFAULT_FLOW_RATE = rate;
    }
}
