package no.uis.ux.cipsi.net.monitoringbalancing.domain.solver;

import java.io.Serializable;
import java.util.Comparator;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;

import org.apache.commons.lang.builder.CompareToBuilder;

public class MonitoringHostStrengthComparator implements Comparator<MonitoringHost>, Serializable {

    @Override
    public int compare(MonitoringHost h1, MonitoringHost h2) {
        return new CompareToBuilder()
        .append(h1.getMultiplicand(), h2.getMultiplicand())
        .append(h2.getCost(), h1.getCost()) // Higher-end HW results in better/lower overall cost and better optimization?
        .toComparison();
    }

}
