package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm.FatTreeStateMachine.State;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import edu.uci.ics.jung.graph.Graph;

public class PathFinder {
    public static void main(String[] args) throws Exception {
        TopologyManager topologyManager = new TopologyManager();
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(Configs.getDefaultConfigs());
        PathFinder pathFinder = new PathFinder();

        Collection<Node> nodes = topo.getVertices();
        for (Node node : nodes) {
            if (node instanceof Host){
                if (((Host) node).getId().equalsIgnoreCase("host1")){
                    FatTreeStateMachine stateMachine = new FatTreeStateMachine();
                    stateMachine.resetState(State.H1);
                    pathFinder.traverseGraph(topo, node, null, stateMachine);

                    break;
                }
            }
        }
        int totalPaths = 0;
        int totalUniqSrcDstPaths = 0;
        for (Node node : pathFinder.pathsMap.keySet()) {
            for (Node node2 : pathFinder.pathsMap.get(node).keySet()) {
                List<List<WeightedLink>> paths = pathFinder.pathsMap.get(node).get(node2);
                System.out.println(node + " -> " + node2 + ": ");
                for (List<WeightedLink> path : paths) {
                    System.out.println(pathFinder.prettyPrintPath(topo, path));
                    totalPaths++;
                }
                totalUniqSrcDstPaths++;
                System.out.println();
            }
        }
        System.out.println("Total paths:" + totalPaths);
        System.out.println("Total Uniq Src Dst paths:" + totalUniqSrcDstPaths);
        System.out.println(pathFinder.pathsMap);
    }

    HashMap<Node, HashMap<Node, List<List<WeightedLink>>>> pathsMap = new HashMap<Node, HashMap<Node,List<List<WeightedLink>>>>();


    private void traverseGraph(Graph<Node, WeightedLink> topo, Node src, List<WeightedLink> partialPath, FatTreeStateMachine stateMachine) throws Exception {

        if (partialPath == null){
            partialPath = new ArrayList<WeightedLink>();
        }
        for (WeightedLink outLink : topo.getOutEdges(src)) {
            Node neighbor = topo.getDest(outLink);
            if (!shouldProceed(src, neighbor, stateMachine)){
                System.out.println("Shouldn't proceed from " + src + " -> " + neighbor + " StateMachine: " + stateMachine);
                continue;
            } else {
                stateMachine.moveState(neighbor);
            }
            // TODO: Check!
            // 1) Not parent
            // 2) In same direction
            List<WeightedLink> path = new ArrayList<WeightedLink>();
            path.add(outLink);


            partialPath = appendPath(partialPath, path);
            FatTreeStateMachine sm = new FatTreeStateMachine(stateMachine);
            System.out.println("partial path:" + prettyPrintPath(topo, partialPath));
            traverseGraph(topo, neighbor, partialPath, sm);


        }
        addSubPaths(topo, partialPath);
    }

    private boolean shouldProceed(Node src, Node neighbor, FatTreeStateMachine stateMachine) {
        return stateMachine.isValidMove(neighbor);
    }

    private List<WeightedLink> appendPath(List<WeightedLink> origin, List<WeightedLink> addition) {
        origin.addAll(addition);
        return origin;
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


    private String prettyPrintPath(Graph<Node, WeightedLink> topo, List<WeightedLink> path) {
        StringBuilder p = new StringBuilder();
        for (WeightedLink weightedLink : path) {
            p.append(topo.getSource(weightedLink)).append("-")
            .append(weightedLink).append("-")
            .append(topo.getDest(weightedLink)).append("->");
        }
        return p.toString();
    }



















}
