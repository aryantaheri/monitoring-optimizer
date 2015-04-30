package no.uis.ux.cipsi.net.monitoringbalancing.app.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Host;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Node;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch.TYPE;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs;
import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class NumericPathFinder {

    public static void main(String[] args) {
        buildTopology(Configs.getDefaultConfigs(), 2);
        int k = 4;
        for (int i = 1; i <= 16; i++) {
            System.out.println("host"+i + ": podId" + getPodId(k, i) + ", edgeId" + getEdgeId(k, i) + ", aggrsId" + getAggIds(k, i));
        }
    }
    private void findPaths() throws Exception {
        Configs cnf = Configs.getDefaultConfigs();
        cnf.putConfig(ConfigName.TOPOLOGY_KPORT, "48");
        Graph<Node, WeightedLink> topo = TopologyManager.buildTopology(cnf);
        PathFinder pathFinder = new PathFinder();

        Collection<Node> nodes = topo.getVertices();
        for (Node node : nodes) {
            if (node instanceof Host){
            }
        }
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

    private static int getPodId(int k, int hostId) {
        int podId = (hostId - 1) / k;
        return podId;
    }

    private static int getEdgeId(int k, int hostId) {
        int podId = getPodId(k, hostId);
        int localEdgeIndex = (hostId - 1 - podId * k) / (k/2);

        int f2 = (hostId - 1) % (k/2);
        int edgeId = (podId + 1)*k + k/2 + localEdgeIndex + 1;
        return edgeId;
    }

    private static List<Integer> getAggIds(int k, int hostId) {
        int podId = getPodId(k, hostId);
        int localAggIndex = (hostId - 1 - podId * k) / (k/2);

        int f2 = (hostId - 1) % (k/2);
        int aggrStartId = (podId + 1)*k + 1;
        List<Integer> aggs = new ArrayList<Integer>();
        for (int i = 0; i < k/2; i++) {
            aggs.add(new Integer(aggrStartId+i));
        }

        return aggs;
    }

}
