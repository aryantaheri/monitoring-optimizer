package no.uis.ux.cipsi.net.monitoringbalancing.solver.score;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringBalancingEasyScoreCalculator implements EasyScoreCalculator<MonitoringBalance> {
    private static Logger log = LoggerFactory.getLogger(MonitoringBalancingEasyScoreCalculator.class);
    private static final float TUNNELLING_OVERHEAD = 0.2f;
    private Map<WeightedLink, Double> linkUsageMap;
    private Map<Switch, Double> switchFabricUsageMap;
    private Map<Switch, Double> switchForwardingUsageMap;
    private Map<MonitoringHost, Double> hostUsageMap;

    private Map<Switch, Double> monitoringSwitchCostMap;
    private Map<MonitoringHost, Double> monitoringHostCostMap;
    private Map<WeightedLink, Double> monitoringLinkCostMap;

    @Override
    public HardSoftBigDecimalScore calculateScore(MonitoringBalance monitoringBalance) {
        linkUsageMap = new HashMap<WeightedLink, Double>();
        switchFabricUsageMap = new HashMap<Switch, Double>();
        switchForwardingUsageMap = new HashMap<Switch, Double>();
        hostUsageMap = new HashMap<MonitoringHost, Double>();

        monitoringSwitchCostMap = new HashMap<Switch, Double>();
        monitoringHostCostMap = new HashMap<MonitoringHost, Double>();
        monitoringLinkCostMap = new HashMap<WeightedLink, Double>();

        List<TrafficFlow> trafficFlows = monitoringBalance.getTrafficFlows();
        calculateTrafficResourceUsage(trafficFlows);
        calculateMonitoringResourceUsage(trafficFlows);

        double hardScore = sumHardScore();
        double softScore = sumSoftScore();
        log.trace("calculateScore monitoringSwitchCostMap={}", monitoringSwitchCostMap);
        log.trace("calculateScore monitoringHostCostMap={}", monitoringHostCostMap);
        log.trace("calculateScore monitoringLinkCostMap={}", monitoringLinkCostMap);
        log.trace("calculateScore hardScore={} softScore={}", hardScore, softScore);
        return HardSoftBigDecimalScore.valueOf(BigDecimal.valueOf(hardScore), BigDecimal.valueOf(softScore));
    }

    private double sumHardScore() {
        double hardScore = 0;
        hardScore += sumLinkHardScore();
        hardScore += sumSwitchHardScore();
        hardScore += sumHostHardScore();
        return hardScore;
    }

    private double sumHostHardScore() {
        // TODO Auto-generated method stub
        return 0;
    }

    private double sumSwitchHardScore() {
        // TODO Auto-generated method stub
        return 0;
    }

    private double sumLinkHardScore() {
        double hardScore = 0;
        for (Entry<WeightedLink, Double> linkEntry : linkUsageMap.entrySet()) {
            if (linkEntry.getValue() > linkEntry.getKey().getSpeed()){
                hardScore +=  (linkEntry.getKey().getSpeed() - linkEntry.getValue());
            }
        }
        return hardScore;
    }

    private double sumSoftScore() {
        double softScore = 0;
        for (Entry<Switch, Double> switchEntry: monitoringSwitchCostMap.entrySet()) {
            softScore -= switchEntry.getValue();
        }
        for (Entry<MonitoringHost, Double> hostEntry: monitoringHostCostMap.entrySet()) {
            softScore -= hostEntry.getValue();
        }
        for (Entry<WeightedLink, Double> linkEntry: monitoringLinkCostMap.entrySet()) {
            softScore -= linkEntry.getValue();
        }
        return softScore;
    }


    private void calculateMonitoringResourceUsage(List<TrafficFlow> trafficFlows) {
        for (TrafficFlow trafficFlow : trafficFlows) {
            calculateMonitoringResourceUsage(trafficFlow);
        }
    }


    /**
     * Resource Usage for monitoring traffic MonitoringSwitch -> MonitoringHost,
     * which should account for the tunneling overhead as well.
     * @param trafficFlow
     */
    private void calculateMonitoringResourceUsage(TrafficFlow trafficFlow) {
        if (trafficFlow.getMonitoringHost() == null || trafficFlow.getMonitoringSwitch() == null) {
            //            log.debug("calculateMonitoringResourceUsage trafficFlow={} missing monitoringSwitch={} or monitoringHost={}", trafficFlow, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
            return;
        }
        //        log.debug("calculateMonitoringResourceUsage trafficFlow={} missing monitoringSwitch={} or monitoringHost={}", trafficFlow, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
        List<WeightedLink> path = TopologyManager.getInstance().getRandomShortestPath(trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost(), 4);
        calculateTrafficFlowLinkUsage(trafficFlow, path, TUNNELLING_OVERHEAD);
        calculateTrafficFlowSwitchUsage(trafficFlow, path);
        calculateTrafficFlowHostUsage(trafficFlow, path);

        calculateMonitoringServiceSwitchCost(trafficFlow);
        calculateMonitoringServiceHostCost(trafficFlow);
        calculateMonitoringServiceLinkCost(trafficFlow, path);
    }

    private void calculateMonitoringServiceSwitchCost(TrafficFlow trafficFlow) {
        Switch monitoringSwitch = trafficFlow.getMonitoringSwitch();
        Double usage = monitoringSwitchCostMap.get(monitoringSwitch);
        if (usage == null){
            usage = new Double(monitoringSwitch.getInitCost());
        }
        usage += monitoringSwitch.getPerFlowReuseCost();
        if (!TopologyManager.getInstance().isSwitchOnPath(trafficFlow.getPath(), monitoringSwitch)){
            usage += 10000;
        }
        monitoringSwitchCostMap.put(monitoringSwitch, usage);
    }

    private void calculateMonitoringServiceHostCost(TrafficFlow trafficFlow) {
        MonitoringHost host = trafficFlow.getMonitoringHost();
        Double usage = monitoringHostCostMap.get(host);
        if (usage == null){
            usage = new Double(host.getCost());
            monitoringHostCostMap.put(host, usage);
        }
        // No extra costs per flow until the hard limit of the network interface/link capacity is saturated.
    }


    private void calculateMonitoringServiceLinkCost(TrafficFlow trafficFlow, List<WeightedLink> path) {
        for (WeightedLink weightedLink : path) {
            Double cost = monitoringLinkCostMap.get(weightedLink);
            if (cost == null){
                cost = new Double(0);
            }
            cost += weightedLink.getMonitorServiceCost(trafficFlow.getRate());
            monitoringLinkCostMap.put(weightedLink, cost);
        }
    }
    /**
     * End-to-end traffic consume some resources on the switches, links, and hosts.
     * Calculate regular connectivity resource consumption.
     */
    private void calculateTrafficResourceUsage(List<TrafficFlow> trafficFlows) {
        for (TrafficFlow trafficFlow : trafficFlows) {
            calculateTrafficFlowResourceUsage(trafficFlow);
        }
    }

    /**
     * K=4
     * inter-pod paths: 4
     * intra-pod paths: 2
     * Resource usage for traffic SRC->DST
     * @param trafficFlow
     */
    private void calculateTrafficFlowResourceUsage(TrafficFlow trafficFlow) {
        List<WeightedLink> path = trafficFlow.getPath();
        calculateTrafficFlowLinkUsage(trafficFlow, path, 0);
        calculateTrafficFlowSwitchUsage(trafficFlow, path);
        calculateTrafficFlowHostUsage(trafficFlow, path);
    }

    /**
     *
     * @param trafficFlow
     * @param path
     * @param overhead ratio (e.g. Tunneling overhead)
     */
    private void calculateTrafficFlowLinkUsage(TrafficFlow trafficFlow, List<WeightedLink> path, float overhead) {
        for (WeightedLink weightedLink : path) {
            Double usage = linkUsageMap.get(weightedLink);
            if (usage == null){
                usage = new Double(0);
            }
            usage += trafficFlow.getRate() + trafficFlow.getRate()*overhead;
            linkUsageMap.put(weightedLink, usage);
        }

    }

    private void calculateTrafficFlowHostUsage(TrafficFlow trafficFlow, List<WeightedLink> path) {
        // TODO Auto-generated method stub

    }

    private void calculateTrafficFlowSwitchUsage(TrafficFlow trafficFlow, List<WeightedLink> path) {
        // TODO Auto-generated method stub

    }
}
