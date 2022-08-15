package cfh.maps.graph;

import static cfh.maps.graph.GraphPanel.PROP_TRAIL;
import static cfh.maps.graph.GraphPanel.Mode.NONE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import cfh.maps.gui.Point;

public class CostWalker implements Walker {

    private static final Color FLAG_COLOR = Color.GREEN.darker();
    private static final Color TRAIL_COLOR = Color.BLUE;
    
    private Solver solver = null;
    private Trail trail = null;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    @Override
    public String getName() {
        return "Cost";
    }
    
    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public void step(GraphPanel panel, Node start, Node node, Edge edge) {
        if (node == null) 
            return;
        if (solver == null) {
            solver = createSolver(panel.getGraph());
        }
        if (start == null) {
            panel.setStart(node);
            support.firePropertyChange(PROP_TRAIL, null, getLength());
        } else {
            String old = getLength();
            trail = solver.getTrail(start, node);
            panel.repaint();
            support.firePropertyChange(PROP_TRAIL, old, getLength());
        }
    }
    
    @Override
    public void back(GraphPanel panel, Node start, Node node, Edge edge) {
        String old = getLength();
        if (trail == null) {
            panel.setMode(NONE);
        } else {
            trail = null;
            panel.repaint();
            support.firePropertyChange(PROP_TRAIL, old, getLength());
        }
    }
    
    private String getLength() {
        double length = (trail != null) ? trail.getCost() : 0;
        return String.format("%.1f", length);
    }
    
    private static Solver createSolver(Graph graph) {
        Solver result = new Solver(graph.getNodes().toArray(new Node[0]));
        for (Edge edge : graph.getEdges()) {
            result.addEdge(edge, edge.getNode1(), edge.getNode2(), edge.getLength());
        }
        return result;
    }


    @Override
    public void paintNode(Graphics2D gg, Node node, int x, int y) {
        if (trail != null && node == trail.getTo()) {
            GraphPanel.drawFlag(gg, x, y, FLAG_COLOR);
        }
    }
    
    @Override
    public void paintEdge(Graphics2D gg, Edge edge, Point from, Point to) {
        if (trail != null) {
            List<Object> path = trail.getPath();
            if (path.contains(edge)) {
                gg.setColor(TRAIL_COLOR);
                gg.setStroke(new BasicStroke(3));
            }
        }
    }

    public void addChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
