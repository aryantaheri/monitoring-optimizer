package no.uis.ux.cipsi.net.monitoringbalancing.solver.score;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;

public class IncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<TrafficFlow> {


    @Override
    public void resetWorkingSolution(TrafficFlow workingSolution) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityAdded(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEntityAdded(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public Score calculateScore() {
        // TODO Auto-generated method stub
        return null;
    }


    private void insert(TrafficFlow trafficFlow) {
        // TODO Auto-generated method stub

    }

    private void retract(TrafficFlow trafficFlow) {
        // TODO Auto-generated method stub

    }
}
