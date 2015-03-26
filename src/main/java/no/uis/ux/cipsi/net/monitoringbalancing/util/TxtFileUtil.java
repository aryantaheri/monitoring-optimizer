package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.WeightedLink;
import no.uis.ux.cipsi.net.monitoringbalancing.persistence.MonitoringBalancingGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxtFileUtil {
    private static Logger log = LoggerFactory.getLogger(TxtFileUtil.class);

    // TODO: change to enum
    private static final String COMMENTS = "#";

    public static final String TOPOLOGY_KPORT = "topology.kport";
    private static final String SWITCH_FABRIC_CAPACITY = "sw.fabric_capacity";
    private static final String SWITCH_FORWARDING_CAPACITY = "sw.forwarding_capacity";
    private static final String SWITCH_INIT_COST = "sw.init_cost";
    private static final String SWITCH_PERFLOW_REUSE_COST_RATIO = "sw.perflow_reuse_cost_ratio";

    private static final String MONITORING_HOST_COST = "monitoring_host.cost";

    private static final String LINK_COST_POD_SENSITIVITY = "link.cost_pod_sensitivity";

    private static final String FLOW_RATE = "flow.rate";

    /**
     * Dummy reader
     * @param input
     * @throws IOException
     */
    public static void setStaticValues(File input) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(COMMENTS)) continue;

                String value = line.split("=")[1];
                if (line.startsWith(MONITORING_HOST_COST)){
                    MonitoringHost.setDefaultCost(Double.valueOf(value));

                } else if (line.startsWith(SWITCH_INIT_COST)){
                    Switch.setDefaultInitCost(Double.valueOf(value));
                } else if (line.startsWith(SWITCH_PERFLOW_REUSE_COST_RATIO)){
                    Switch.setDefaultPerFlowReuseCostRatio(Double.valueOf(value));

                } else if (line.startsWith(LINK_COST_POD_SENSITIVITY)){
                    WeightedLink.setDefaultPodSensitivity(Double.valueOf(value));

                } else if (line.startsWith(FLOW_RATE)){
                    MonitoringBalancingGenerator.setDefaultFlowRate(Double.valueOf(value));

                } else if (line.startsWith(TOPOLOGY_KPORT)){
                    //TODO
                }


            }

        } catch (FileNotFoundException e) {
            log.error("setStaticValues ", e);
        } catch (NumberFormatException e) {
            log.error("setStaticValues ", e);
        } catch (IOException e) {
            log.error("setStaticValues ", e);
        }
    }

    public static String getConfig(File input, String name) {
        BufferedReader reader = null;
        String value = null;
        try {
            reader = new BufferedReader(new FileReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(COMMENTS)) continue;

                if (line.startsWith(name)){
                    value = line.split("=")[1];
                    return value;
                }

            }

        } catch (Exception e) {
            log.error("getConfig ", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                log.error("getConfig ", e);
            }
        }
        return null;
    }
}
