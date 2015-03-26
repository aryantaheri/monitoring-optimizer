package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxtFileUtil {
    private static Logger log = LoggerFactory.getLogger(TxtFileUtil.class);

    // TODO: change to enum
    //    private static final String COMMENTS = "#";
    //
    //    public static final String TOPOLOGY_KPORT = "topology.kport";
    //    private static final String SWITCH_FABRIC_CAPACITY = "sw.fabric_capacity";
    //    private static final String SWITCH_FORWARDING_CAPACITY = "sw.forwarding_capacity";
    //    private static final String SWITCH_INIT_COST = "sw.init_cost";
    //    private static final String SWITCH_PERFLOW_REUSE_COST_RATIO = "sw.perflow_reuse_cost_ratio";
    //
    //    private static final String MONITORING_HOST_COST = "monitoring_host.cost";
    //
    //    private static final String LINK_COST_POD_SENSITIVITY = "link.cost_pod_sensitivity";
    //
    //    private static final String FLOW_RATE = "flow.rate";


    /**
     * Dummy reader
     * @param input
     * @return
     * @throws IOException
     */
    public static Configs getConfigurations(File input) {
        BufferedReader reader = null;
        Configs configs = new Configs();
        try {
            reader = new BufferedReader(new FileReader(input));
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(ConfigName.COMMENTS.getName())) continue;

                String value = line.split("=")[1];
                if (line.startsWith(ConfigName.MONITORING_HOST_COST.getName())){
                    configs.putConfig(ConfigName.MONITORING_HOST_COST, value);

                } else if (line.startsWith(ConfigName.SWITCH_INIT_COST.getName())){
                    configs.putConfig(ConfigName.SWITCH_INIT_COST, value);

                } else if (line.startsWith(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO.getName())){
                    configs.putConfig(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO, value);

                } else if (line.startsWith(ConfigName.LINK_COST_POD_SENSITIVITY.getName())){
                    configs.putConfig(ConfigName.LINK_COST_POD_SENSITIVITY, value);

                } else if (line.startsWith(ConfigName.FLOW_RATE.getName())){
                    configs.putConfig(ConfigName.FLOW_RATE, value);

                } else if (line.startsWith(ConfigName.TOPOLOGY_KPORT.getName())){
                    configs.putConfig(ConfigName.TOPOLOGY_KPORT, value);
                }

            }

        } catch (Exception e) {
            log.error("getConfigurations ", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                log.error("getConfigurations ", e);
            }
        }
        return configs;
    }

    public static String getConfig(File input, ConfigName name) {
        BufferedReader reader = null;
        String value = null;
        try {
            reader = new BufferedReader(new FileReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith(name.getName())){
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
