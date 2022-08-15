package cfh.maps.graph;

import static cfh.maps.graph.GraphPanel.*;
import static cfh.maps.graph.GraphPanel.Mode.NONE;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

import cfh.maps.gui.Point;

public class SolveWalker implements Walker {

    private static final Color UNBALANCED_COLOR = Color.RED;
    
    private Solver solver = null;
    private Collection<Node> unbalanced = null;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    @Override
    public String getName() {
        return "Solve";
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }

    @Override
    public void step(GraphPanel panel, Node start, Node node, Edge edge) {
        if (solver == null) {
            solver = createSolver(panel.getGraph());
        }
        if (unbalanced == null) {
            unbalanced = solver.getUnbalanced();
            panel.repaint();
            support.firePropertyChange(PROP_MSG, null, "unbalanced nodes");
            support.firePropertyChange(PROP_TRAIL, null, Integer.toString(unbalanced.size()));
        } else {
            solver.doNormalize();
//            panel.setMode(NONE);
        }
    }
    
    @Override
    public void back(GraphPanel panel, Node start, Node node, Edge edge) {
        support.firePropertyChange(PROP_MSG, null, "");
        if (unbalanced != null) {
            unbalanced = null;
            panel.repaint();
        } else {
            panel.setMode(NONE);
        }
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
        if (unbalanced != null && unbalanced.contains(node)) {
            gg.setColor(UNBALANCED_COLOR);
            GraphPanel.drawMarkedNode(gg, x, y);
        }
    }
    
    @Override
    public void paintEdge(Graphics2D gg, Edge edge, Point from, Point to) {
    }

    public void addChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
