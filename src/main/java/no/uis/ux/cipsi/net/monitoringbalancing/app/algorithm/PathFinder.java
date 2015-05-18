package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm.FatTreeStateMachine.State;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;
import edu.uci.ics.jung.graph.Graph;

public class PathFinder {
    public static void main(String[] args) throws Exception {
        int kPort = 48;
        Configs cnf = Configs.getDefaultConfigs(kPort);
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, ""+kPort);
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);

        new PathFinder().findPaths(topo);
    }

    HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();

    public HashMap<Node,HashMap<Node,List<List<WeightedLink>>>> findPaths(Graph<Node, WeightedLink> topo) throws Exception {
        //        Configs cnf = Configs.getDefaultConfigs();
        //        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, "48");
        //        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);

        Collection<Node> nodes = topo.getVertices();
        for (Node node : nodes) {
            if (node instanceof Host){
                FatTreeStateMachine stateMachine = new FatTreeStateMachine();
                stateMachine.resetState(State.H1);
                traverseGraph(topo, node, null, stateMachine);
            }
            //            if (node instanceof Switch){
            //                if (((Switch) node).getType().equals(TYPE.CORE)){
            //                    FatTreeStateMachine stateMachine = new FatTreeStateMachine();
            //                    stateMachine.resetState(State.C);
            //                    traverseGraph(topo, node, null, stateMachine);
            //                }
            //            }
        }
        writePaths();
        printInfo(topo);
        return pathsMap;
    }


    private void traverseGraph(Graph<Node, WeightedLink> topo, Node src, List<WeightedLink> partialPath, FatTreeStateMachine stateMachine) throws Exception {

        if (partialPath == null){
            partialPath = new ArrayList<WeightedLink>();
        }
        for (WeightedLink outLink : topo.getOutEdges(src)) {
            System.out.println("-------------------------------------------");
            System.out.println("Sw on Path: " + prettyPrintSwitchOnPath(topo, partialPath));
            System.out.println("StateMachine: " + stateMachine);
            Node neighbor = topo.getDest(outLink);
            if (!shouldProceed(topo, src, neighbor, partialPath, stateMachine)){
                System.out.println("Shouldn't proceed from " + src + " -> " + neighbor + " StateMachine: " + stateMachine);
                continue;
            } else {
                FatTreeStateMachine sm = new FatTreeStateMachine(stateMachine);
                sm.moveState(neighbor);
                System.out.println("Proceed from " + src + " -> " + neighbor + " New StateMachine: " + sm);
                List<WeightedLink> path = new ArrayList<WeightedLink>();
                path.add(outLink);


                List<WeightedLink> newPartialPath = appendPath(partialPath, path);
                System.out.println("partial path after move:" + prettyPrintPath(topo, newPartialPath));
                System.out.println("StateMachine after move: " + sm);
                traverseGraph(topo, neighbor, newPartialPath, sm);
            }

        }
        addSubPaths(topo, partialPath);
    }

    private boolean shouldProceed(Graph<Node,WeightedLink> topo, Node src, Node neighbor, List<WeightedLink> partialPath, FatTreeStateMachine stateMachine) {
        if (TopologyManager.getNodesOnPath(topo, partialPath).contains(neighbor)) return false;
        return stateMachine.isValidMove(neighbor);
    }

    private List<WeightedLink> appendPath(List<WeightedLink> origin, List<WeightedLink> addition) {
        List<WeightedLink> newPath = new ArrayList<WeightedLink>(origin);
        newPath.addAll(addition);
        return newPath;
    }

    private void addSubPaths(Graph<Node, WeightedLink> topo, List<WeightedLink> path) {
        for (int i = 0; i < path.size(); i++) {
            for (int j = i; j < path.size(); j++) {
                Node src = topo.getSource(path.get(i));
                Node dst = topo.getDest(path.get(j));

                addPath(src, dst, path.subList(i, j + 1));
            }
        }
    }

    private void addPath(Node src, Node dst, List<WeightedLink> path) {
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



    private void findCores(Graph<Node, WeightedLink> topo) {
        Collection<Node> nodes = topo.getVertices();
        for (Node node : nodes) {
            if (node instanceof Switch){
                if (((Switch) node).getType().equals(TYPE.CORE)){
                    findAggregations(topo, (Switch)node);
                }
            }
        }

    }

    private void findAggregations(Graph<Node,WeightedLink> topo, Switch coreSw) {
        Collection<Node> aggrSwiches = topo.getNeighbors(coreSw);
        for (Node aggrSw : aggrSwiches) {
            findEdges(topo, aggrSw);
        }
    }

    private void findEdges(Graph<Node,WeightedLink> topo, Node aggrSw) {
        Collection<Node> neighbors = topo.getNeighbors(aggrSw);
        for (Node neighbor : neighbors) {
            if ((neighbor instanceof Switch) && (((Switch) neighbor).getType().equals(TYPE.EDGE))){
                findHosts(topo, neighbor);
            }
        }
    }

    private void findHosts(Graph<Node, WeightedLink> topo, Node edgeSw) {
        Collection<Node> neighbors = topo.getNeighbors(edgeSw);
        for (Node neighbor : neighbors) {
            if (!(neighbor instanceof Switch)){
                // Host
                //                addNeighborPath(topo, edgeSw, neighbor);
            }
        }
    }


    public static String prettyPrintPaths(Graph<Node, WeightedLink> topo, List<List<WeightedLink>> paths) {
        StringBuilder out = new StringBuilder();
        for (List<WeightedLink> path : paths) {
            out.append(prettyPrintPath(topo, path));
        }
        return out.toString();
    }

    public static String prettyPrintPath(Graph<Node, WeightedLink> topo, List<WeightedLink> path) {
        StringBuilder p = new StringBuilder();
        for (WeightedLink weightedLink : path) {
            p.append(topo.getSource(weightedLink)).append("-")
            .append(weightedLink).append("-")
            .append(topo.getDest(weightedLink)).append("->");
        }
        return p.toString();
    }

    public static String prettyPrintSwitchOnPath(Graph<Node, WeightedLink> topo, List<WeightedLink> path) {
        StringBuilder p = new StringBuilder();
        for (WeightedLink weightedLink : path) {
            p.append(topo.getSource(weightedLink)).append("-")
            //            .append(weightedLink).append("-")
            .append(topo.getDest(weightedLink)).append("->");
        }
        return p.toString();
    }


    private List<Node> getAllHosts(Graph<Node, WeightedLink> topo) {
        List<Node> hosts = new ArrayList<Node>();
        Collection<Node> nodes = topo.getVertices();
        for (Node node : nodes) {
            if (node instanceof Host){
                hosts.add(node);
            }
        }
        return hosts;
    }



    public String printPathsInCsv() {
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
        writeToFile(out.toString());
        return out.toString();
    }

    private void writeToFile(String out) {
        PrintWriter writer;
        try {
            writer = new PrintWriter("/tmp/paths.txt");
            writer.write(out);
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void printInfo(Graph<Node,WeightedLink> topo) {
        int totalPaths = 0;
        int totalUniqSrcDstPaths = 0;
        List<Node> dstList = new ArrayList<Node>();
        for (Node node : pathsMap.keySet()) {
            for (Node node2 : pathsMap.get(node).keySet()) {
                if (!dstList.contains(node2)) dstList.add(node2);
                List<List<WeightedLink>> paths = pathsMap.get(node).get(node2);
                System.out.println(node + " -> " + node2 + ": ");
                for (List<WeightedLink> path : paths) {
                    System.out.println(prettyPrintPath(topo, path));
                    totalPaths++;
                }
                if (node.equals(node2)){
                    System.out.println("Ooops: " + node + " -> " + node2);
                }
                totalUniqSrcDstPaths++;
                System.out.println();
            }
        }
        System.out.println("Hosts [" + getAllHosts(topo).size() + "] " + getAllHosts(topo));
        System.out.println("Total paths:" + totalPaths);
        System.out.println("Total Uniq Src Dst paths:" + totalUniqSrcDstPaths);
        System.out.println("Source nodes[" + pathsMap.keySet().size() + "] " + pathsMap.keySet());
        System.out.println("Destination nodes[" + dstList.size() + "] " + dstList);

        System.out.println(printPathsInCsv());
    }

    private void writePaths() {

    }







}
