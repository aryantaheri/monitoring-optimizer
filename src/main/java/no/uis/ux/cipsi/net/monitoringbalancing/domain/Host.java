package no.uis.ux.cipsi.net.monitoringbalancing.domain;

public class Host extends Node {

    private int hostIndex;
    private int podIndex;
    private int edgeIndex;

    public Host(String id) {
        this.id = id;
    }

    public int getHostIndex() {
        return hostIndex;
    }
    public void setHostIndex(int hostIndex) {
        this.hostIndex = hostIndex;
    }
    public int getPodIndex() {
        return podIndex;
    }
    public void setPodIndex(int podIndex) {
        this.podIndex = podIndex;
    }
    public int getEdgeIndex() {
        return edgeIndex;
    }
    public void setEdgeIndex(int edgeIndex) {
        this.edgeIndex = edgeIndex;
    }

    @Override
    public String toString() {
        return id;
    }
}
