package no.uis.ux.cipsi.net.monitoringbalancing.solver.move;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class MonitoringHostChangeMove extends AbstractMove {

    private TrafficFlow trafficFlow;
    private MonitoringHost toMonitoringHost;

    public MonitoringHostChangeMove(TrafficFlow trafficFlow, MonitoringHost toMonitoringHost) {
        this.trafficFlow = trafficFlow;
        this.toMonitoringHost = toMonitoringHost;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return !Objects.equals(trafficFlow.getMonitoringHost(), toMonitoringHost);
    }

    @Override
    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new MonitoringHostChangeMove(trafficFlow, trafficFlow.getMonitoringHost());
    }

    @Override
    public void doMove(ScoreDirector scoreDirector) {
        MonitoringBalancingMoveHelper.moveMonitoringHost(scoreDirector, trafficFlow, toMonitoringHost);
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(trafficFlow);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toMonitoringHost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MonitoringHostChangeMove) {
            MonitoringHostChangeMove other = (MonitoringHostChangeMove) o;
            return new EqualsBuilder()
            .append(trafficFlow, other.trafficFlow)
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
        .append(toMonitoringHost)
        .toHashCode();
    }

    @Override
    public String toString() {
        return trafficFlow + " => " + toMonitoringHost;
    }

}
