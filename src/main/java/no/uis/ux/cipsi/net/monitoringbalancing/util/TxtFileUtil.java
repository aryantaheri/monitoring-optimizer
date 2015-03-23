package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxtFileUtil {
    private static Logger log = LoggerFactory.getLogger(TxtFileUtil.class);

    private static final String COMMENTS = "#";

    private static final String SWITCH_FABRIC_CAPACITY = "sw.fabric_capacity";
    private static final String SWITCH_FORWARDING_CAPACITY = "sw.forwarding_capacity";
    private static final String SWITCH_INIT_COST = "sw.init_cost";
    private static final String SWITCH_PERFLOW_REUSE_COST_RATIO = "sw.perflow_reuse_cost_ratio";

    private static final String MONITORING_HOST_COST = "monitoring_host.cost";


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
}
