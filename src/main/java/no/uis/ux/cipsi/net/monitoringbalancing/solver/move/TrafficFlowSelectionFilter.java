package no.uis.ux.cipsi.net.monitoringbalancing.solver.move;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringBalance;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class TrafficFlowSelectionFilter implements SelectionFilter<TrafficFlow>{

    @Override
    public boolean accept(ScoreDirector scoreDirector, TrafficFlow trafficFlow) {
        MonitoringBalance monitoringBalance = (MonitoringBalance) scoreDirector.getWorkingSolution();
        return accept(monitoringBalance, trafficFlow);
    }

    private boolean accept(MonitoringBalance monitoringBalance, TrafficFlow trafficFlow) {
        if(trafficFlow.getMonitoringHost() == null || trafficFlow.getMonitoringSwitch() == null){
            System.out.println(trafficFlow + " " + trafficFlow.getMonitoringHost() + " " + trafficFlow.getMonitoringSwitch());
            return false;
        } else {
            boolean onPath = TopologyManager.getInstance().getSwitchesOnPath(trafficFlow.getPath()).contains(trafficFlow.getMonitoringSwitch());
            System.out.println(trafficFlow + " " + trafficFlow.getMonitoringSwitch() + " isOnPath: " + onPath);
            return onPath;
        }
    }


}
