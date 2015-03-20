package no.uis.ux.cipsi.net.monitoringbalancing.domain;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

import no.uis.ux.cipsi.net.monitoringbalancing.app.TopologyManager;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.solver.MonitoringHostStrengthComparator;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.solver.MonitoringSwitchStrengthComparator;
import no.uis.ux.cipsi.net.monitoringbalancing.domain.solver.TrafficFlowDifficultyComparator;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@PlanningEntity(difficultyComparatorClass = TrafficFlowDifficultyComparator.class)
@XStreamAlias("TrafficFlow")
public class TrafficFlow implements Serializable{

    private static final long serialVersionUID = 1L;

    private Node srcNode;
    private Node dstNode;
    private InetAddress srcIp;
    private InetAddress dstIp;
    private Short srcPort;
    private Short dstPort;
    private Short protocol;
    private double rate;    // in bps NOT pps



    private List<WeightedLink> path;

    private Switch monitoringSwitch;
    private MonitoringHost monitoringHost;

    public TrafficFlow() {
        // Clones
    }

    public TrafficFlow(Node srcNode, Node dstNode, InetAddress srcIp,
            InetAddress dstIp, Short srcPort, Short dstPort, Short protocol,
            double rate, List<WeightedLink> path) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.rate = rate;
        this.path = path;
    }
    public Node getSrcNode() {
        return srcNode;
    }
    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;
    }
    public Node getDstNode() {
        return dstNode;
    }
    public void setDstNode(Node dstNode) {
        this.dstNode = dstNode;
    }
    public InetAddress getSrcIp() {
        return srcIp;
    }
    public void setSrcIp(InetAddress srcIp) {
        this.srcIp = srcIp;
    }
    public InetAddress getDstIp() {
        return dstIp;
    }
    public void setDstIp(InetAddress dstIp) {
        this.dstIp = dstIp;
    }
    public Short getSrcPort() {
        return srcPort;
    }
    public void setSrcPort(Short srcPort) {
        this.srcPort = srcPort;
    }
    public Short getDstPort() {
        return dstPort;
    }
    public void setDstPort(Short dstPort) {
        this.dstPort = dstPort;
    }
    public Short getProtocol() {
        return protocol;
    }
    public void setProtocol(Short protocol) {
        this.protocol = protocol;
    }
    public List<WeightedLink> getPath() {
        return path;
    }
    public void setPath(List<WeightedLink> path) {
        this.path = path;
    }
    public double getRate() {
        return rate;
    }
    public void setRate(double rate) {
        this.rate = rate;
    }

    @PlanningVariable(valueRangeProviderRefs = {"trafficFlowOnPathSwitchRange"},
            strengthComparatorClass = MonitoringSwitchStrengthComparator.class)
    public Switch getMonitoringSwitch() {
        return monitoringSwitch;
    }
    public void setMonitoringSwitch(Switch monitoringSwitch) {
        this.monitoringSwitch = monitoringSwitch;
    }

    @ValueRangeProvider(id = "trafficFlowOnPathSwitchRange")
    private List<Switch> getPossibleMonitoringSwitches() {
        return TopologyManager.getInstance().getSwitchesOnPath(path);
    }

    @PlanningVariable(valueRangeProviderRefs = {"monitoringHostRange"},
            strengthComparatorClass = MonitoringHostStrengthComparator.class)
    public MonitoringHost getMonitoringHost() {
        return monitoringHost;
    }
    public void setMonitoringHost(MonitoringHost monitoringHost) {
        this.monitoringHost = monitoringHost;
    }

    @Override
    public String toString() {
        String val = srcNode + "(" + srcIp + ":" + srcPort + ")" + "->" +
                dstNode + "(" + dstIp + ":" + dstPort + ") [Proto: " +
                protocol + ", pathSize: " + path.size() + ", Rate: " + rate + "]";
        return val;
    }

    //    @Override
    //    public int hashCode() {
    //        final int prime = 31;
    //        int result = 1;
    //        result = prime * result + ((dstIp == null) ? 0 : dstIp.hashCode());
    //        result = prime * result + ((dstNode == null) ? 0 : dstNode.hashCode());
    //        result = prime * result + ((dstPort == null) ? 0 : dstPort.hashCode());
    //        result = prime * result
    //                + ((monitoringHost == null) ? 0 : monitoringHost.hashCode());
    //        result = prime
    //                * result
    //                + ((monitoringSwitch == null) ? 0 : monitoringSwitch.hashCode());
    //        result = prime * result + ((path == null) ? 0 : path.hashCode());
    //        result = prime * result
    //                + ((protocol == null) ? 0 : protocol.hashCode());
    //        long temp;
    //        temp = Double.doubleToLongBits(rate);
    //        result = prime * result + (int) (temp ^ (temp >>> 32));
    //        result = prime * result + ((srcIp == null) ? 0 : srcIp.hashCode());
    //        result = prime * result + ((srcNode == null) ? 0 : srcNode.hashCode());
    //        result = prime * result + ((srcPort == null) ? 0 : srcPort.hashCode());
    //        return result;
    //    }
    //    @Override
    //    public boolean equals(Object obj) {
    //        if (this == obj) {
    //            return true;
    //        }
    //        if (obj == null) {
    //            return false;
    //        }
    //        if (!(obj instanceof TrafficFlow)) {
    //            return false;
    //        }
    //        TrafficFlow other = (TrafficFlow) obj;
    //        if (dstIp == null) {
    //            if (other.dstIp != null) {
    //                return false;
    //            }
    //        } else if (!dstIp.equals(other.dstIp)) {
    //            return false;
    //        }
    //        if (dstNode == null) {
    //            if (other.dstNode != null) {
    //                return false;
    //            }
    //        } else if (!dstNode.equals(other.dstNode)) {
    //            return false;
    //        }
    //        if (dstPort == null) {
    //            if (other.dstPort != null) {
    //                return false;
    //            }
    //        } else if (!dstPort.equals(other.dstPort)) {
    //            return false;
    //        }
    //        if (monitoringHost == null) {
    //            if (other.monitoringHost != null) {
    //                return false;
    //            }
    //        } else if (!monitoringHost.equals(other.monitoringHost)) {
    //            return false;
    //        }
    //        if (monitoringSwitch == null) {
    //            if (other.monitoringSwitch != null) {
    //                return false;
    //            }
    //        } else if (!monitoringSwitch.equals(other.monitoringSwitch)) {
    //            return false;
    //        }
    //        if (path == null) {
    //            if (other.path != null) {
    //                return false;
    //            }
    //        } else if (!path.equals(other.path)) {
    //            return false;
    //        }
    //        if (protocol == null) {
    //            if (other.protocol != null) {
    //                return false;
    //            }
    //        } else if (!protocol.equals(other.protocol)) {
    //            return false;
    //        }
    //        if (Double.doubleToLongBits(rate) != Double
    //                .doubleToLongBits(other.rate)) {
    //            return false;
    //        }
    //        if (srcIp == null) {
    //            if (other.srcIp != null) {
    //                return false;
    //            }
    //        } else if (!srcIp.equals(other.srcIp)) {
    //            return false;
    //        }
    //        if (srcNode == null) {
    //            if (other.srcNode != null) {
    //                return false;
    //            }
    //        } else if (!srcNode.equals(other.srcNode)) {
    //            return false;
    //        }
    //        if (srcPort == null) {
    //            if (other.srcPort != null) {
    //                return false;
    //            }
    //        } else if (!srcPort.equals(other.srcPort)) {
    //            return false;
    //        }
    //        return true;
    //    }



}
