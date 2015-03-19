package no.uis.ux.cipsi.net.monitoringbalancing.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.solver.move.MonitoringChangeMove;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

public class MonitoringChangeMoveFactory implements MoveListFactory<MonitoringBalance> {

    @Override
    public List<? extends Move> createMoveList(MonitoringBalance solution) {
        List<Move> moveList = new ArrayList<Move>();
        List<Switch> monitoringSwitches = solution.getMonitoringSwitches();
        List<MonitoringHost> monitoringHosts = solution.getMonitoringHosts();
        for (TrafficFlow trafficFlow : solution.getTrafficFlows()) {
            for (Switch monitoringSwitch : monitoringSwitches) {
                for (MonitoringHost monitoringHost : monitoringHosts) {
                    //                    System.out.println("MonitoringChangeMoveFactory " + trafficFlow + " " + monitoringSwitch + " " + monitoringHost);
                    moveList.add(new MonitoringChangeMove(trafficFlow, monitoringSwitch, monitoringHost));
                }
            }
        }

        return moveList;

        //        List<CloudComputer> cloudComputerList = cloudBalance.getComputerList();
        //        for (CloudProcess cloudProcess : cloudBalance.getProcessList()) {
        //            for (CloudComputer cloudComputer : cloudComputerList) {
        //                moveList.add(new CloudComputerChangeMove(cloudProcess, cloudComputer));
        //            }
        //        }
        //        return moveList;
    }

}
