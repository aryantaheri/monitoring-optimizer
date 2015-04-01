package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

public class YenNoCache<V, E> extends Yen<V, E>{

    public YenNoCache(Graph<V, E> graph,
            Transformer<E, Number> nev) {
        super(graph, nev);
        dijkstra.enableCaching(false);
        dijkstra.setMaxTargets(8);
        //        dijkstra.setMaxDistance(8);
    }

}
