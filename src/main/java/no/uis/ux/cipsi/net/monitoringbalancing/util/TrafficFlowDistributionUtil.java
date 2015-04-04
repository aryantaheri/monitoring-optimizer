package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

public class TrafficFlowDistributionUtil {

    // Probabilities string -> Map<hostId, List<hostId>>
    private static Map<String, Map<String, List<String>>> distributions = new TreeMap<String, Map<String,List<String>>>();

    private static Map<String, List<String>> getDistribution(Configs cnf){
        String distString = getDistributionString(cnf);
        return distributions.get(distString);
    }

    private static String getDistributionString(Configs cnf) {
        String distString = cnf.getConfig(ConfigName.TOPOLOGY_KPORT) + "-" +
                cnf.getConfig(ConfigName.FLOW_INTER_POD_PROB) + "-" +
                cnf.getConfig(ConfigName.FLOW_INTRA_POD_PROB) + "-" +
                cnf.getConfig(ConfigName.FLOW_INTRA_EDGE_PROB);
        return distString;
    }

    public static boolean distributionExists(Configs cnf){
        if (getDistribution(cnf) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean flowExists(Host src, Host dst, Configs cnf){
        Map<String, List<String>> srcDstMap = getDistribution(cnf);
        if (srcDstMap == null){
            // Not initialized for this cnf
            return null;
        } else if (srcDstMap.get(src.getId()) == null) {
            return Boolean.FALSE;
        } else if (srcDstMap.get(src.getId()).contains(dst.getId())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static void addFlows(Configs cnf, List<TrafficFlow> flows){
        String distString = getDistributionString(cnf);
        Map<String, List<String>> srcDstMap = distributions.get(distString);

        if (srcDstMap == null) {
            srcDstMap = new TreeMap<String, List<String>>();
        }
        for (TrafficFlow flow : flows) {
            List<String> dsts = srcDstMap.get(flow.getSrcNode().getId());
            if (dsts == null) {
                dsts = new ArrayList<String>();
            }
            dsts.add(flow.getDstNode().getId());
            srcDstMap.put(flow.getSrcNode().getId(), dsts);
        }
        distributions.put(distString, srcDstMap);
    }
}

