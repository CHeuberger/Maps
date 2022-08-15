package cfh.maps.graph;

import static cfh.maps.graph.GraphPanel.Mode.NONE;
import static cfh.maps.graph.GraphPanel.Mode.WALKER;
import static java.lang.Math.abs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JPanel;

import cfh.maps.gui.Line;
import cfh.maps.gui.Point;

class GraphPanel extends JPanel {

    public static final String PROP_MODE = "MODE";
    public static final String PROP_MSG = "MESSAGE";
    public static final String PROP_TRAIL = "TRAIL";
    
    enum Mode {
        NONE, SET_START, WALKER;
    }
    private static final int POINTDIST = 10;
    private static final int EDGEDIST = 5;
    
    private static final Color START_COLOR = Color.ORANGE;
    
    private final Graph graph;
    private final Image image;
    
    private Mode mode;
    private Walker walker = null;
    
    private Node startNode = null;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    GraphPanel(Graph graph, Image image) {
        if (graph == null) throw new IllegalArgumentException("null graph");
        this.graph = graph;
        this.image = image;
        
        setMode(NONE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    doLeftClicked(e);
                } else if (e.getButton() == MouseEvent.BUTTON3)
                    doRightClicked(e);
            }
        });
    }

    Graph getGraph() {
        return graph;
    }

    void setMode(Mode mode) {
        Mode old = this.mode;
        this.mode = mode;
        if (mode != WALKER) {
            walker = null;
        }
        Cursor cursor = null;
        switch (mode) {
            case SET_START: 
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR); 
                break;
            case WALKER:
                cursor = walker.getCursor();
                break;
            default:
                System.err.println("unhandled mode: " + mode);
                //$FALL-THROUGH$
            case NONE: 
                cursor = Cursor.getDefaultCursor(); 
                break;
        }
        if (cursor == null) {
            cursor = Cursor.getDefaultCursor();
        }
        setCursor(cursor);
        repaint();
        support.firePropertyChange(PROP_MODE, old, mode);
    }
    
    void setWalker(Walker walker) {
        this.walker = walker;
        setMode(walker != null ? WALKER : NONE);
        support.firePropertyChange(PROP_MODE, null, walker);
    }
    
    void setStart(Node node) {
        startNode = node;
        repaint();
    }
    
    private void doLeftClicked(MouseEvent e) {
        Node node;
        switch (mode) {
            case NONE:
                break;
            case SET_START:
                node = graph.findNode(e.getX(), e.getY(), POINTDIST);
                if (node != startNode) {
                    setStart(node);
                }
                setMode(NONE);
                break;
            case WALKER:
                node = graph.findNode(e.getX(), e.getY(), POINTDIST);
                Edge edge = graph.findEdge(e.getX(), e.getY(), EDGEDIST);
                walker.step(this, startNode, node, edge);
                break;
        }
    }

    private void doRightClicked(MouseEvent e) {
        switch (mode) {
            case NONE:
                break;
            case SET_START:
                startNode = null;
                repaint();
                setMode(NONE);
                break;
            case WALKER:
                Node node = graph.findNode(e.getX(), e.getY(), POINTDIST);
                Edge edge = graph.findEdge(e.getX(), e.getY(), EDGEDIST);
                walker.back(this, startNode, node, edge);
                break;
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (image != null)
            return new Dimension(image.getWidth(this), image.getHeight(this));
        else
            return super.getPreferredSize();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gg = (Graphics2D) g.create();
        try {
            Composite composite = gg.getComposite();
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
            gg.drawImage(image, 0, 0, this);
            gg.setComposite(composite);

            gg.setColor(Color.GRAY.darker());
            
            for (Edge edge : graph.getEdges()) {
                drawEdge(gg, edge);
            }

            for (Node node : graph.getNodes()) {
                drawNode(gg, node);
            }
        } finally {
            gg.dispose();
        }
    }

    private void drawNode(Graphics2D gg, Node node) {
        Point point = node.getPoint();
        int x = point.getX();
        int y = point.getY();
        
        Graphics2D tmp = (Graphics2D) gg.create();
        if (mode == WALKER) {
            walker.paintNode(tmp, node, x, y);
        }
        tmp.fillOval(x-3, y-3, 6, 6);
        tmp.dispose();
        
        gg.drawString(node.getId(), x-6, y-6);
        
        if (node == startNode) {
            drawFlag(gg, x, y, START_COLOR);
        }
    }
    
    private void drawEdge(Graphics2D gg, Edge edge) {
        for (Line line : edge.getLines()) {
            Point from = line.getFrom();
            Point to = line.getTo();

            Graphics2D tmp = (Graphics2D) gg.create();
            if (mode == WALKER) {
                walker.paintEdge(tmp, edge, from, to);
            }
            tmp.drawLine(from.getX(), from.getY(), to.getX(), to.getY());
            tmp.dispose();
        
            double dist = line.getLength();
            String txt = String.format("%.0f", dist);
            if (abs(from.getX()-to.getX()) > abs(from.getY()-to.getY())) {
                gg.drawString(txt, (from.getX()+to.getX())/2-16, (from.getY()+to.getY())/2-6);
            } else {
                gg.drawString(txt, (from.getX()+to.getX())/2+6, (from.getY()+to.getY())/2+4);
            }
        }
    }
    
    static void drawMarkedNode(Graphics2D gg, int x, int y) {
        Stroke tmp = gg.getStroke();
        gg.setStroke(new BasicStroke(3));
        gg.drawOval(x-7, y-7, 14, 14);
        gg.setStroke(tmp);
    }
    
    static void drawFlag(Graphics2D gg, int x, int y, Color color) {
        Color tmp = gg.getColor();
        gg.setColor(color);
        gg.drawLine(x, y, x+4, y-20);
        Polygon pol = new Polygon();
        pol.addPoint(x+4, y-20);
        pol.addPoint(x+14, y-15);
        pol.addPoint(x+2, y-10);
        gg.fillPolygon(pol);
        gg.setColor(tmp);
    }

    public void addGraphChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeGraphChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
