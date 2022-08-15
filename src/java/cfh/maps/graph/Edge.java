package cfh.maps.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cfh.maps.gui.Line;

class Edge {

    private final List<Line> lines = new ArrayList<Line>();
    
    private Node node1;
    private Node node2;
    private double length = 0;
    
    Edge(Line line, Node node1, Node node2) {
        addLine(line);
        this.node1 = node1;
        this.node2 = node2;
    }
    
    void setNode1(Node node) {
        node1 = node;
    }
    
    void setNode2(Node node) {
        node2 = node;
    }
    
    void addLine(Line line) {
        lines.add(line);
        length += line.getLength();
    }

    void addLines(List<Line> list) {
        for (Line line : list) {
            addLine(line);
        }
    }

    Node getNode1() {
        return node1;
    }
    
    Node getNode2() {
        return node2;
    }
    
    List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    double getLength() {
        return length;
    }
    
    @Override
    public String toString() {
        return node1.toString() + "-" + node2.toString();
    }
}
