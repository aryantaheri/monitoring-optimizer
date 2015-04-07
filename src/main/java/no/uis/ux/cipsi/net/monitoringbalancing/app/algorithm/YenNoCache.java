package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

public class YenNoCache<V, E> extends Yen<V, E>{


    private static final long serialVersionUID = 1L;

    public YenNoCache(Graph<V, E> graph,
            Transformer<E, Number> nev) {
        super(graph, nev);
        dijkstra.enableCaching(false);
        //        dijkstra.setMaxTargets(8);
        dijkstra.setMaxDistance(8);
    }

    public Graph<V,E> getGraph() {
        return graph;
    }

}
