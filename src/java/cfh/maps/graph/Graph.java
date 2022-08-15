package cfh.maps.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cfh.maps.gui.Line;
import cfh.maps.gui.Map;
import cfh.maps.gui.Point;

public class Graph {

    private final Set<Node> nodes = new HashSet<Node>();
    private final Set<Edge> edges = new HashSet<Edge>();
    
    public static Graph create(Map map) {
        char[] id = {'A', 'A'};
        
        Graph graph = new Graph();
        java.util.Map<Point, Node> nodeMap = new HashMap<Point, Node>();
        for (Line line : map.getLines()) {
            Point point;
            
            point = line.getFrom();
            Node node1 = nodeMap.get(point);
            if (node1 == null) {
                node1 = graph.createNode(new String(id), point);
                nodeMap.put(point, node1);
                for (int i = id.length-1; i >= 0; i--) {
                    if (++id[i] <= 'Z')
                        break;
                    id[i] = 'A';
                }
            }
            
            point = line.getTo();
            Node node2 = nodeMap.get(point);
            if (node2 == null) {
                node2 = graph.createNode(new String(id), point);
                nodeMap.put(point, node2);
                for (int i = id.length-1; i >= 0; i--) {
                    if (++id[i] <= 'Z')
                        break;
                    id[i] = 'A';
                }
            }
            
            graph.createEdge(line, node1, node2);
        }
        return graph;
    }
    
    Node createNode(String id, Point point) {
        Node node = new Node(id, point);
        nodes.add(node);
        return node;
    }
    
    Edge createEdge(Line line, Node node1, Node node2) {
        Edge edge = new Edge(line, node1, node2);
        edges.add(edge);
        node1.addEdge(edge);
        node2.addEdge(edge);
        return edge;
    }
    
    void removeNode(Node node) {
        nodes.remove(node);
    }
    
    void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes);
    }
    
    Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(edges); 
    }

    Node findNode(int x, int y, int minDistance) {
        Node found = null;
        double dist = minDistance;
        for (Node node : nodes) {
            Point point = node.getPoint();
            double d = point.distanceTo(x, y);
            if (d <= dist) {
                found = node;
                dist = d;
            }
        }
        return found;
    }
    
    Edge findEdge(int x, int y, int minDistance) {
        Edge found = null;
        double dist = minDistance;
        for (Edge edge : edges) {
            for (Line line : edge.getLines()) {
                double d = line.distanceTo(x, y);
                if (d <= dist) {
                    found = edge;
                    dist = d;
                }
            }
        }
        return found;
    }
    
    void compact() {
        Set<Node> dead = new HashSet<Node>();
        for (Node node : nodes) {
            switch (node.getEdgeCount()) {
                case 0:
                    assert node.getEdgeCount() == 0 : node.getEdgeCount();
                    dead.add(node);
                    break;
                case 2:
                    List<Edge> nodeEdges = node.getEdges();
                    assert nodeEdges.size() == 2 : nodeEdges.size();
                    Edge e1 = nodeEdges.get(0);
                    Edge e2 = nodeEdges.get(1);
                    Node n = (e2.getNode1() == node) ? e2.getNode2() : e2.getNode1();
                    if (n.removeEdge(e2)) {
                        e1.addLines(e2.getLines());
                        if (e1.getNode1() == node) {
                            e1.setNode1(n);
                        } else {
                            e1.setNode2(n);
                        }
                        n.addEdge(e1);
                        edges.remove(e2);
                        dead.add(node);
                    }
                    break;
                default:  // do nothing 
                    break;
            }
        }
        for (Node node : dead) {
            nodes.remove(node);
        }
    }
}
