package no.uis.ux.cipsi.net.monitoringbalancing.util;

import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;

public class Configs {

    Map<ConfigName, String> configMap = new HashedMap<ConfigName, String>();

    public enum ConfigName {
        COMMENTS("#"),
        TOPOLOGY_KPORT("topology.kport"),
        SWITCH_FABRIC_CAPACITY("sw.fabric_capacity"),
        SWITCH_FORWARDING_CAPACITY("sw.forwarding_capacity"),
        SWITCH_INIT_COST("sw.init_cost"),
        SWITCH_PERFLOW_REUSE_COST_RATIO("sw.perflow_reuse_cost_ratio"),
        MONITORING_HOST_COST("monitoring_host.cost"),
        LINK_COST_POD_SENSITIVITY("link.cost_pod_sensitivity"),
        FLOW_RATE("flow.rate");

        private String name;
        private ConfigName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    public void putConfig(ConfigName config, String value) {
        configMap.put(config, value.trim());
    }

    public String getConfig(ConfigName config) {
        return configMap.get(config);
    }

    public static Configs getDefaultConfigs(){
        Configs configs = new Configs();
        configs.putConfig(ConfigName.TOPOLOGY_KPORT, "4");
        configs.putConfig(ConfigName.SWITCH_INIT_COST, "10");
        configs.putConfig(ConfigName.SWITCH_PERFLOW_REUSE_COST_RATIO, "0.05");
        configs.putConfig(ConfigName.LINK_COST_POD_SENSITIVITY, "1");
        configs.putConfig(ConfigName.MONITORING_HOST_COST, "1000");
        configs.putConfig(ConfigName.FLOW_RATE, "100000000");
        return configs;
    }
}
