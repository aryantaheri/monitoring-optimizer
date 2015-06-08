package no.uis.ux.cipsi.net.monitoringbalancing.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.solver.move.MonitoringChangeMove;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import edu.uci.ics.jung.graph.Graph;

public class MonitoringChangeMoveFactory implements MoveListFactory<MonitoringBalance> {

    @Override
    public List<? extends Move> createMoveList(MonitoringBalance solution) {
        List<Move> moveList = new ArrayList<Move>();
        List<Switch> monitoringSwitches = solution.getMonitoringSwitches();
        List<MonitoringHost> monitoringHosts = solution.getMonitoringHosts();
        Graph<Node, WeightedLink> topology = solution.getTopology();
        for (TrafficFlow trafficFlow : solution.getTrafficFlows()) {
            for (Switch monitoringSwitch : monitoringSwitches) {
                for (MonitoringHost monitoringHost : monitoringHosts) {
                    if (trafficFlow.getOnPathMonitoringSwitches().contains(monitoringSwitch)){
                        // System.out.println("MonitoringChangeMoveFactory " + trafficFlow + " " + monitoringSwitch + " " + monitoringHost);
                        moveList.add(new MonitoringChangeMove(topology, trafficFlow, monitoringSwitch, monitoringHost));
                    }
                }
            }
        }

        return moveList;
    }

}
