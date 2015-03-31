package no.uis.ux.cipsi.net.monitoringbalancing.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import com.google.common.collect.HashBasedTable;

public class AlgoCache {

    private static HashMap<Yen<Node, WeightedLink>, HashBasedTable<Node, Node, List<List<WeightedLink>>>> cache = new HashMap<Yen<Node, WeightedLink>, HashBasedTable<Node, Node, List<List<WeightedLink>>>>();

    public static List<List<WeightedLink>> getPaths(Yen<Node, WeightedLink> algo, Node src, Node dst) {

        List<List<WeightedLink>> paths = null;
        HashBasedTable<Node, Node, List<List<WeightedLink>>> topoCache = cache.get(algo);
        if (topoCache == null) {
            return null;
        }
        paths = topoCache.get(src, dst);
        return paths;
    }

    public static void insertPaths(Yen<Node, WeightedLink> algo, Node src, Node dst, List<List<WeightedLink>> paths) {
        HashBasedTable<Node, Node, List<List<WeightedLink>>> topoCache = cache.get(algo);
        if (topoCache == null) {
            topoCache = HashBasedTable.create();
        }
        topoCache.put(src, dst, paths);
        cache.put(algo, topoCache);
    }

    public static void appendPaths(Yen<Node, WeightedLink> algo, Node src, Node dst, List<List<WeightedLink>> paths) {
        HashBasedTable<Node, Node, List<List<WeightedLink>>> topoCache = cache.get(algo);
        if (topoCache == null) {
            topoCache = HashBasedTable.create();
        }
        List<List<WeightedLink>> origPaths = topoCache.get(src, dst);
        if (origPaths == null){
            origPaths = new ArrayList<List<WeightedLink>>();
        }
        origPaths.addAll(paths);
        topoCache.put(src, dst, origPaths);
        cache.put(algo, topoCache);
    }

    public static void mergePaths(Yen<Node, WeightedLink> algo, Node src, Node dst, List<List<WeightedLink>> paths) {
        HashBasedTable<Node, Node, List<List<WeightedLink>>> topoCache = cache.get(algo);
        if (topoCache == null) {
            insertPaths(algo, src, dst, paths);
            return;
        }
        List<List<WeightedLink>> origPaths = topoCache.get(src, dst);
        if (origPaths == null){
            insertPaths(algo, src, dst, paths);
            return;
        }

        Set<List<WeightedLink>> mergedPathSet = new HashSet<List<WeightedLink>>();
        mergedPathSet.addAll(origPaths);
        mergedPathSet.addAll(paths);
        List<List<WeightedLink>> mergedPathList = new ArrayList<List<WeightedLink>>(mergedPathSet);
        Collections.sort(mergedPathList, new Comparator<List<WeightedLink>>() {
            @Override
            public int compare(List<WeightedLink> p1, List<WeightedLink> p2) {
                return Integer.compare(p1.size(), p2.size());
            }
        });

        insertPaths(algo, src, dst, mergedPathList);

    }
}
