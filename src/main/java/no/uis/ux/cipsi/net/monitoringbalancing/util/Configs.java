package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class Configs {

    Map<ConfigName, String> configMap = new TreeMap<ConfigName, String>();

    public enum ConfigName {
        COMMENTS("#"),
        TOPOLOGY_KPORT("topology.kport"),
        SWITCH_FABRIC_CAPACITY("sw.fabric_capacity"),
        SWITCH_FORWARDING_CAPACITY("sw.forwarding_capacity"),
        SWITCH_INIT_COST("sw.init_cost"),
        SWITCH_PERFLOW_REUSE_COST_RATIO("sw.perflow_reuse_cost_ratio"),
        MONITORING_HOST_COST("monitoring_host.cost"),
        LINK_COST_POD_SENSITIVITY("link.cost_pod_sensitivity"),
        FLOW_RATE("flow.rate"),
        FLOW_INTER_POD_PROB("flow.inter_pod_probability"),
        FLOW_INTRA_POD_PROB("flow.intra_pod_probability"),
        FLOW_INTRA_EDGE_PROB("flow.intra_edge_probability");

        private String name;
        private ConfigName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    public Configs() {
    }

    public Configs(Map<ConfigName, String> configMap) {
        this.configMap = configMap;
    }

    public Configs(String k, String swinit, String swreuse, String host, String linkpod, String rate, String interpodProb, String intrapodProb, String intraedgeProb) {
        configMap.put(ConfigName.TOPOLOGY_KPORT, k);
        configMap.put(ConfigName.SWITCH_INIT_COST, swinit);
        configMap.put(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO, swreuse);
        configMap.put(ConfigName.LINK_COST_POD_SENSITIVITY, linkpod);
        configMap.put(ConfigName.MONITORING_HOST_COST, host);
        configMap.put(ConfigName.FLOW_RATE, rate);
        configMap.put(ConfigName.FLOW_INTER_POD_PROB, interpodProb);
        configMap.put(ConfigName.FLOW_INTRA_POD_PROB, intrapodProb);
        configMap.put(ConfigName.FLOW_INTRA_EDGE_PROB, intraedgeProb);

    }

    public void putConfig(ConfigName config, String value) {
        configMap.put(config, value.trim());
    }

    public String getConfig(ConfigName config) {
        return configMap.get(config);
    }

    public Map<ConfigName, String> getConfigMap() {
        return configMap;
    }

    public Map<String, String> getConfigMapStrings() {
        Map<String, String> stringMap = new TreeMap<String, String>();
        for (Entry<ConfigName, String> entry : configMap.entrySet()) {
            stringMap.put(entry.getKey().toString(), entry.getValue());
        }
        return stringMap;
    }

    public static Configs getDefaultConfigs(){
        Configs configs = new Configs();
        configs.putConfig(ConfigName.TOPOLOGY_KPORT, "4");
        configs.putConfig(ConfigName.SWITCH_INIT_COST, "10");
        configs.putConfig(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO, "0.05");
        configs.putConfig(ConfigName.LINK_COST_POD_SENSITIVITY, "1");
        configs.putConfig(ConfigName.MONITORING_HOST_COST, "1000");
        configs.putConfig(ConfigName.FLOW_RATE, "100000000");
        configs.putConfig(ConfigName.FLOW_INTER_POD_PROB, "0.01");
        configs.putConfig(ConfigName.FLOW_INTRA_POD_PROB, "0.01");
        configs.putConfig(ConfigName.FLOW_INTRA_EDGE_PROB, "0.01");
        return configs;
    }
    @Override
    public String toString() {
        return configMap.toString();
    }

    public static Map<String, String> getWildCardMapStrings() {
        Configs cnf = getDefaultConfigs();
        Map<String, String> stringMap = new TreeMap<String, String>();
        for (Entry<ConfigName, String> entry : cnf.getConfigMap().entrySet()) {
            stringMap.put(entry.getKey().toString(), "*");
        }
        return stringMap;
    }
}
