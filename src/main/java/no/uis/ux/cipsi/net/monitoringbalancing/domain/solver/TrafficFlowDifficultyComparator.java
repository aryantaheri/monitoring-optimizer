package no.uis.ux.cipsi.net.monitoringbalancing.domain.solver;

import java.io.Serializable;
import java.util.Comparator;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;

import org.apache.commons.lang.builder.CompareToBuilder;

public class TrafficFlowDifficultyComparator implements Comparator<TrafficFlow>, Serializable {

    @Override
    public int compare(TrafficFlow f1, TrafficFlow f2) {
        return new CompareToBuilder()
        .append(f1.getRate(), f2.getRate())
        .toComparison();
        //TODO we should care about the traffic type as well
        // if it's on the same edge, same pod or inter-pod
    }
}