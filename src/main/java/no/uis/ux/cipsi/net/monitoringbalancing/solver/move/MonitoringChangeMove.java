package no.uis.ux.cipsi.net.monitoringbalancing.solver.move;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MonitoringChangeMove extends AbstractMove {

    private TrafficFlow trafficFlow;
    private Switch toMonitoringSwitch;
    private MonitoringHost toMonitoringHost;

    public MonitoringChangeMove(TrafficFlow trafficFlow, Switch toMonitoringSwitch, MonitoringHost toMonitoringHost) {
        this.trafficFlow = trafficFlow;
        this.toMonitoringSwitch = toMonitoringSwitch;
        this.toMonitoringHost = toMonitoringHost;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        //        System.out.println("isMoveDoable " + trafficFlow + " " + toMonitoringSwitch + " " + toMonitoringHost);
        if (toMonitoringHost == null || toMonitoringSwitch == null) return false;
        boolean isSwitchOnPath = TopologyManager.getInstance().getSwitchesOnPath(trafficFlow.getPath()).contains(toMonitoringSwitch);
        boolean equalSwitch = Objects.equals(trafficFlow.getMonitoringSwitch(), toMonitoringSwitch);
        boolean equalHost = Objects.equals(trafficFlow.getMonitoringHost(), toMonitoringHost);

        boolean movable = isSwitchOnPath & !(equalSwitch & equalHost);
        return movable;
    }

    @Override
    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new MonitoringChangeMove(trafficFlow, trafficFlow.getMonitoringSwitch(), trafficFlow.getMonitoringHost());
    }

    @Override
    public void doMove(ScoreDirector scoreDirector) {
        MonitoringBalancingMoveHelper.move(scoreDirector, trafficFlow, toMonitoringSwitch, toMonitoringHost);
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(trafficFlow);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(Arrays.asList(toMonitoringSwitch, toMonitoringHost));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MonitoringChangeMove) {
            MonitoringChangeMove other = (MonitoringChangeMove) o;
            return new EqualsBuilder()
            .append(trafficFlow, other.trafficFlow)
            .append(toMonitoringSwitch, other.toMonitoringSwitch)
            .append(toMonitoringHost, other.toMonitoringHost)
            .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(trafficFlow)
        .append(toMonitoringSwitch)
        .append(toMonitoringHost)
        .toHashCode();
    }

    @Override
    public String toString() {
        return trafficFlow + " => (" + toMonitoringSwitch + ", " + toMonitoringHost + ")";
    }
}
