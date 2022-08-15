package cfh.maps.graph;

import static cfh.maps.graph.GraphPanel.Mode.*;
import static cfh.maps.graph.GraphPanel.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import cfh.maps.gui.Point;

public class ManualWalker implements Walker {

    private static final Color NODE_COLOR = Color.ORANGE;
    private static final Color TRAIL_COLOR = Color.BLUE;
    
    private final LinkedList<Edge> trail = new LinkedList<Edge>();
    private Node lastTrail = null;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    Collection<Edge> getTrail() {
        return Collections.unmodifiableCollection(trail);
    }
    
    @Override
    public String getName() {
        return "Manual";
    }
   
    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public void step(GraphPanel panel, Node start, Node node, Edge edge) {
        if (lastTrail == null) {
            lastTrail = start;
        }
        if (lastTrail == null) {
            lastTrail = node;
            panel.setStart(node);
            support.firePropertyChange(PROP_TRAIL, null, getLength());
        } else {
            Edge found = null;
            if (node != null) {
                if (node != lastTrail) {
                    for (Edge e : lastTrail.getEdges()) {
                        if (e.getNode1() == node || e.getNode2() == node) {
                            found = e;
                            break;
                        }
                    }
                }
            } else if (edge != null) {
                if (lastTrail.getEdges().contains(edge)) {
                    found = edge;
                }
            }
            if (found != null) {
                String old = getLength();
                trail.add(found);
                if (found.getNode1() == lastTrail) {
                    lastTrail = found.getNode2();
                } else {
                    lastTrail = found.getNode1();
                }
                panel.repaint();
                support.firePropertyChange(PROP_TRAIL, old, getLength());
            }
        }
    }
    
    @Override
    public void back(GraphPanel panel, Node start, Node node, Edge edge) {
        if (trail.isEmpty()) {
            panel.setMode(NONE);
        } else {
            String old = getLength();
            Edge removed = trail.removeLast();
            if (trail.isEmpty()) {
                lastTrail = null;
            } else {
                if (lastTrail == removed.getNode2()) {
                    lastTrail = removed.getNode1();
                } else {
                    lastTrail = removed.getNode2();
                }
            }
            panel.repaint();
            support.firePropertyChange(PROP_TRAIL, old, getLength());
        }
    }
    
    private String getLength() {
        double length = 0;
        for (Edge edge : trail) {
            length += edge.getLength();
        }
        return String.format("%.1f", length);
    }

    @Override
    public void paintNode(Graphics2D gg, Node node, int x, int y) {
        if (node == lastTrail) {
            gg.setColor(NODE_COLOR);
            GraphPanel.drawMarkedNode(gg, x, y);
        }
    }
    
    @Override
    public void paintEdge(Graphics2D gg, Edge edge, Point from, Point to) {
        if (trail.contains(edge)) {
            gg.setColor(TRAIL_COLOR);
            gg.setStroke(new BasicStroke(3));
        }
    }

    public void addChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
