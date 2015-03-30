package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;

import no.uis.ux.cipsi.net.monitoringbalancing.util.Configs.ConfigName;

import org.apache.commons.lang.text.StrSubstitutor;

public class ConfigUtil {
    //FLOW_RATE=100, MONITORING_HOST_COST=1000, SWITCH_PERFLOW_REUSE_COST_RATIO=0.05, SWITCH_INIT_COST=10, TOPOLOGY_KPORT=48, LINK_COST_POD_SENSITIVITY=1
    static String fileNameTemplate = "k${TOPOLOGY_KPORT}-swinit${SWITCH_INIT_COST}-swreuse${SWITCH_PERFLOW_REUSE_COST_RATIO}-host${MONITORING_HOST_COST}-linkpod${LINK_COST_POD_SENSITIVITY}-rate${FLOW_RATE}.txt";
    public static void main(String[] args) {
        generateConfigFiles("data/monitoringbalancing/unsolved/");

    }
    public static void generateConfigFiles(String dir) {
        ArrayList<Configs> inputs = getInputConfigs();
        for (Configs configs : inputs) {
            generateConfigFile(dir, configs);
        }
    }

    private static void generateConfigFile(String dir, Configs configs) {
        System.out.println(configs.getConfigMapStrings());
        StrSubstitutor sub = new StrSubstitutor(configs.getConfigMapStrings());
        String fileName = sub.replace(fileNameTemplate);
        System.out.println(fileName);

        File input = new File(dir, fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(input)));
            for (Entry<ConfigName, String> item : configs.getConfigMap().entrySet()) {
                writer.print(item.getKey().getName());
                writer.print(" = ");
                writer.print(item.getValue());
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) writer.close();
        }
    }

    //    private static ArrayList<String[]> getInputs(){
    //        ArrayList<String[]> inputs = new ArrayList<String[]>();
    //
    //        //inputs.add(new String[]{  "K",    "swinit",   "swreuse",  "host",     "linkpod,   "rate"});
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "1000",     "1",        "100"});
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "1000",     "1",        "150"});
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "1000",     "1",        "300"});
    //
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "10000",     "1",        "100"});
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "10000",     "1",        "150"});
    //        inputs.add(new String[]{    "48",   "10",       "0.05",     "10000",     "1",        "300"});
    //        return inputs;
    //    }
    private static ArrayList<Configs> getInputConfigs(){
        ArrayList<Configs> inputConfigs = new ArrayList<Configs>();
        //                               "K",    "swinit",   "swreuse",  "host",     "linkpod,   "rate"
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "1000",     "1",        "100000000", "0.001", "0.01", "0.1"));
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "1000",     "1",        "150000000", "0.001", "0.01", "0.1"));
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "1000",     "1",        "300000000", "0.001", "0.01", "0.1"));

        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "10000",     "1",        "100000000", "0.001", "0.01", "0.1"));
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "10000",     "1",        "150000000", "0.001", "0.01", "0.1"));
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "10000",     "1",        "300000000", "0.001", "0.01", "0.1"));

        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "1000",     "10",        "100000000", "0.001", "0.01", "0.1"));
        inputConfigs.add(new Configs(    "48",   "10",       "0.05",     "1000",     "0.1",        "100000000", "0.001", "0.01", "0.1"));
        return inputConfigs;
    }
}
