package no.uis.ux.cipsi.net.monitoringbalancing.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm.NumericPathFinder;
import no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm.YenNoCache;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.TrafficFlow;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;



public class TopologyManager {

    private static Logger log = LoggerFactory.getLogger(TopologyManager.class);
    private static Random random = new Random();

    //    private static final int kPort = 4;

    //    private Graph<Node, WeightedLink> topology;
    //    private Yen<Node, WeightedLink> yenKShortestPathsAlgo;


    //    public void test(){
    //        System.out.println(topology.getVertexCount());
    //        System.out.println(topology.getEdgeCount());
    //        for (WeightedLink link : topology.getEdges()) {
    //            System.out.println(link + "[" + topology.getSource(link) + "->" + topology.getDest(link) + "]");
    //        }
    //        List<List<WeightedLink>> paths = yenKShortestPathsAlgo.getShortestPaths(topology.getVertices().toArray(new Switch[topology.getVertexCount()])[0], topology.getVertices().toArray(new Switch[topology.getVertexCount()])[1], 5);
    //        for (List<WeightedLink> path : paths) {
    //            System.out.println(path);
    //        }
    //    }

    //    public TopologyManager() {
    //        topology = buildTopology(kPort);
    //        yenKShortestPathsAlgo = new Yen<Node, WeightedLink>(topology, weightTransformer);
    //    }


    public static List<WeightedLink> getDeterministicShortestPath(Graph<Node, WeightedLink> topo, Configs configs, Node src, Node dst, int k, TrafficFlow flow) {
        if (src == null || dst == null){
            log.error("getDeterministicShortestPath src={} or dst={} is null", src, dst);
            return null;
        }
        List<List<WeightedLink>> paths = getKShortestPaths(topo, configs, src, dst, k);
        if (paths.size() < k){
            log.trace("getDeterministicShortestPath(src={}, dst={}) #AvailablePaths {} is less than K {}. Choosing #AvailablePaths {}", src, dst, paths.size(), k, Math.min(paths.size(), k));
        }
        int flowHashCode = flowHashCode(flow);
        int deterministicIndex = Math.abs(flowHashCode) % Math.min(paths.size(), k);
        return paths.get(deterministicIndex);

    }

    public static List<WeightedLink> getRandomShortestPath(Graph<Node, WeightedLink> topo, Configs configs, Node src, Node dst, int k){
        //        log.debug("getRandomShortestPath src={} dst={} k={}", src, dst, k);
        if (src == null || dst == null){
            log.error("getRandomShortestPath src={} or dst={} is null", src, dst);
            return null;
        }
        List<List<WeightedLink>> paths = getKShortestPaths(topo, configs, src, dst, k);
        if (paths.size() < k){
            log.trace("getRandomShortestPath(src={}, dst={}) #AvailablePaths {} is less than K {}. Choosing #AvailablePaths {}", src, dst, paths.size(), k, Math.min(paths.size(), k));
        }
        int randomIndex = random.nextInt(Math.min(paths.size(), k));
        return paths.get(randomIndex);
    }


    public static List<WeightedLink> getShortestPath(Graph<Node, WeightedLink> topo, Configs configs, Node src, Node dst){
        //        log.debug("getRandomShortestPath src={} dst={} k={}", src, dst, k);
        if (src == null || dst == null){
            log.error("getRandomShortestPath src={} or dst={} is null", src, dst);
            return null;
        }
        List<List<WeightedLink>> paths = getKShortestPaths(topo, configs, src, dst, 1);
        if (paths.size() < 1){
            log.error("getRandomShortestPath(src={}, dst={}) #AvailablePaths {} is less than 1. returning", src, dst, paths.size());
            return null;
        }
        return paths.get(0);
    }

    public static List<List<WeightedLink>> getKShortestPaths(Graph<Node, WeightedLink> topo, Configs configs, Node src, Node dst, int k){
        //        List<List<WeightedLink>> cachedPaths = AlgoCache.getPaths(yenKShortestPathsAlgo, src, dst);
        //        if (cachedPaths != null && cachedPaths.size() >= k) {
        //            log.trace("getKShortestPaths cache hit");
        //            return cachedPaths.subList(0, k);
        //        }
        //        List<List<WeightedLink>> paths = yenKShortestPathsAlgo.getShortestPaths(src, dst, k);
        //        AlgoCache.mergePaths(yenKShortestPathsAlgo, src, dst, paths);
        if (src == null || dst == null){
            log.error("getRandomShortestPath src={} or dst={} is null", src, dst);
            return null;
        }
        List<List<WeightedLink>> paths = null;
        try {
            int kPort = Integer.parseInt(configs.getConfig(ConfigName.TOPOLOGY_KPORT));
            List<List<WeightedLink>> allPaths = NumericPathFinder.findPath(topo, kPort, src, dst);
            if (allPaths != null) {
                paths = allPaths.subList(0, Math.min(k, allPaths.size()));
            }
        } catch (Exception e) {
            log.error("getKShortestPaths: This should never happen", e);
            return null;
        }
        return paths;
    }

    public static Yen<Node,WeightedLink> buildShortestPathAlgo(Graph<Node, WeightedLink> topology) {
        YenNoCache<Node, WeightedLink> yenKShortestPathsAlgo = new YenNoCache<Node, WeightedLink>(
                topology, new Transformer<WeightedLink, Number>() {
                    @Override
                    public Number transform(WeightedLink link) {
                        return link.getWeight();
                    }
                });
        return yenKShortestPathsAlgo;
    }

