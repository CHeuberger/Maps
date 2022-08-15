package cfh.maps.graph;

import java.awt.Cursor;
import java.awt.Graphics2D;

import cfh.maps.gui.Point;

public interface Walker {
    
    String getName();

    Cursor getCursor();
    
    void step(GraphPanel panel, Node start, Node node, Edge edge);
    
    void back(GraphPanel panel, Node start, Node node, Edge edge);
    
    void paintNode(Graphics2D gg, Node node, int x, int y);
    
    void paintEdge(Graphics2D gg, Edge edge, Point from, Point to);
}
