package no.uis.ux.cipsi.net.monitoringbalancing.solver.move;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MonitoringBalancingMoveHelper {

    public static void moveMonitoringSwitch(ScoreDirector scoreDirector, TrafficFlow trafficFlow, Switch toMonitoringSwitch) {
        //FIXME: switch or monitoringSwitch
        scoreDirector.beforeVariableChanged(trafficFlow, "monitoringSwitch");
        trafficFlow.setMonitoringSwitch(toMonitoringSwitch);
        scoreDirector.afterVariableChanged(trafficFlow, "monitoringSwitch");

    }

    public static void moveMonitoringHost(ScoreDirector scoreDirector, TrafficFlow trafficFlow, MonitoringHost toMonitoringHost) {
        //FIXME: host or monitoringHost where is it?
        scoreDirector.beforeVariableChanged(trafficFlow, "monitoringHost");
        trafficFlow.setMonitoringHost(toMonitoringHost);
        scoreDirector.afterVariableChanged(trafficFlow, "monitoringHost");

    }

    public static void move(ScoreDirector scoreDirector, TrafficFlow trafficFlow, Switch toMonitoringSwitch, MonitoringHost toMonitoringHost) {

        scoreDirector.beforeVariableChanged(trafficFlow, "monitoringSwitch");
        scoreDirector.beforeVariableChanged(trafficFlow, "monitoringHost");

        trafficFlow.setMonitoringSwitch(toMonitoringSwitch);
        trafficFlow.setMonitoringHost(toMonitoringHost);

        scoreDirector.afterVariableChanged(trafficFlow, "monitoringSwitch");
        scoreDirector.afterVariableChanged(trafficFlow, "monitoringHost");

    }

}
