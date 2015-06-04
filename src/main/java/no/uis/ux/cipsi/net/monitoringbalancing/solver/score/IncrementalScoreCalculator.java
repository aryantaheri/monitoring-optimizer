package no.uis.ux.cipsi.net.monitoringbalancing.solver.score;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class IncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<MonitoringBalance> {

    private static Logger log = LoggerFactory.getLogger(IncrementalScoreCalculator.class);
    private static final int NULL_MON_SW_SCORE = 10000;
    private static final int NULL_MON_HOST_SCORE = 20000;

    private Map<WeightedLink, Double> linkUsageMap;
    private Map<Switch, Double> switchFabricUsageMap;
    private Map<Switch, Double> switchForwardingUsageMap;
    private Map<MonitoringHost, Double> hostUsageMap;

    private Map<Switch, Double> monitoringSwitchCostMap;
    private Map<MonitoringHost, Double> monitoringHostCostMap;
    private Map<WeightedLink, Double> monitoringLinkCostMap;

    Graph<Node, WeightedLink> topology;
    Configs configs;

    double hardScore = 0;
    double softScore = 0;

    @Override
    public void resetWorkingSolution(MonitoringBalance workingSolution) {
        linkUsageMap = new HashMap<WeightedLink, Double>();
        switchFabricUsageMap = new HashMap<Switch, Double>();
        switchForwardingUsageMap = new HashMap<Switch, Double>();
        hostUsageMap = new HashMap<MonitoringHost, Double>();

        monitoringSwitchCostMap = new HashMap<Switch, Double>();
        monitoringHostCostMap = new HashMap<MonitoringHost, Double>();
        monitoringLinkCostMap = new HashMap<WeightedLink, Double>();

        configs = workingSolution.getConfigs();
        topology = workingSolution.getTopology();

        hardScore = 0;
        softScore = 0;

        List<TrafficFlow> trafficFlows = workingSolution.getTrafficFlows();
        for (TrafficFlow trafficFlow : trafficFlows) {
            insert(trafficFlow);
        }
    }

    @Override
    public void beforeEntityAdded(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEntityAdded(Object entity) {
        insert((TrafficFlow) entity);
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        // TODO: include variableName and limit the change domain
        //        log.debug("beforeVariableChanged e={}, variableName={}", entity, variableName);
        retract((TrafficFlow) entity);
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        // TODO: include variableName and limit the change domain
        //        log.debug("afterVariableChanged e={}, variableName={}", entity, variableName);
        insert((TrafficFlow) entity);
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        retract((TrafficFlow) entity);

    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public HardSoftBigDecimalScore calculateScore() {
        return HardSoftBigDecimalScore.valueOf(BigDecimal.valueOf(hardScore), BigDecimal.valueOf(softScore));
    }


    private void insert(TrafficFlow trafficFlow) {

        MonitoringHost monHost = trafficFlow.getMonitoringHost();
        Switch monSwitch = trafficFlow.getMonitoringSwitch();

        if (monHost == null || monSwitch == null) {
            // TODO: is this right? or they need individual cases
            if (monHost == null){
                hardScore -= NULL_MON_HOST_SCORE;
            }
            if (monSwitch == null){
                hardScore -= NULL_MON_SW_SCORE;
            }
            //            System.out.println(monHost);
            //            System.out.println(monSwitch);
            //            System.exit(-1);
            return;
        }

        //        if (!trafficFlow.getOnPathMonitoringSwitches().contains(monSwitch)){
        //            System.out.println("WTF");
        //            System.exit(-2);
        //        }
        calculateTrafficFlowResourceUsage(trafficFlow, true);
        calculateMonitoringResourceUsage(topology, configs, trafficFlow, true);
    }

    private void retract(TrafficFlow trafficFlow) {
        MonitoringHost monHost = trafficFlow.getMonitoringHost();
        Switch monSwitch = trafficFlow.getMonitoringSwitch();

        if (monHost == null || monSwitch == null) {
            // TODO: is this right? or they need individual cases
            if (monHost == null){
                hardScore += NULL_MON_HOST_SCORE;
            }
            if (monSwitch == null){
                hardScore += NULL_MON_SW_SCORE;
            }
            return;
        }

        calculateTrafficFlowResourceUsage(trafficFlow, false);
        calculateMonitoringResourceUsage(topology, configs, trafficFlow, false);
    }

    private void calculateTrafficFlowResourceUsage(TrafficFlow trafficFlow, boolean added) {
        List<WeightedLink> path = trafficFlow.getPath();
        calculateTrafficFlowLinkUsage(trafficFlow, path, 0, added);
        calculateTrafficFlowSwitchUsage(trafficFlow, path, added);
        calculateTrafficFlowHostUsage(trafficFlow, path, added);
    }

    private void calculateTrafficFlowLinkUsage(TrafficFlow trafficFlow, List<WeightedLink> path, float overhead, boolean added) {
        for (WeightedLink weightedLink : path) {
            Double oldUsage = linkUsageMap.get(weightedLink);
            if (oldUsage == null){
                oldUsage = new Double(0);
            }
            Double newUsage = new Double(oldUsage);
            if (added) {
                newUsage += trafficFlow.getRate() + trafficFlow.getRate()*overhead;
            } else {
                newUsage -= trafficFlow.getRate() + trafficFlow.getRate()*overhead;
            }
            double oldAvailableBandwidth = weightedLink.getSpeed() - weightedLink.getUsage() - oldUsage;
            double newAvailableBandwidth = weightedLink.getSpeed() - weightedLink.getUsage() - newUsage;

            // Calculate the hard score for links in Mbps
            hardScore += (Math.min(newAvailableBandwidth, 0) - Math.min(oldAvailableBandwidth, 0)) / Math.pow(10, 6);
            linkUsageMap.put(weightedLink, newUsage);
        }

    }

    private void calculateTrafficFlowHostUsage(TrafficFlow trafficFlow, List<WeightedLink> path, boolean added) {
        // TODO Auto-generated method stub
    }

    private void calculateTrafficFlowSwitchUsage(TrafficFlow trafficFlow, List<WeightedLink> path, boolean added) {
        // TODO Auto-generated method stub
    }

    /**
     * Resource Usage for monitoring traffic MonitoringSwitch -> MonitoringHost,
     * which should account for the tunneling overhead as well.
     * @param trafficFlow
     */
    private void calculateMonitoringResourceUsage(Graph<Node,WeightedLink> topology, Configs configs, TrafficFlow trafficFlow, boolean added) {
        if (trafficFlow.getMonitoringHost() == null || trafficFlow.getMonitoringSwitch() == null) {
            //            log.debug("calculateMonitoringResourceUsage trafficFlow={} missing monitoringSwitch={} or monitoringHost={}", trafficFlow, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
            return;
        }
        //        log.debug("calculateMonitoringResourceUsage trafficFlow={} missing monitoringSwitch={} or monitoringHost={}", trafficFlow, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
        // FIXME: This is wrong, inserting a flow with the path P1, and retracting the flow with the path P2
        // List<WeightedLink> path = TopologyManager.getRandomShortestPath(topology, configs, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost(), 4);
        // This should solve the problem. The ASSERT function of opta-planner can be used to verify.
        List<WeightedLink> path = TopologyManager.getDeterministicShortestPath(topology, configs, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost(), 4, trafficFlow);
        trafficFlow.setMonitoringSwitchHostPath(path);

        calculateTrafficFlowLinkUsage(trafficFlow, path, WeightedLink.DEFAULT_TUNNELLING_BANDWIDTH_OVERHEAD, added);
        calculateTrafficFlowSwitchUsage(trafficFlow, path, added);
        calculateTrafficFlowHostUsage(trafficFlow, path, added);

        calculateMonitoringServiceSwitchCost(trafficFlow, added);
        calculateMonitoringServiceHostCost(trafficFlow, added);
        calculateMonitoringServiceLinkCost(trafficFlow, path, added);
    }


    private void calculateMonitoringServiceSwitchCost(TrafficFlow trafficFlow, boolean added) {
        Switch monitoringSwitch = trafficFlow.getMonitoringSwitch();
        Double oldUsage = monitoringSwitchCostMap.get(monitoringSwitch);
        if (oldUsage == null){
            oldUsage = new Double(0);
        }
        Double newUsage = new Double(oldUsage);

        if (added){
            if (newUsage == 0){
                newUsage = new Double(monitoringSwitch.getInitCost());
            }
            newUsage += monitoringSwitch.getPerFlowReuseCost();

        } else {
            newUsage -= monitoringSwitch.getPerFlowReuseCost();
            if (newUsage == monitoringSwitch.getInitCost()){
                newUsage = new Double(0);
            }
        }
        softScore -= (newUsage - oldUsage);
        monitoringSwitchCostMap.put(monitoringSwitch, newUsage);
    }

    private void calculateMonitoringServiceHostCost(TrafficFlow trafficFlow, boolean added) {
        MonitoringHost host = trafficFlow.getMonitoringHost();
        Double oldUsage = monitoringHostCostMap.get(host);
        if (oldUsage == null){
            oldUsage = new Double(0);
        }
        Double newUsage = new Double(oldUsage);

        if (added) {
            if (oldUsage == 0) {
                newUsage = new Double(host.getCost());
            }
            // Add a small value to keep track of the host reuse
            newUsage += 1;

        } else {
            // Oops, how to understand if this is the last usage of this host???
            // Use the +1 per reuse dummy cost to do so
            newUsage -= 1;
            if (newUsage == host.getCost()) {
                newUsage = new Double(0);
            }
        }
        softScore -= (newUsage - oldUsage);
        monitoringHostCostMap.put(host, newUsage);
    }


    private void calculateMonitoringServiceLinkCost(TrafficFlow trafficFlow, List<WeightedLink> path, boolean added) {
        for (WeightedLink weightedLink : path) {
            Double oldCost = monitoringLinkCostMap.get(weightedLink);
            if (oldCost == null){
                oldCost = new Double(0);
            }
            Double newCost = new Double(oldCost);

            if (added) {
                newCost += weightedLink.getMonitorServiceCost(trafficFlow.getRate());
            } else {
                newCost -= weightedLink.getMonitorServiceCost(trafficFlow.getRate());
            }

            softScore -= (newCost - oldCost);
            monitoringLinkCostMap.put(weightedLink, newCost);
        }
    }




}
