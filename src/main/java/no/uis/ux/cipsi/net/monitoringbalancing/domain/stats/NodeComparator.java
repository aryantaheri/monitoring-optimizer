package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.Comparator;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;

public class NodeComparator implements Comparator<Node>{

    @Override
    public int compare(Node n1, Node n2) {
        return n1.getId().compareTo(n2.getId());
    }

}
