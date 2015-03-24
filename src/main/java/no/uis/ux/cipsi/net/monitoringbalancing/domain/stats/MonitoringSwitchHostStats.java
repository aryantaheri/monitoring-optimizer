package no.uis.ux.cipsi.net.monitoringbalancing.domain.stats;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import no.uis.ux.cipsi.net.monitoringbalancing.domain.MonitoringHost;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.Switch;

public class MonitoringSwitchHostStats {

    Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap;
    public MonitoringSwitchHostStats(
            Map<Switch, Set<MonitoringHost>> monitoringSwitchHostMap) {
        this.monitoringSwitchHostMap = new TreeMap<Switch, Set<MonitoringHost>>(monitoringSwitchHostMap);
    }

    private String getSwitchMapString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n Switch-Host Mapping:");
        for (Entry<Switch, Set<MonitoringHost>> switchEntry : monitoringSwitchHostMap.entrySet()) {
            builder.append("\n  ").append(switchEntry.getKey()).append(" -> ").append(switchEntry.getValue());
        }
        return builder.toString();
    }

    private String getHostMapString() {
        Map<MonitoringHost, Set<Switch>> hostSwitchMap = getHostSwitchMap();
        StringBuilder builder = new StringBuilder();
        builder.append("\n Host-Switch Mapping:");
        for (Entry<MonitoringHost, Set<Switch>> hostEntry : hostSwitchMap.entrySet()) {
            builder.append("\n  ").append(hostEntry.getKey()).append(" -> ").append(hostEntry.getValue());
        }
        return builder.toString();
    }

    private Map<MonitoringHost, Set<Switch>> getHostSwitchMap() {
        Map<MonitoringHost, Set<Switch>> hostSwitchMap = new TreeMap<MonitoringHost, Set<Switch>>();

        for (Entry<Switch, Set<MonitoringHost>> switchEntry : monitoringSwitchHostMap.entrySet()) {
            for (MonitoringHost host : switchEntry.getValue()) {
                Set<Switch> switches = hostSwitchMap.get(host);
                if (switches == null){
                    switches = new HashSet<Switch>();
                }
                switches.add(switchEntry.getKey());
                hostSwitchMap.put(host, switches);
            }
        }
        return hostSwitchMap;
    }

    private String getSwitchHostMapString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getSwitchMapString())
        .append(getHostMapString());
        return builder.toString();
    }

    @Override
    public String toString() {
        return getSwitchHostMapString();
    }
}
