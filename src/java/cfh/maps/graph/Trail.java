package cfh.maps.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Trail {
    
    private final Node from;
    private final Node to;
    private final double cost;
    private final List<Object> path = new ArrayList<Object>();
    
    Trail(Node from, Node to, double cost) {
        assert from != null;
        assert to != null;
        assert cost >= 0 : cost;
        
        this.from = from;
        this.to = to;
        this.cost = cost;
    }
    
    void addPath(Object edge) {
        path.add(edge);
    }
    
    public Node getFrom() {
        return from;
    }
    
    public Node getTo() {
        return to;
    }
    
    public double getCost() {
        return cost;
    }
    
    public List<Object> getPath() {
        return Collections.unmodifiableList(path);
    }
}