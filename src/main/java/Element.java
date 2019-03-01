public class Element {
    private Long nodeID;
    private double priority;
    public Element(Long nodeID, Double priority) {
        this.nodeID = nodeID;
        this.priority = priority;

    }
    public Long getNodeID() {
        return nodeID;
    }
    public void setPriority(double p) {
        priority = p;
    }

    public double getPriority() {
        return priority;
    }
}
