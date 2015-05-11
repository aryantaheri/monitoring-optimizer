package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;

public class PathFinderUtil {
    private static Logger log = LoggerFactory.getLogger(PathFinderUtil.class);
    private static HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> numericPathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();
    private static HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> stateMachinePathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

    public static void main(String[] args) {
        int k = 4;
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);
        setNumericPathsMap(topo, k);
        setStateMachinePathsMap(topo, k);
        verify(topo, k);
        //        benchmarkDijkstra();
    }

    public static void benchmarkDijkstra(int k, int terminatingHostCount, boolean persistWithSubpath) {
        DescriptiveStatistics pairPerSecondStats = new DescriptiveStatistics();

        HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> numericPathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);
        Yen<Node, WeightedLink> yen = new Yen<Node, WeightedLink>(topo,
                new Transformer<WeightedLink, Number>() {
            @Override
            public Number transform(WeightedLink link) {
                return link.getWeight();
            }
        });

        int pairs = 0;
        long lastTimeStamp = new Date().getTime();
        Collection<Node> nodes = topo.getVertices();
        for (Node src : nodes) {
            for (Node dst : nodes) {
                if (src.equals(dst)) continue;
                if (pairs == terminatingHostCount) break;
                if(src instanceof Host && dst instanceof Host) {
                    List<List<WeightedLink>> paths;
                    try {
                        paths = yen.getShortestPaths(src, dst, 4);
                        if (persistWithSubpath){
                            PathFinderUtil.addPaths(numericPathsMap, topo, paths);
                        }

                        pairs++;
                        if (pairs % 100 == 0){
                            long currentTimeStamp = new Date().getTime();
                            double pairPerSecond = 100*1000 / (currentTimeStamp - lastTimeStamp);
                            //                            log.info("Pairs processed={} time={} avg-pair-per-second={}",
                            //                                    pairs, new Date(currentTimeStamp), pairPerSecond);
                            lastTimeStamp = currentTimeStamp;
                            pairPerSecondStats.addValue(pairPerSecond);
                        }
                    } catch (Exception e) {
                        log.error("benchmarkDijkstra msg={}", e.getMessage());
                    }
                }
            }
        }
        int foundPaths = PathFinderUtil.getTotalPaths(numericPathsMap);
        double totalPaths = Math.pow(k, 6);
        log.info("Stats: k={} persistWithSubPath={} pairPerSecond={}/{}/{}/{} foundPaths={} totalPaths={} pairs={}",
                k, persistWithSubpath,
                pairPerSecondStats.getMin(), pairPerSecondStats.getMean(), pairPerSecondStats.getMax(), pairPerSecondStats.getStandardDeviation(),
                foundPaths, totalPaths, pairs);

    }

    private static void setStateMachinePathsMap(Graph<Node, WeightedLink> topo, int k) {
        try {
            stateMachinePathsMap = new PathFinder().findPaths(topo);
            writePathsInCsv("/tmp/statemachine-paths.txt", stateMachinePathsMap);
        } catch (Exception e) {
            log.error("setStateMachinePathsMap", e);
        }
    }

    private static void setNumericPathsMap(Graph<Node, WeightedLink> topo, int k) {
        Collection<Node> nodes = topo.getVertices();
        for (Node src : nodes) {
            for (Node dst : nodes) {
                if (src.equals(dst)) continue;
                List<List<WeightedLink>> paths;
                try {
                    paths = NumericPathFinder.findPath(topo, k, src, dst);
                    addPaths(numericPathsMap, topo, paths);
                } catch (Exception e) {
                    log.error("setNumericPathsMap msg={}", e.getMessage());
                }
            }
        }
        writePathsInCsv("/tmp/numeric-paths.txt", numericPathsMap);
    }

    public static boolean verify(Graph<Node, WeightedLink> topo, int k){
        boolean correct = false;

        boolean hostCorrect = verifyHost(topo, k);
        boolean switchCorrect = verifySwitch(topo, k);
        boolean hostSwitchCorrect = verifyHostSwitch(topo, k);
        boolean switchHostCorrect = verifySwitchHost(topo, k);

        log.info("verify: K={}, hostCorrect={}, switchCorrect={}, hostSwitchCorrect={}, switchHostCorrect={}",
                k, hostCorrect, switchCorrect, hostSwitchCorrect, switchHostCorrect);
        correct = hostCorrect && switchCorrect && hostSwitchCorrect && switchHostCorrect;
        return correct;
    }

    private static boolean verifySwitchHost(Graph<Node, WeightedLink> topo, int k) {
        Collection<Node> nodes = topo.getVertices();
        boolean correct = true;
        for (Node src : nodes) {
            if (!(src instanceof Switch)) continue;
            for (Node dst : nodes) {
                if (!(dst instanceof Host)) continue;
                List<List<WeightedLink>> numericPaths = getPaths(numericPathsMap, src, dst);
                List<List<WeightedLink>> stateMachinePaths = getPaths(stateMachinePathsMap, src, dst);
                boolean areEqual = arePathsEqual(numericPaths, stateMachinePaths);
                if (!areEqual){
                    correct = false;
                }
            }
        }
        return correct;
    }

    private static boolean verifyHostSwitch(Graph<Node, WeightedLink> topo, int k) {
        Collection<Node> nodes = topo.getVertices();
        boolean correct = true;
        for (Node src : nodes) {
            if (!(src instanceof Host)) continue;
            for (Node dst : nodes) {
                if (!(dst instanceof Switch)) continue;
                List<List<WeightedLink>> numericPaths = getPaths(numericPathsMap, src, dst);
                List<List<WeightedLink>> stateMachinePaths = getPaths(stateMachinePathsMap, src, dst);
                boolean areEqual = arePathsEqual(numericPaths, stateMachinePaths);
                if (!areEqual){
                    correct = false;
                }
            }
        }
        return correct;
    }

    private static boolean verifySwitch(Graph<Node, WeightedLink> topo, int k) {
        Collection<Node> nodes = topo.getVertices();
        boolean correct = true;
        for (Node src : nodes) {
            if (!(src instanceof Switch)) continue;
            for (Node dst : nodes) {
                if (!(dst instanceof Switch)) continue;
                List<List<WeightedLink>> numericPaths = getPaths(numericPathsMap, src, dst);
                List<List<WeightedLink>> stateMachinePaths = getPaths(stateMachinePathsMap, src, dst);
                boolean areEqual = arePathsEqual(numericPaths, stateMachinePaths);
                if (!areEqual){
                    correct = false;
                }
            }
        }
        return correct;
    }

    private static boolean verifyHost(Graph<Node, WeightedLink> topo, int k) {
        Collection<Node> nodes = topo.getVertices();
        boolean correct = true;
        for (Node src : nodes) {
            if (!(src instanceof Host)) continue;
            for (Node dst : nodes) {
                if (!(dst instanceof Host)) continue;
                List<List<WeightedLink>> numericPaths = getPaths(numericPathsMap, src, dst);
                List<List<WeightedLink>> stateMachinePaths = getPaths(stateMachinePathsMap, src, dst);
                boolean areEqual = arePathsEqual(numericPaths, stateMachinePaths);
                if (!areEqual){
                    correct = false;
                }
            }
        }
        return correct;
    }

    private static boolean arePathsEqual(List<List<WeightedLink>> paths1, List<List<WeightedLink>> paths2){
        boolean correct = true;
        boolean equalFound = false;
        if (paths1 == null && paths2 == null) return true;
        if ((paths1 == null && paths2 != null) ||(paths1 != null && paths2 == null)) return false;
        for (List<WeightedLink> path1 : paths1) {
            equalFound = false;
            for (List<WeightedLink> path2 : paths2) {
                if (path1.equals(path2)) {
                    equalFound = true;
                    break;
                }
            }
            if (!equalFound){
                log.error("verifyPaths: path1={} is not found n paths1={}", path1, paths2);
                correct = false;
            }
        }
        return correct;
    }

    private static List<List<WeightedLink>> getPaths(HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap,
            Node src, Node dst) {
        if (pathsMap == null) return null;
        if (pathsMap.get(src) == null) return null;
        return pathsMap.get(src).get(dst);
    }

    public static void addPaths(HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap,
            Graph<Node, WeightedLink> topo, List<List<WeightedLink>> paths) {
        if (paths == null) return;
        for (List<WeightedLink> path : paths) {
            //            addPath(pathsMap, src, dst, path);
            addSubPaths(pathsMap, topo, path);
        }
    }

    private static void addSubPaths(HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap,
            Graph<Node, WeightedLink> topo, List<WeightedLink> path) {
        for (int i = 0; i < path.size(); i++) {
            for (int j = i; j < path.size(); j++) {
                Node src = topo.getSource(path.get(i));
                Node dst = topo.getDest(path.get(j));

                addPath(pathsMap, src, dst, path.subList(i, j + 1));
            }
        }
    }

    private static void addPath(HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap,
            Node src, Node dst, List<WeightedLink> path) {
        HashMap<Node, List<List<WeightedLink>>> srcMap = pathsMap.get(src);
        if (srcMap == null) {
            srcMap = new HashMap<Node, List<List<WeightedLink>>>();
        }
        List<List<WeightedLink>> paths = srcMap.get(dst);
        if (paths == null) {
            paths = new ArrayList<List<WeightedLink>>();
        }
        if (!paths.contains(path)){
            paths.add(path);
        }
        srcMap.put(dst, paths);
        pathsMap.put(src, srcMap);
    }


    public static int getTotalPaths(HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap) {
        int total = 0;
        for (HashMap<Node, List<List<WeightedLink>>> srcMap : pathsMap.values()) {
            for (List<List<WeightedLink>> paths : srcMap.values()) {
                total += paths.size();
            }
        }
        return total;
    }

    public static String writePathsInCsv(String fileName, HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap) {
        StringBuilder out = new StringBuilder();
        List<Node> sources = new ArrayList<Node>(pathsMap.keySet());

        Set<Node> tmp = new HashSet<Node>();

        for (Node node : sources) {
            tmp.addAll(pathsMap.get(node).keySet());
        }
        List<Node> destinations = new ArrayList<Node>(tmp);

        Collections.sort(sources, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getId().startsWith("host") && o2.getId().startsWith("sw")) return -1;
                if (o2.getId().startsWith("host") && o1.getId().startsWith("sw")) return 1;
                return Integer.compare(Integer.parseInt(o1.getId().replaceAll("[^0-9]+", "")),
                        Integer.parseInt(o2.getId().replaceAll("[^0-9]+", "")));
            }
        });
        Collections.sort(destinations, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getId().startsWith("host") && o2.getId().startsWith("sw")) return -1;
                if (o2.getId().startsWith("host") && o1.getId().startsWith("sw")) return 1;
                return Integer.compare(Integer.parseInt(o1.getId().replaceAll("[^0-9]+", "")),
                        Integer.parseInt(o2.getId().replaceAll("[^0-9]+", "")));
            }
        });

        for (Node node : destinations) {
            out.append(",").append(node);
        }
        out.append('\n');
        for (Node src : sources) {
            out.append(src).append(",");
            for (Node dst : destinations) {
                if (pathsMap.get(src).get(dst) == null){
                    out.append(pathsMap.get(src).get(dst)).append(",");
                } else {
                    out.append(pathsMap.get(src).get(dst).size()).append(",");
                }
            }
            out.append('\n');
        }
        writeToFile(fileName, out.toString());
        return out.toString();
    }

    private static void writeToFile(String file, String content) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
            writer.write(content);
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
