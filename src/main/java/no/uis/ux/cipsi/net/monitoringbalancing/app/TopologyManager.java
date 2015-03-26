package no.uis.ux.cipsi.net.monitoringbalancing.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import mulavito.algorithms.shortestpath.ksp.Yen;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;

import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;



public class TopologyManager {

    private static Logger log = LoggerFactory.getLogger(TopologyManager.class);
    private static final int kPort = 4;

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


    public static List<WeightedLink> getRandomShortestPath(Yen<Node, WeightedLink> yenKShortestPathsAlgo, Node src, Node dst, int k){
        //        log.debug("getRandomShortestPath src={} dst={} k={}", src, dst, k);
        List<List<WeightedLink>> paths = yenKShortestPathsAlgo.getShortestPaths(src, dst, k);
        if (paths.size() < k){
            log.trace("getRandomShortestPath(src={}, dst={}) #AvailablePaths {} is less than K {}. Choosing #AvailablePaths {}", src, dst, paths.size(), k, Math.min(paths.size(), k));
        }
        Random random = new Random();
        int randomIndex = random.nextInt(Math.min(paths.size(), k));
        return paths.get(randomIndex);
    }

    public static List<List<WeightedLink>> getKShortestPaths(Yen<Node, WeightedLink> yenKShortestPathsAlgo, Node src, Node dst, int k){
        List<List<WeightedLink>> paths = yenKShortestPathsAlgo.getShortestPaths(src, dst, k);
        return paths;
    }

    public static Yen<Node,WeightedLink> buildShortestPathAlgo(Graph<Node, WeightedLink> topology) {
        Yen<Node, WeightedLink> yenKShortestPathsAlgo = new Yen<Node, WeightedLink>(
                topology, new Transformer<WeightedLink, Number>() {
                    @Override
                    public Number transform(WeightedLink link) {
                        return link.getWeight();
                    }
                });
        return yenKShortestPathsAlgo;
    }

    public static Graph<Node, WeightedLink> buildTopology(int kPort) {
        Graph<Node, WeightedLink> topology = new DirectedSparseGraph<Node, WeightedLink>();

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
            coreArray[i] = new Switch("sw"+switches, true, TYPE.CORE);
            topology.addVertex(coreArray[i]);
        }

        // Create K pods
        for (int i = 0; i < kPort; i++) {

            // Create pod's aggrs
            Switch[] podAggrArray = new Switch[kPort/2];
            for (int j = 0; j < kPort/2; j++) {
                switches++;
                podAggrArray[j] = new Switch("sw"+switches, true, TYPE.AGGREGATION);
                topology.addVertex(podAggrArray[j]);

                // Connect Aggr to Cores
                // j*(k/2): # previously filled ports = connected core switches to this pod
                //        : index of first core switch to be connected to this aggr switch
                // (j+1)*(k/2): index of last core switch to be connected to this aggr switch
                for (int k = j*(kPort/2); k < (j+1)*(kPort/2); k++) {
                    links++;
                    topology.addEdge(new WeightedLink("link"+links), podAggrArray[j], coreArray[k]);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links), coreArray[k], podAggrArray[j]);
                }
            }

            // Create pod's edges
            for (int j = 0; j < kPort/2; j++) {
                switches++;
                Switch edgeSw = new Switch("sw"+switches, true, TYPE.EDGE);
                topology.addVertex(edgeSw);

                boolean monitoringHostCreated = false;
                // Connect edge to aggrs
                for (int k = 0; k < kPort/2; k++) {
                    links++;
                    topology.addEdge(new WeightedLink("link"+links), edgeSw, podAggrArray[k]);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links), podAggrArray[k], edgeSw);

                    // Create and connect a single Host to Edge
                    hosts++;
                    Host host;
                    if (!monitoringHostCreated){
                        host = new MonitoringHost("host"+hosts);
                        monitoringHostCreated = true;
                    } else {
                        host = new Host("host"+hosts);
                    }
                    host.setPodIndex(i);
                    host.setEdgeIndex(j);
                    host.setHostIndex(hosts);
                    topology.addVertex(host);

                    links++;
                    topology.addEdge(new WeightedLink("link"+links), edgeSw, host);
                    links++;
                    topology.addEdge(new WeightedLink("link"+links), host, edgeSw);
                }

            }
        }
        return topology;
    }

    public static List<Switch> getSwitchesOnPath(Graph<Node, WeightedLink> topology, List<WeightedLink> path){
        List<Switch> switchesOnPath = new ArrayList<Switch>();
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

}
