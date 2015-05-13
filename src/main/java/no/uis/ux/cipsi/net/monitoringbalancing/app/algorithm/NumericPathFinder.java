package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class NumericPathFinder {
    private static Logger log = LoggerFactory.getLogger(NumericPathFinder.class);

    public static void main(String[] args) {
        benchmarkNumericPathFinder(4, 2000, true);
        //        benchmarkNumericPathFinder(8, 2000, true);
        //        benchmarkNumericPathFinder(16, 2000, true);
        //        benchmarkNumericPathFinder(32, 2000, true);
        //        benchmarkNumericPathFinder(48, 2000, true);
        //        benchmarkNumericPathFinder(4, 200, false);
        //        benchmarkNumericPathFinder(8, 2000, false);
        //        benchmarkNumericPathFinder(16, 2000, false);
        //        benchmarkNumericPathFinder(32, 20000, false);
        //        benchmarkNumericPathFinder(48, 20000, false);


        //        PathFinderUtil.benchmarkDijkstra(4, 200, true);
        //        PathFinderUtil.benchmarkDijkstra(8, 2000, true);
        //        PathFinderUtil.benchmarkDijkstra(16, 2000, true);
        //        PathFinderUtil.benchmarkDijkstra(32, 2000, true);
        //        PathFinderUtil.benchmarkDijkstra(48, 2000, true);
        //        PathFinderUtil.benchmarkDijkstra(4, 200, false);
        //        PathFinderUtil.benchmarkDijkstra(8, 2000, false);
        //        PathFinderUtil.benchmarkDijkstra(16, 2000, false);
        //        PathFinderUtil.benchmarkDijkstra(32, 2000, false);
        //        PathFinderUtil.benchmarkDijkstra(48, 2000, false);
    }
    public static void main2(String[] args) {
        HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> numericPathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

        int k = 8;
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);

        int pairs = 0;
        long lastTimeStamp = new Date().getTime();
        Collection<Node> nodes = topo.getVertices();
        for (Node src : nodes) {
            for (Node dst : nodes) {
                if (src.equals(dst)) continue;
                if(src instanceof Host && dst instanceof Host) {
                    List<List<WeightedLink>> paths;
                    try {
                        paths = NumericPathFinder.findPath(topo, k, src, dst);
                        PathFinderUtil.addPaths(numericPathsMap, topo, paths);
                        pairs++;
                        if (pairs % 100 == 0){
                            long currentTimeStamp = new Date().getTime();
                            log.info("Pairs processed={} time={} avg-pair-per-second={}",
                                    pairs, new Date(currentTimeStamp), 100*1000 / (currentTimeStamp - lastTimeStamp));
                            lastTimeStamp = currentTimeStamp;
                        }
                    } catch (Exception e) {
                        log.error("setNumericPathsMap msg={}", e.getMessage());
                    }
                }
            }
        }
        PathFinderUtil.writePathsInCsv("/tmp/numeric-paths-48.txt", numericPathsMap);
        //        testFindHostToAllHosts();
    }

    private static double benchmarkNumericPathFinder(int k, int terminatingHostCount, boolean persistWithSubpath) {
        DescriptiveStatistics pairPerSecondStats = new DescriptiveStatistics();

        HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> numericPathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);

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
                        paths = NumericPathFinder.findPath(topo, k, src, dst);
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
                        log.error("setNumericPathsMap msg={}", e.getMessage());
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
        return pairPerSecondStats.getMean();
    }

    private static void testFindHostToAllHosts() {
        HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> numericPathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

        int k = 4;
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);

        int pairs = 0;
        long lastTimeStamp = new Date().getTime();
        Collection<Node> nodes = topo.getVertices();
        for (Node src : nodes) {
            if (!(src.getId().equalsIgnoreCase("host1"))) continue;
            for (Node dst : nodes) {
                if (!(dst instanceof Host)) continue;
                if (src.equals(dst)) continue;
                if(src instanceof Host && dst instanceof Host) {
                    List<List<WeightedLink>> paths;
                    try {
                        paths = NumericPathFinder.findPath(topo, k, src, dst);
                        PathFinderUtil.addPaths(numericPathsMap, topo, paths);
                        pairs++;
                        if (pairs % 100 == 0){
                            long currentTimeStamp = new Date().getTime();
                            log.info("Pairs processed={} time={} avg-pair-per-second={}",
                                    pairs, new Date(currentTimeStamp), 100*1000 / (currentTimeStamp - lastTimeStamp));
                            lastTimeStamp = currentTimeStamp;
                        }
                    } catch (Exception e) {
                        log.error("setNumericPathsMap msg={}", e.getMessage());
                    }
                }
            }
            break;
        }
        PathFinderUtil.writePathsInCsv("/tmp/numeric-paths-4-hosttohosts.txt", numericPathsMap);

    }

    public static void test(String[] args) throws Exception {
        int k = 4;
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+k);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);
        for (int i = 1; i <= k*k*k/4; i++) {
            System.out.print("host"+i + ": podId: " + getPodId(k, i) + ", edgeId: " + getEdgeId(k, i) + ", aggrsId: " + getAggIds(k, i));
            for (int aggs : getAggIds(k, i)) {
                System.out.print(", aggrId: " + aggs + ", coreId: " + getCoreIds(k, aggs));
            }
            System.out.println("");
        }
        List<List<WeightedLink>> paths = findHostPaths(topo, k, getHost(topo, 1), getHost(topo, 2));
        System.out.println("src: " + getHost(topo, 1) + " dst: " + getHost(topo, 2));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }

        paths = findHostPaths(topo, k, getHost(topo, 1), getHost(topo, k/2+1));
        System.out.println("src: " + getHost(topo, 1) + " dst: " + getHost(topo, k/2+1));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }

        paths = findHostPaths(topo, k, getHost(topo, 1), getHost(topo, (k*k/4)+1));
        System.out.println("src: " + getHost(topo, 1) + " dst: " + getHost(topo, k+1));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }

        paths = findSwitchPaths(topo, k, getSwitch(topo, 7), getSwitch(topo, 8));
        System.out.println("src: " + getSwitch(topo, 7) + " dst: " + getSwitch(topo, 8));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }

        Node src = getSwitch(topo, 7);
        Node dst = getHost(topo, 1);
        paths = findSwitchHostPaths(topo, k, (Switch) src, (Host) dst);
        System.out.println("src: " + src + " dst: " + dst);
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        System.out.println(PathFinder.prettyPrintPaths(topo, paths));

        paths = findSwitchHostPaths(topo, k, getSwitch(topo, 6), getHost(topo, 1));
        System.out.println("src: " + getSwitch(topo, 7) + " dst: " + getHost(topo, 1));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }

        paths = findSwitchHostPaths(topo, k, getSwitch(topo, 1), getHost(topo, 1));
        System.out.println("src: " + getSwitch(topo, 7) + " dst: " + getHost(topo, 1));
        System.out.println("path size: " + paths.size());
        System.out.println(paths);
        for (List<WeightedLink> path : paths) {
            System.out.println(PathFinder.prettyPrintPath(topo, path));
        }
    }

    public static List<List<WeightedLink>>  findPath(Graph<Node, WeightedLink> topo, int k, Node src, Node dst) throws Exception {
        if (src.equals(dst)) return null;

        if (src instanceof Host && dst instanceof Host){
            return findHostPaths(topo, k, src, dst);
        } else if (src instanceof Switch && dst instanceof Host){
            return findSwitchHostPaths(topo, k, (Switch) src, (Host) dst);
        } else if (src instanceof Switch && dst instanceof Switch){
            return findSwitchPaths(topo, k, (Switch) src, (Switch) dst);
        } else {
            throw new NotImplementedException("NumericFindPath is not implemented for src="+src+", dst="+dst);
        }

    }

    private static List<List<WeightedLink>> findSwitchHostPaths(Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {
        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));
        int dstPodId = getPodId(k, getIntValue(dst.getId()));

        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));


        if (srcPodId == dstPodId){
            // Same Pod
            if (src.getType() == TYPE.EDGE){
                if (dstEdgeId == getIntValue(src.getId())) {
                    // Directly connected
                    return getLocalEdgePaths(topo, k, src, dst);
                } else {
                    // Connected via local Aggrs.
                    return getRemoteEdgePaths(topo, k, src, dst);
                }
            } else if (src.getType() == TYPE.AGGREGATION) {
                return getLocalAggrPaths(topo, k, src, dst);
            }
        } else {
            // Different Pods
            // Use Edge+Aggrs+Cores
            if (src.getType() == TYPE.EDGE){
                return getDifferentPodEdgeSwitchHostPaths(topo, k, src, dst);
            } else if (src.getType() == TYPE.AGGREGATION) {
                return getDifferentPodAggrSwitchHostPaths(topo, k, src, dst);
            } else if (src.getType() == TYPE.CORE) {
                return getDifferentPodCoreSwitchHostPaths(topo, k, src, dst);
            }
        }
        return null;
    }



    private static List<List<WeightedLink>> getDifferentPodEdgeSwitchHostPaths(Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {
        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);

        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));

        List<Integer> srcAggrIds = getAggIdsFromPodId(k, srcPodId);
        List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));

        List<Switch> srcAggrs = getSwitches(topo, srcAggrIds);
        List<Switch> dstAggrs = getSwitches(topo, dstAggrIds);

        List<WeightedLink> downLinks3 = getLinks(topo, dstEdgeSw, dst);

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        for (Switch srcAggr : srcAggrs) {
            for (Switch dstAggr : dstAggrs) {

                List<WeightedLink> upLinks2 = getLinks(topo, src, srcAggr);
                List<WeightedLink> downLinks2 = getLinks(topo, dstAggr, dstEdgeSw);

                List<Integer> commonCoreIds = getCommonCoreIds(k, getIntValue(srcAggr.getId()), getIntValue(dstAggr.getId()));
                List<WeightedLink> upLinks3;
                List<WeightedLink> downLinks1;
                for (Integer coreId : commonCoreIds) {
                    Switch core = getSwitch(topo, coreId);
                    upLinks3 = getLinks(topo, srcAggr, core);
                    downLinks1 = getLinks(topo, core, dstAggr);

                    paths.addAll(mergeLinks(Lists.newArrayList(upLinks2, upLinks3, downLinks1, downLinks2, downLinks3)));
                }
            }
        }

        return paths;
    }

    private static List<List<WeightedLink>> getDifferentPodAggrSwitchHostPaths(Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {
        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);

        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));

        List<Integer> srcAggrIds = getAggIdsFromPodId(k, srcPodId);
        List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));

        List<Switch> srcAggrs = getSwitches(topo, srcAggrIds);
        List<Switch> dstAggrs = getSwitches(topo, dstAggrIds);

        List<WeightedLink> downLinks3 = getLinks(topo, dstEdgeSw, dst);

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        for (Switch dstAggr : dstAggrs) {

            List<WeightedLink> downLinks2 = getLinks(topo, dstAggr, dstEdgeSw);

            List<Integer> commonCoreIds = getCommonCoreIds(k, getIntValue(src.getId()), getIntValue(dstAggr.getId()));
            List<WeightedLink> upLinks3;
            List<WeightedLink> downLinks1;
            for (Integer coreId : commonCoreIds) {
                Switch core = getSwitch(topo, coreId);
                upLinks3 = getLinks(topo, src, core);
                downLinks1 = getLinks(topo, core, dstAggr);

                paths.addAll(mergeLinks(Lists.newArrayList(upLinks3, downLinks1, downLinks2, downLinks3)));
            }
        }

        return paths;
    }


    private static List<List<WeightedLink>> getDifferentPodCoreSwitchHostPaths(Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {
        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);


        List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));
        List<Switch> dstAggrs = getSwitches(topo, dstAggrIds);

        List<WeightedLink> downLinks3 = getLinks(topo, dstEdgeSw, dst);

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        for (Switch dstAggr : dstAggrs) {

            List<WeightedLink> downLinks2 = getLinks(topo, dstAggr, dstEdgeSw);

            List<WeightedLink> downLinks1;
            downLinks1 = getLinks(topo, src, dstAggr);
            if (downLinks1.size() == 0) continue;
            paths.addAll(mergeLinks(Lists.newArrayList(downLinks1, downLinks2, downLinks3)));
        }

        return paths;
    }

    private static List<List<WeightedLink>> getLocalAggrPaths(
            Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {

        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));
        int dstPodId = getPodId(k, getIntValue(dst.getId()));

        if (srcPodId != dstPodId){
            log.error("getLocalAggrPaths: Internal error, srcPodId != dstPodId for src={} dst={}", src, dst);
        }

        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);

        List<WeightedLink> downLinks1 = getLinks(topo, src, dstEdgeSw);
        List<WeightedLink> downLinks2 = getLinks(topo, dstEdgeSw, dst);

        List<List<WeightedLink>> listOfLinks = new ArrayList<List<WeightedLink>>();
        listOfLinks.add(downLinks1);
        listOfLinks.add(downLinks2);

        return mergeLinks(listOfLinks);

    }

    private static List<List<WeightedLink>> getRemoteEdgePaths(
            Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {

        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));
        int dstPodId = getPodId(k, getIntValue(dst.getId()));

        if (srcPodId != dstPodId){
            log.error("getLocalAggrPaths: Internal error, srcPodId != dstPodId for src={} dst={}", src, dst);
        }

        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);


        List<Integer> aggrIds = getAggIdsFromPodId(k, srcPodId);

        List<Switch> aggrs = getSwitches(topo, aggrIds);
        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<WeightedLink> path;

        List<WeightedLink> downLinks2 = getLinks(topo, dstEdgeSw, dst);

        for (Switch aggr : aggrs) {
            List<WeightedLink> upLinks1 = getLinks(topo, src, aggr);
            List<WeightedLink> downLinks1 = getLinks(topo, aggr, dstEdgeSw);
            paths.addAll(mergeLinks(Lists.newArrayList(upLinks1, downLinks1, downLinks2)));
        }

        return paths;
    }

    private static List<List<WeightedLink>> getLocalEdgePaths(
            Graph<Node, WeightedLink> topo, int k, Switch src, Host dst) {
        List<WeightedLink> links = getLinks(topo, src, dst);
        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        for (WeightedLink link : links) {
            List<WeightedLink> path = new ArrayList<WeightedLink>();
            path.add(link);
            paths.add(path);
        }
        return paths;
    }

    private static List<List<WeightedLink>> findSwitchPaths(Graph<Node, WeightedLink> topo, int k, Switch src, Switch dst) {
        if (src.equals(dst)) return null;
        if (src.getType() == TYPE.CORE && dst.getType() == TYPE.CORE) return null;
        int srcPodId = getSwitchPodId(k, getIntValue(src.getId()));
        int dstPodId = getSwitchPodId(k, getIntValue(dst.getId()));

        if (src.getType() == TYPE.AGGREGATION &&
                dst.getType() == TYPE.AGGREGATION &&
                srcPodId == dstPodId) return null;

        if (srcPodId == dstPodId) {
            // Same pod
            if (src.getType() == TYPE.EDGE && dst.getType() == TYPE.EDGE){
                // Edges on same Pod
                return getSamePodEdgeSwitchPaths(topo, k, srcPodId, src, dst);

            } else if (src.getType() == TYPE.EDGE && dst.getType() == TYPE.AGGREGATION ||
                    src.getType() == TYPE.AGGREGATION && dst.getType() == TYPE.EDGE){
                // Edge <-> Aggr
                //TODO
            } else if (src.getType() == TYPE.AGGREGATION && dst.getType() == TYPE.AGGREGATION){
                // Aggr <-X-> Aggr
                return null;
            }
        } else {
            // Different pods
            //TODO
            if (src.getType() == TYPE.EDGE && dst.getType() == TYPE.EDGE){
                //TODO

            } else if (src.getType() == TYPE.EDGE && dst.getType() == TYPE.AGGREGATION){
                //TODO

            } else if (src.getType() == TYPE.AGGREGATION && dst.getType() == TYPE.EDGE){
                //TODO

            } else if (src.getType() == TYPE.AGGREGATION && dst.getType() == TYPE.AGGREGATION){
                //TODO

            }
        }

        return null;
    }

    private static List<List<WeightedLink>> getSamePodEdgeSwitchPaths(
            Graph<Node, WeightedLink> topo, int k, int podId, Switch src, Switch dst) {

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<Integer> aggrIds = getAggIdsFromPodId(k, podId);
        for (Integer aggrId : aggrIds) {
            Switch aggr = getSwitch(topo, aggrId);
            List<WeightedLink> upLinks = getLinks(topo, src, aggr);
            List<WeightedLink> downLinks = getLinks(topo, aggr, dst);

            List<List<WeightedLink>> listOfLinks = new ArrayList<List<WeightedLink>>();
            listOfLinks.add(upLinks);
            listOfLinks.add(downLinks);
            paths.addAll(mergeLinks(listOfLinks));
        }
        return paths;
    }

    private static List<List<WeightedLink>> mergeLinks(
            List<List<WeightedLink>> listOfLinks) {


        if (listOfLinks.size() == 1) {
            List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
            List<WeightedLink> path = null;
            for (List<WeightedLink> links : listOfLinks) {
                for (WeightedLink link : links) {
                    path = new ArrayList<WeightedLink>();
                    path.add(link);
                    paths.add(path);
                }
            }
            return paths;
        } else {
            List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();

            List<WeightedLink> srcList = listOfLinks.get(0);
            List<List<WeightedLink>> partialPathList = mergeLinks(listOfLinks.subList(1, listOfLinks.size()));
            for (WeightedLink link : srcList) {
                for (List<WeightedLink> partialPath : partialPathList) {
                    List<WeightedLink> path = new ArrayList<WeightedLink>();
                    path.add(link);
                    path.addAll(partialPath);
                    paths.add(path);
                }
            }
            return paths;
        }

    }


    private static List<List<WeightedLink>> findHostPaths(Graph<Node, WeightedLink> topo, int k, Node src, Node dst) throws Exception {
        if (src.equals(dst)) return null;

        if (src instanceof Host && dst instanceof Host){
            int srcPodId = getPodId(k, getIntValue(src.getId()));
            int dstPodId = getPodId(k, getIntValue(dst.getId()));

            int srcEdgeId = getEdgeId(k, getIntValue(src.getId()));
            int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));

            List<Integer> srcAggrIds = getAggIds(k, getIntValue(src.getId()));
            List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));

            if (srcPodId == dstPodId){
                // Same Pod
                if (srcEdgeId == dstEdgeId){
                    // Same Edge
                    // Use Edge
                    return getSameEdgePaths(topo, srcEdgeId, src, dst);
                } else {
                    // Different Edges
                    // Use Aggrs
                    return getDifferentEdgePaths(topo, k, srcEdgeId, src, dstEdgeId, dst);
                }
            } else {
                // Different Pods
                // Use Edge+Aggrs+Cores
                return getDifferentPodPaths(topo, k, src, dst);
            }
        }
        return null;
    }

    private static List<List<WeightedLink>> getDifferentPodPaths(Graph<Node, WeightedLink> topo, int k, Node src, Node dst) {
        int srcEdgeId = getEdgeId(k, getIntValue(src.getId()));
        int dstEdgeId = getEdgeId(k, getIntValue(dst.getId()));

        Switch srcEdgeSw = getSwitch(topo, srcEdgeId);
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);

        List<Integer> srcAggrIds = getAggIds(k, getIntValue(src.getId()));
        List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));

        List<Switch> srcAggrs = getSwitches(topo, srcAggrIds);
        List<Switch> dstAggrs = getSwitches(topo, dstAggrIds);

        List<WeightedLink> upLinks1 = getLinks(topo, src, srcEdgeSw);
        List<WeightedLink> downLinks3 = getLinks(topo, dstEdgeSw, dst);

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        for (Switch srcAggr : srcAggrs) {
            for (Switch dstAggr : dstAggrs) {

                List<WeightedLink> upLinks2 = getLinks(topo, srcEdgeSw, srcAggr);
                List<WeightedLink> downLinks2 = getLinks(topo, dstAggr, dstEdgeSw);

                List<Integer> commonCoreIds = getCommonCoreIds(k, getIntValue(srcAggr.getId()), getIntValue(dstAggr.getId()));
                List<WeightedLink> upLinks3;
                List<WeightedLink> downLinks1;
                for (Integer coreId : commonCoreIds) {
                    Switch core = getSwitch(topo, coreId);
                    upLinks3 = getLinks(topo, srcAggr, core);
                    downLinks1 = getLinks(topo, core, dstAggr);

                    paths.addAll(mergeLinks(upLinks1, upLinks2, upLinks3, downLinks1, downLinks2, downLinks3));
                }
            }
        }


        return paths;
    }

    private static List<List<WeightedLink>> getDifferentEdgePaths(Graph<Node, WeightedLink> topo, int k, int srcEdgeId, Node src,
            int dstEdgeId, Node dst) {
        Switch srcEdgeSw = getSwitch(topo, srcEdgeId);
        Switch dstEdgeSw = getSwitch(topo, dstEdgeId);

        List<Integer> srcAggrIds = getAggIds(k, getIntValue(src.getId()));
        List<Integer> dstAggrIds = getAggIds(k, getIntValue(dst.getId()));

        if (!srcAggrIds.equals(dstAggrIds)) {
            log.error("srcAggrIds={} and dstAggrIds={} are not equal", srcAggrIds, dstAggrIds);
            return null;
        }

        List<Switch> aggrs = getSwitches(topo, srcAggrIds);
        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<WeightedLink> path;

        List<WeightedLink> upLinks1 = getLinks(topo, src, srcEdgeSw);
        List<WeightedLink> downLinks2 = getLinks(topo, dstEdgeSw, dst);

        for (Switch aggr : aggrs) {
            List<WeightedLink> upLinks2 = getLinks(topo, srcEdgeSw, aggr);
            List<WeightedLink> downLinks1 = getLinks(topo, aggr, dstEdgeSw);
            paths.addAll(mergeLinks(upLinks1, upLinks2, downLinks1, downLinks2));
        }

        return paths;
    }

    private static Collection<? extends List<WeightedLink>> mergeLinks(
            List<WeightedLink> upLinks1, List<WeightedLink> upLinks2,
            List<WeightedLink> upLinks3, List<WeightedLink> downLinks1,
            List<WeightedLink> downLinks2, List<WeightedLink> downLinks3) {

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<WeightedLink> path;

        for (WeightedLink upLink1 : upLinks1) {
            for (WeightedLink upLink2 : upLinks2) {
                for (WeightedLink upLink3 : upLinks3) {
                    for (WeightedLink downLink1 : downLinks1) {
                        for (WeightedLink downLink2 : downLinks2) {
                            for (WeightedLink downLink3 : downLinks3) {
                                path = new ArrayList<WeightedLink>();
                                path.add(upLink1);
                                path.add(upLink2);
                                path.add(upLink3);
                                path.add(downLink1);
                                path.add(downLink2);
                                path.add(downLink3);
                                paths.add(path);
                            }
                        }
                    }
                }
            }
        }
        return paths;

    }


    private static List<List<WeightedLink>> mergeLinks(List<WeightedLink> upLinks1,
            List<WeightedLink> upLinks2, List<WeightedLink> downLinks1,
            List<WeightedLink> downLinks2) {

        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<WeightedLink> path;

        for (WeightedLink upLink1 : upLinks1) {
            for (WeightedLink upLink2 : upLinks2) {
                for (WeightedLink downLink1 : downLinks1) {
                    for (WeightedLink downLink2 : downLinks2) {
                        path = new ArrayList<WeightedLink>();
                        path.add(upLink1);
                        path.add(upLink2);
                        path.add(downLink1);
                        path.add(downLink2);
                        paths.add(path);
                    }
                }
            }
        }
        return paths;
    }

    private static List<List<WeightedLink>> getSameEdgePaths(Graph<Node,WeightedLink> topo, int edgeId, Node src, Node dst) {
        Switch edgeSw = getSwitch(topo, edgeId);
        List<List<WeightedLink>> paths = new ArrayList<List<WeightedLink>>();
        List<WeightedLink> path;

        for (WeightedLink upLink : topo.getInEdges(edgeSw)) {
            if (topo.getSource(upLink).equals(src)){

                for (WeightedLink downLink : topo.getOutEdges(edgeSw)) {
                    if (topo.getDest(downLink).equals(dst)){
                        path = new ArrayList<WeightedLink>();
                        path.add(upLink);
                        path.add(downLink);
                        paths.add(path);
                    }
                }
            }
        }

        return paths;
    }

    private static List<WeightedLink> getLinks(Graph<Node,WeightedLink> topo, Node src, Node dst) {
        List<WeightedLink> links = new ArrayList<WeightedLink>();
        for (WeightedLink link : topo.getInEdges(dst)) {
            if (topo.getSource(link).equals(src)){
                links.add(link);
            }
        }
        return links;
    }

    private static Map<Integer, Map<String, Node>> nodeIdCache = new HashMap<Integer, Map<String, Node>>();

    private static Node getNodeFromCache(Graph<Node,WeightedLink> topo, String nodeName) {
        if (nodeIdCache.get(topo.hashCode()) == null) return null;
        return nodeIdCache.get(topo.hashCode()).get(nodeName);
    }
    private static void addNodeToCache(Graph<Node,WeightedLink> topo, String nodeName, Node node) {
        Map<String, Node> topoCache = nodeIdCache.get(topo.hashCode());
        if (topoCache == null) {
            topoCache = new HashMap<String, Node>();
        }
        topoCache.put(nodeName, node);
        nodeIdCache.put(topo.hashCode(), topoCache);
    }

    private static List<Switch> getSwitches(Graph<Node,WeightedLink> topo, List<Integer> swIds) {
        List<Switch> switches = new ArrayList<Switch>();
        for (Integer swId : swIds) {
            switches.add(getSwitch(topo, swId));
        }

        //        for (Node node : topo.getVertices()) {
        //            for (Integer swId : swIds) {
        //                String id = "sw"+swId;
        //                if (node instanceof Switch && node.getId().equalsIgnoreCase(id)){
        //                    switches.add((Switch) node);
        //                }
        //            }
        //        }
        return switches;
    }

    private static Switch getSwitch(Graph<Node,WeightedLink> topo, int swId) {
        String id = "sw"+swId;
        Switch cachedNode = (Switch) getNodeFromCache(topo, id);
        if (cachedNode != null) return cachedNode;
        for (Node node : topo.getVertices()) {
            if (node instanceof Switch && node.getId().equalsIgnoreCase(id)){
                addNodeToCache(topo, id, node);
                return (Switch) node;
            }
        }
        return null;
    }

    private static Host getHost(Graph<Node,WeightedLink> topo, int hostId) {
        String id = "host"+hostId;
        for (Node node : topo.getVertices()) {
            if (node instanceof Host && node.getId().equalsIgnoreCase(id)){
                return (Host) node;
            }
        }
        return null;
    }

    private static int getIntValue(String id) {
        return Integer.parseInt(id.replaceAll("[^0-9]+", ""));
    }

    private static int getSwitchPodId(int k, int switchId) {
        if ((switchId - 1 ) < (k*k/4)){
            // Core
            return -1;
        }
        int podId = (switchId - (k*k/4) - 1) / (k);
        return podId;
    }

    private static int getPodId(int k, int hostId) {
        int podId = (hostId - 1) / (k*k/4);
        return podId;
    }

    private static int getEdgeId(int k, int hostId) {
        int podId = getPodId(k, hostId);
        //        int localEdgeIndex = (hostId - 1 - podId * k) / (k/2);
        int localEdgeIndex = ((hostId -1)% (k*k/4)) / (k/2);

        // edgeId = #cores + #sw in previous pods + #aggr in this pod + localIndex
        int edgeId = (k * k / 4) + (podId)*k + k/2 + localEdgeIndex + 1;
        return edgeId;
    }

    private static List<Integer> getAggIds(int k, int hostId) {
        int podId = getPodId(k, hostId);

        int aggrStartId = (k * k / 4) + (podId)*k + 1;
        List<Integer> aggs = new ArrayList<Integer>();
        for (int i = 0; i < k/2; i++) {
            aggs.add(new Integer(aggrStartId+i));
        }

        return aggs;
    }

    private static List<Integer> getCoreIds(int k, int aggrId) {
        // coreId: [1,k)
        //        int localAggrIndex = ((aggrId - (k*k/4) - 1) % (k/2));
        int localAggrIndex = ((aggrId - 1) % (k/2));
        List<Integer> cores = new ArrayList<Integer>();
        for (int i = 0; i < k/2; i++) {
            cores.add(new Integer(localAggrIndex * k/2 + i + 1));
        }
        return cores;
    }

    private static List<Integer> getCommonCoreIds(int k, int srcAggr, int dstAggr) {
        List<Integer> srcCores = getCoreIds(k, srcAggr);
        List<Integer> dstCores = getCoreIds(k, dstAggr);
        List<Integer> commonCores = new ArrayList<Integer>();
        for (Integer srcCore : srcCores) {
            for (Integer dstCore : dstCores) {
                if (srcCore.equals(dstCore)){
                    commonCores.add(srcCore);
                }
            }
        }
        return commonCores;
    }

    private static List<Integer> getAggIdsFromPodId(int k, int podId) {

        int aggrStartId = (k * k / 4) + (podId)*k + 1;
        List<Integer> aggs = new ArrayList<Integer>();
        for (int i = 0; i < k/2; i++) {
            aggs.add(new Integer(aggrStartId+i));
        }

        return aggs;
    }

    public static Graph<Node, WeightedLink> buildTopology(Configs configs, int givenHostId) {
        //        Graph<Node, WeightedLink> topology = new DirectedSparseGraph<Node, WeightedLink>();
        Graph<Node, WeightedLink> topology = new DirectedSparseGraph<Node, WeightedLink>();
        int kPort = Integer.parseInt(configs.getConfig(ConfigName.TOPOLOGY_KPORT));
        double podSensitivity = Double.valueOf(configs.getConfig(ConfigName.LINK_COST_POD_SENSITIVITY));
        double monitoringHostCost = Double.valueOf(configs.getConfig(ConfigName.MONITORING_HOST_COST));
        double switchInitCost = Double.valueOf(configs.getConfig(ConfigName.SWITCH_INIT_COST));
        double switchPerFlowReuseCostRatio = Double.valueOf(configs.getConfig(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO));


        int cores = (int) (Math.pow(kPort, 2)/4);
        int aggrs = (int) (Math.pow(kPort, 2)/2);
        int edges = (int) (Math.pow(kPort, 2)/2);
        int switches = 0;
        int links = 0;
        int hosts = 0;

        Switch[] coreArray = new Switch[cores];

        // Create core switches
        for (int i = 0; i < cores; i++) {
            switches++;
            coreArray[i] = new Switch("sw"+switches, true, TYPE.CORE, switchInitCost, switchPerFlowReuseCostRatio);
            topology.addVertex(coreArray[i]);
        }

        // Create K pods
        for (int i = 0; i < kPort; i++) {

            // Create pod's aggrs
            Switch[] podAggrArray = new Switch[kPort/2];
            for (int j = 0; j < kPort/2; j++) {
                switches++;
                podAggrArray[j] = new Switch("sw"+switches, true, TYPE.AGGREGATION, switchInitCost, switchPerFlowReuseCostRatio);
                topology.addVertex(podAggrArray[j]);

                // Connect Aggr to Cores
                // j*(k/2): # previously filled ports = connected core switches to this pod
                //        : index of first core switch to be connected to this aggr switch
                // (j+1)*(k/2): index of last core switch to be connected to this aggr switch
                for (int k = j*(kPort/2); k < (j+1)*(kPort/2); k++) {
                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), podAggrArray[j], coreArray[k]);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), coreArray[k], podAggrArray[j]);
                }
            }

            // Create pod's edges
            for (int j = 0; j < kPort/2; j++) {
                switches++;
                Switch edgeSw = new Switch("sw"+switches, true, TYPE.EDGE, switchInitCost, switchPerFlowReuseCostRatio);
                topology.addVertex(edgeSw);

                boolean monitoringHostCreated = false;
                // Connect edge to aggrs
                for (int k = 0; k < kPort/2; k++) {
                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), edgeSw, podAggrArray[k]);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), podAggrArray[k], edgeSw);

                    // Create and connect a single Host to Edge
                    hosts++;
                    Host host;
                    if (!monitoringHostCreated){
                        host = new MonitoringHost("host"+hosts, monitoringHostCost);
                        monitoringHostCreated = true;
                    } else {
                        host = new Host("host"+hosts);
                    }
                    host.setPodIndex(i);
                    host.setEdgeIndex(j);
                    host.setHostIndex(hosts);
                    topology.addVertex(host);

                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), edgeSw, host);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), host, edgeSw);
                    if (hosts == givenHostId){
                        System.out.println("Found Host: " + host);
                        System.out.println("Found Edge: " + edgeSw);
                        System.out.println("Found Pod: " + podAggrArray[k]);
                    }
                }

            }
        }
        return topology;
    }

}
