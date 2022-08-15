package cfh.maps.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cfh.maps.gui.Point;

class Node {

    private final String id;
    private final Point point;
    
    private final List<Edge> edges = new ArrayList<Edge>();
    
    Node(String id, Point point) {
        if (id == null) throw new IllegalArgumentException("null id");
        if (point == null) throw new IllegalArgumentException("null point");
        this.id = id;
        this.point = point;
    }
    
    String getId() {
        return id;
    }
    
    boolean addEdge(Edge edge) {
        return edges.add(edge);
    }
    
    boolean removeEdge(Edge edge) {
        return edges.remove(edge);
    }

    Point getPoint() {
        return point;
    }
    
    List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    int getEdgeCount() {
        return edges.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return id.equals(other.id) && point.equals(other.point);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode()*37 + point.hashCode();
    }
    
    @Override
    public String toString() {
        return "[" + id + "]";
    }
}
