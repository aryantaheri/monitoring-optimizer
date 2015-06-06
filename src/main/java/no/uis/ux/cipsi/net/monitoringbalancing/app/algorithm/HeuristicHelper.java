package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class HeuristicHelper {
    private static Logger log = LoggerFactory.getLogger(HeuristicHelper.class);

    public static TrafficFlow initTrafficFlowMonitoringVariables(Graph<Node,WeightedLink> topology, Configs configs, TrafficFlow flow, List<MonitoringHost> monitoringHosts) {
        //        flow.setMonitoringHost(TopologyManager.getOversubscribedClosestMonitoringHost(topology, configs, flow, monitoringHosts));
        //        flow.setMonitoringHost(getPackedClosestMonitoringHost(topology, configs, flow, monitoringHosts));
        //        flow.setMonitoringSwitch(getSrcClosestMonitoringSwitch(topology, flow));
        List<Node> hostSwitch = getPackedClosestMonitoringHostSwitch(topology, configs, flow, monitoringHosts);
        flow.setMonitoringHost((MonitoringHost) hostSwitch.get(0));
        flow.setMonitoringSwitch((Switch) hostSwitch.get(1));

        return flow;
    }

    public static Switch getSrcClosestMonitoringSwitch(Graph<Node, WeightedLink> topology, TrafficFlow flow){
        List<Switch> onPathSwitches = flow.getOnPathMonitoringSwitches();
        Switch candidateSw = null;
        if (onPathSwitches != null && onPathSwitches.size() > 0) {
            candidateSw = onPathSwitches.get(0);
        }
        return candidateSw;
    }

    public static Switch getDstClosestMonitoringSwitch(Graph<Node, WeightedLink> topology, TrafficFlow flow){
        List<Switch> onPathSwitches = flow.getOnPathMonitoringSwitches();
        Switch candidateSw = null;
        if (onPathSwitches != null && onPathSwitches.size() > 0) {
            candidateSw = onPathSwitches.get(onPathSwitches.size() - 1);
        }
        return candidateSw;
    }

    public static MonitoringHost getClosestMonitoringHost(Graph<Node, WeightedLink> topology, TrafficFlow flow) {
        MonitoringHost monitoringHost = null;
        Switch candidateSw = getSrcClosestMonitoringSwitch(topology, flow);
        if (candidateSw != null && candidateSw.getType().equals(TYPE.EDGE)) {
            // CandidateSw is an Edge, and in each edge there should be a MH
            for (Node node : topology.getNeighbors(candidateSw)) {
                if (node instanceof MonitoringHost){
                    monitoringHost = (MonitoringHost) node;
                    break;
                }
            }
        }
        return monitoringHost;
    }

    public static MonitoringHost getOversubscribedClosestMonitoringHost(Graph<Node, WeightedLink> topology, Configs configs, TrafficFlow flow){
        List<MonitoringHost> monitoringHosts = null;
        monitoringHosts = TopologyManager.getMonitoringHosts(topology);
        return getOversubscribedClosestMonitoringHost(topology, configs, flow, monitoringHosts);
    }

    public static MonitoringHost getOversubscribedClosestMonitoringHost(Graph<Node, WeightedLink> topology, Configs configs, TrafficFlow flow, List<MonitoringHost> monitoringHosts){
        MonitoringHost monitoringHostCandidate = null;
        Switch candidateSw = getSrcClosestMonitoringSwitch(topology, flow);
        int k = Integer.parseInt(configs.getConfig(ConfigName.TOPOLOGY_KPORT));

        int srcId = NumericPathFinder.getIntValue(flow.getSrcNode().getId());
        int dstId = NumericPathFinder.getIntValue(flow.getDstNode().getId());

        int srcPodId = NumericPathFinder.getPodId(k, srcId);
        int dstPodId = NumericPathFinder.getPodId(k, dstId);

        for (MonitoringHost mh : monitoringHosts) {

            int monitoringHostPodId = NumericPathFinder.getPodId(k, NumericPathFinder.getIntValue(mh.getId()));
            //            if (monitoringHostPodId == srcPodId || monitoringHostPodId == dstPodId) {
            //                // This can take the first MH in the same pod as flow src/dst and oversubscribe it
            //                return mh;
            //            }
            if (monitoringHostPodId == srcPodId) {
                // This can take the first MH in the same pod as flow src and oversubscribe it
                return mh;
            }

        }

        if (monitoringHostCandidate == null){
            monitoringHostCandidate = getRandomMonitoringHost(monitoringHosts);
            log.error("MonitoringHostCandidate is null. Choosing a random one={}", monitoringHostCandidate);
        }
        return monitoringHostCandidate;
    }

    private static Map<MonitoringHost, Double> monitoringHostUsageMap = new HashMap<MonitoringHost, Double>();

    public static MonitoringHost getPackedClosestMonitoringHost(Graph<Node, WeightedLink> topology, Configs configs, TrafficFlow flow, List<MonitoringHost> monitoringHosts){
        MonitoringHost monitoringHostCandidate = null;
        Switch candidateSw = getSrcClosestMonitoringSwitch(topology, flow);
        int k = Integer.parseInt(configs.getConfig(ConfigName.TOPOLOGY_KPORT));

        int srcId = NumericPathFinder.getIntValue(flow.getSrcNode().getId());
        int dstId = NumericPathFinder.getIntValue(flow.getDstNode().getId());

        int srcPodId = NumericPathFinder.getPodId(k, srcId);
        int dstPodId = NumericPathFinder.getPodId(k, dstId);

        for (MonitoringHost mh : monitoringHosts) {

            int monitoringHostPodId = NumericPathFinder.getPodId(k, NumericPathFinder.getIntValue(mh.getId()));
            //            if (monitoringHostPodId == srcPodId || monitoringHostPodId == dstPodId) {
            //                // This can take the first MH in the same pod as flow src/dst and pack it
            //                return mh;
            //            }
            if (monitoringHostPodId == srcPodId || monitoringHostPodId == dstPodId) {
                Double usage = monitoringHostUsageMap.get(mh);
                if (usage == null){
                    usage = new Double(0);
                }
                Double newUsage = usage + flow.getRate();
                double monitoringHostLinkCapacity = getIngressLinkCapacity(topology, mh);
                if (newUsage < monitoringHostLinkCapacity){
                    // This can take the first MH in the same pod as flow src.dst and pack it
                    monitoringHostUsageMap.put(mh, newUsage);
                    return mh;
                }
            }

        }

        if (monitoringHostCandidate == null){
            monitoringHostCandidate = getRandomMonitoringHost(monitoringHosts);
            log.error("MonitoringHostCandidate is null. Choosing a random one={}", monitoringHostCandidate);
        }
        return monitoringHostCandidate;
    }

    public static List<Node> getPackedClosestMonitoringHostSwitch(Graph<Node, WeightedLink> topology, Configs configs, TrafficFlow flow, List<MonitoringHost> monitoringHosts){
        MonitoringHost monitoringHostCandidate = null;
        Switch candidateSw = null;
        int k = Integer.parseInt(configs.getConfig(ConfigName.TOPOLOGY_KPORT));

        int srcId = NumericPathFinder.getIntValue(flow.getSrcNode().getId());
        int dstId = NumericPathFinder.getIntValue(flow.getDstNode().getId());

        int srcPodId = NumericPathFinder.getPodId(k, srcId);
        int dstPodId = NumericPathFinder.getPodId(k, dstId);

        for (MonitoringHost mh : monitoringHosts) {

            int monitoringHostPodId = NumericPathFinder.getPodId(k, NumericPathFinder.getIntValue(mh.getId()));
            if (monitoringHostPodId == srcPodId || monitoringHostPodId == dstPodId) {
                Double usage = monitoringHostUsageMap.get(mh);
                if (usage == null){
                    usage = new Double(0);
                }
                Double newUsage = usage + flow.getRate();
                double monitoringHostLinkCapacity = getIngressLinkCapacity(topology, mh);
                if (newUsage < monitoringHostLinkCapacity){
                    // This can take the first MH in the same pod as flow src.dst and pack it
                    monitoringHostCandidate = mh;
                    monitoringHostUsageMap.put(mh, newUsage);
                    if (monitoringHostPodId == srcPodId){
                        candidateSw = getSrcClosestMonitoringSwitch(topology, flow);
                    } else if (monitoringHostPodId == dstPodId){
                        candidateSw = getDstClosestMonitoringSwitch(topology, flow);
                    }
                    return Arrays.asList(monitoringHostCandidate, candidateSw);
                }
            }

        }

        if (monitoringHostCandidate == null){
            monitoringHostCandidate = getRandomMonitoringHost(monitoringHosts);
            log.error("MonitoringHostCandidate is null. Choosing a random one={}", monitoringHostCandidate);
            candidateSw = getSrcClosestMonitoringSwitch(topology, flow);
        }
        return Arrays.asList(monitoringHostCandidate, candidateSw);
    }

    private static double getIngressLinkCapacity(Graph<Node, WeightedLink> topology, MonitoringHost mh){
        Collection<WeightedLink> inLinks = topology.getInEdges(mh);
        double capacity = 0;
        for (WeightedLink weightedLink : inLinks) {
            capacity += weightedLink.getSpeed();
        }
        return capacity;
    }

    public static MonitoringHost getRandomMonitoringHost(List<MonitoringHost> monitoringHosts) {
        if (monitoringHosts == null || monitoringHosts.size() == 0) return null;

        Random rnd = new Random();
        int index = rnd.nextInt(monitoringHosts.size());
        MonitoringHost monitoringHostCandidate = monitoringHosts.get(index);
        return monitoringHostCandidate;
    }
}