    public static Graph<Node, WeightedLink> buildTopology(Configs configs) {
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

                        host.setPodIndex(i);
                        host.setEdgeIndex(j);
                        host.setHostIndex(hosts);
                        topology.addVertex(host);

                        // Create links for monitoringHost with 0% utilization
                        links++;
                        topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost, 0), edgeSw, host);
                        links++;
                        topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost, 0), host, edgeSw);

                        monitoringHostCreated = true;

                    } else {
                        host = new Host("host"+hosts);

                        host.setPodIndex(i);
                        host.setEdgeIndex(j);
                        host.setHostIndex(hosts);
                        topology.addVertex(host);

                        // Create links for host with default utilization
                        links++;
                        topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), edgeSw, host);
                        links++;
                        topology.addEdge(new WeightedLink("link"+links, podSensitivity, switchInitCost), host, edgeSw);
                    }

                }

            }
        }
        return topology;
    }

    public static List<Switch> getSwitchesOnPath(Graph<Node, WeightedLink> topology, List<WeightedLink> path){
        List<Switch> switchesOnPath = new ArrayList<Switch>();
        if (path == null || path.size() == 0) return switchesOnPath;

        for (WeightedLink link : path) {
            Node src = topology.getSource(link);
            if (src instanceof Switch){
                switchesOnPath.add((Switch) src);
            }
        }
        Node lastDst = topology.getDest(path.get(path.size() - 1));
        if (lastDst instanceof Switch){
            switchesOnPath.add((Switch) lastDst);
        }

        return switchesOnPath;
    }

    public static List<Node> getNodesOnPath(Graph<Node, WeightedLink> topology, List<WeightedLink> path){
        List<Node> nodesOnPath = new ArrayList<Node>();
        if (path == null || path.size() == 0) return nodesOnPath;

        for (WeightedLink link : path) {
            Node src = topology.getSource(link);
            nodesOnPath.add(src);
        }
        Node lastDst = topology.getDest(path.get(path.size() - 1));
        nodesOnPath.add(lastDst);

        return nodesOnPath;
    }

    public static boolean isSwitchOnPath(Graph<Node, WeightedLink> topology, List<WeightedLink> path, Switch monitoringSwitch) {
        return getSwitchesOnPath(topology, path).contains(monitoringSwitch);
    }



    public static List<Switch> getMonitoringSwitches(Graph<Node, WeightedLink> topology) {
        List<Switch> switches = new ArrayList<Switch>();
        Collection<Node> nodes = topology.getVertices();
        for (Node node : nodes) {
            if (node instanceof Switch){
                switches.add((Switch) node);
            }
        }
        return switches;
    }

    public static List<Host> getHosts(Graph<Node, WeightedLink> topology, boolean includeMonitoringHosts) {
        List<Host> hosts = new ArrayList<Host>();
        Collection<Node> nodes = topology.getVertices();
        for (Node node : nodes) {
            if (includeMonitoringHosts){
                if (node instanceof Host){
                    hosts.add((Host) node);
                }
            } else {
                if (node instanceof Host && ! (node instanceof MonitoringHost)){
                    hosts.add((Host) node);
                }
            }
        }
        return hosts;
    }

    public static List<MonitoringHost> getMonitoringHosts(Graph<Node, WeightedLink> topology) {
        List<MonitoringHost> hosts = new ArrayList<MonitoringHost>();
        Collection<Node> nodes = topology.getVertices();
        for (Node node : nodes) {
            if (node instanceof MonitoringHost){
                hosts.add((MonitoringHost) node);
            }
        }
        return hosts;
    }

    private static int flowHashCode(TrafficFlow flow) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flow.getDstIp() == null) ? 0 : flow.getDstIp().hashCode());
        result = prime * result + ((flow.getSrcIp() == null) ? 0 : flow.getSrcIp().hashCode());

        result = prime * result + ((flow.getDstNode() == null) ? 0 : flow.getDstNode().hashCode());
        result = prime * result + ((flow.getSrcNode() == null) ? 0 : flow.getSrcNode().hashCode());

        result = prime * result + ((flow.getDstPort() == null) ? 0 : flow.getDstPort().hashCode());
        result = prime * result + ((flow.getSrcPort() == null) ? 0 : flow.getSrcPort().hashCode());

        result = prime * result + ((flow.getProtocol() == null) ? 0 : flow.getProtocol().hashCode());

        long temp;
        temp = Double.doubleToLongBits(flow.getRate());
        result = prime * result + (int) (temp ^ (temp >>> 32));

        return result;
    }

    public static Switch getClosestMonitoringSwitch(Graph<Node, WeightedLink> topology, TrafficFlow flow){
        List<Switch> onPathSwitches = flow.getOnPathMonitoringSwitches();
        Switch candidateSw = null;
        if (onPathSwitches != null && onPathSwitches.size() > 1) {
            candidateSw = onPathSwitches.get(0);
        }
        return candidateSw;
    }

    public static MonitoringHost getClosestMonitoringHost(Graph<Node, WeightedLink> topology, TrafficFlow flow) {
        MonitoringHost monitoringHost = null;
        Switch candidateSw = getClosestMonitoringSwitch(topology, flow);
        if (candidateSw != null && candidateSw.getType().equals(TYPE.EDGE)) {
            // CandidateSw is an Edge, and in each edge there should be a MH
            for (Node node : topology.getNeighbors(candidateSw)) {
                if (node instanceof MonitoringHost){
                    monitoringHost = (MonitoringHost) node;
                    break;
                }
            }
        }
        return monitoringHost;
    }
}
