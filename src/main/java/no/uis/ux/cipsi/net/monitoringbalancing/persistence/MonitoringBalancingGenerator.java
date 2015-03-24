package no.uis.ux.cipsi.net.monitoringbalancing.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringBalancingGenerator {
    private static Logger logger = LoggerFactory.getLogger(MonitoringBalancingGenerator.class);

    private static double DEFAULT_FLOW_RATE = Math.pow(10, 8);//100Mbps 10^(2+6)
    public static void main(String[] args) {
        boolean includeMonitoringHostAsTrafficEndpoint = false;
        new MonitoringBalancingGenerator().createMonitoringBalance(includeMonitoringHostAsTrafficEndpoint);
        List<Host> hosts = TopologyManager.getInstance().getHosts(includeMonitoringHostAsTrafficEndpoint);
        logger.info("Hosts[{}] {}", hosts.size(), hosts);
    }

    public MonitoringBalance createMonitoringBalance(boolean includeMonitoringHostAsTrafficEndpoint) {
        List<Switch> monitoringSwitches = generateMonitoringSwitches();
        List<MonitoringHost> monitoringHosts = generateMonitoringHosts();
        List<TrafficFlow> trafficFlows = generateTrafficFlows(includeMonitoringHostAsTrafficEndpoint);
        logger.debug("monitoringSwitches[{}] {}", monitoringSwitches.size(), monitoringSwitches);
        logger.debug("monitoringHosts[{}] {}", monitoringHosts.size(), monitoringHosts);
        logger.debug("flows[{}]", trafficFlows.size());
        for (TrafficFlow trafficFlow : trafficFlows) {
            logger.trace("flow {}", trafficFlow);
        }
        MonitoringBalance monitoringBalance = new MonitoringBalance(monitoringSwitches, monitoringHosts, trafficFlows);

        return monitoringBalance;
    }


    private List<Switch> generateMonitoringSwitches() {
        List<Switch> switches = TopologyManager.getInstance().getMonitoringSwitches();
        return switches;
    }

    private List<MonitoringHost> generateMonitoringHosts() {
        List<MonitoringHost> hosts = TopologyManager.getInstance().getMonitoringHosts();
        return hosts;
    }

    private List<TrafficFlow> generateTrafficFlows(boolean includeMonitoringHostAsTrafficEndpoint) {
        List<TrafficFlow> flows = new ArrayList<TrafficFlow>();
        List<Host> trafficEndpointHosts = TopologyManager.getInstance().getHosts(includeMonitoringHostAsTrafficEndpoint);
        for (Host srcHost : trafficEndpointHosts) {
            for (Host dstHost : trafficEndpointHosts) {
                if (srcHost.equals(dstHost)) continue;
                double rate = DEFAULT_FLOW_RATE;
                TrafficFlow flow = generateTrafficFlow(srcHost, dstHost, rate);
                flows.add(flow);
            }
        }
        return flows;
    }

    private TrafficFlow generateTrafficFlow(Host srcHost, Host dstHost, double rate) {
        int shortestPathsNum = getShortestPathsLimit(srcHost, dstHost);
        List<WeightedLink> path = TopologyManager.getInstance().getRandomShortestPath(srcHost, dstHost, shortestPathsNum);
        TrafficFlow flow = new TrafficFlow(srcHost, dstHost,
                getHostAddress(srcHost),
                getHostAddress(dstHost),
                (short) 80, (short) 80,
                (short) 22, rate, path);
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
            return InetAddress.getByName("1."+host.getPodIndex()+"."+host.getEdgeIndex()+"."+host.getHostIndex());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setDefaultFlowRate(double rate) {
        DEFAULT_FLOW_RATE = rate;
    }
}
