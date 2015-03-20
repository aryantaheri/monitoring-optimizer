package no.uis.ux.cipsi.net.monitoringbalancing.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.impl.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScoreDefinition;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@PlanningSolution
@XStreamAlias("MonitoringBalance")
public class MonitoringBalance implements Solution<HardSoftBigDecimalScore>, Serializable {

    private static final long serialVersionUID = 1L;

    List<Switch> monitoringSwitches;
    List<MonitoringHost> monitoringHosts;
    List<TrafficFlow> trafficFlows;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftBigDecimalScoreDefinition.class})
    private HardSoftBigDecimalScore score;


    public MonitoringBalance() {
        // For generating clones
    }

    public MonitoringBalance(List<Switch> monitoringSwitches,
            List<MonitoringHost> monitoringHosts, List<TrafficFlow> trafficFlows) {
        super();
        this.monitoringSwitches = monitoringSwitches;
        this.monitoringHosts = monitoringHosts;
        this.trafficFlows = trafficFlows;
    }

    @ValueRangeProvider(id = "monitoringSwitchRange")
    public List<Switch> getMonitoringSwitches() {
        return monitoringSwitches;
    }

    public void setMonitoringSwitches(List<Switch> monitoringSwitches) {
        this.monitoringSwitches = monitoringSwitches;
    }

    @ValueRangeProvider(id = "monitoringHostRange")
    public List<MonitoringHost> getMonitoringHosts() {
        return monitoringHosts;
    }

    public void setMonitoringHosts(List<MonitoringHost> monitoringhosts) {
        this.monitoringHosts = monitoringhosts;
    }

    @PlanningEntityCollectionProperty
    public List<TrafficFlow> getTrafficFlows() {
        return trafficFlows;
    }

    public void setTrafficFlows(List<TrafficFlow> trafficFlows) {
        this.trafficFlows = trafficFlows;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HardSoftBigDecimalScore getScore() {
        return score;
    }

    @Override
    public void setScore(HardSoftBigDecimalScore score) {
        this.score = score;
    }

}
