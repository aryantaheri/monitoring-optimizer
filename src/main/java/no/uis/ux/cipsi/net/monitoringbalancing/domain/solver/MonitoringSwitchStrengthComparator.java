package no.uis.ux.cipsi.net.monitoringbalancing.domain.solver;

import java.io.Serializable;
import java.util.Comparator;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;

import org.apache.commons.lang.builder.CompareToBuilder;

public class MonitoringSwitchStrengthComparator implements Comparator<Switch>, Serializable {

    @Override
    public int compare(Switch sw1, Switch sw2) {
        return new CompareToBuilder()
        .append(sw1.getFabricCapacity(), sw2.getFabricCapacity())
        .append(sw1.getForwardingCapacity(), sw2.getForwardingCapacity())
        .append(sw2.getInitCost(), sw2.getInitCost())
        .append(sw2.getPerFlowReuseCost(), sw2.getPerFlowReuseCost())
        .toComparison();
    }

}
