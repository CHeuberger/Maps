package cfh.maps.gui;

import static java.awt.Cursor.CROSSHAIR_CURSOR;
import static java.lang.Math.abs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JPanel;

class MapPanel extends JPanel {

    private static final int POINTDIST = 10;
    private static final int LINEDIST = 5;
    
    enum Mode {
        NONE, POINTS, LINES;
    }

    private Map map;
    
    private Mode mode = Mode.NONE;
    
    private Point pressedPoint = null;
    private Point lastLine = null;

    private final PropertyChangeListener mapListener;
    
    MapPanel(Map map) {
        mapListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                mapChanged(e);
            }
        };
        setMap(map);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    doLeftPressed(e);
                } else {
                    pressedPoint = null;
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    doLeftClicked(e);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    doRightClicked(e);
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                doMouseDragged(e);
            }
        });
    }

    void setMap(Map map) {
        if (map == null) throw new IllegalArgumentException("null map");
        if (this.map != null) {
            this.map.removePropertyChangeListener(mapListener);
        }
        this.map = map;
        this.map.addPropertyChangeListener(mapListener);
        revalidate();
        repaint();
    }

    private void mapChanged(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Map.PROP_IMAGE)) {
            revalidate();
        }
        repaint();
    }

    private void doLeftClicked(MouseEvent e) {
        switch (mode) {
            case POINTS:
                map.addPoint(new Point(e.getPoint()));
                break;
            case LINES:
                Point point = map.findPoint(e.getPoint().x, e.getPoint().y, 2 * POINTDIST);
                if (point != null) {
                    if (point != lastLine) {
                        if (lastLine != null) {
                            map.addLine(new Line(lastLine, point));
                        }
                        lastLine = point;
                    } else {
                        lastLine = null;
                    }
                    repaint();
                }
                break;
            default:
                break;
        }
    }

    private void doRightClicked(MouseEvent e) {
        switch (mode) {
            case POINTS:
                Point point = map.findPoint(e.getPoint().x, e.getPoint().y, POINTDIST);
                if (point != null) {
                    for (Line line : new ArrayList<Line>(map.getLines())) {
                        if (line.getFrom() == point || line.getTo() == point) {
                            map.removeLine(line);
                        }
                    }
                    map.removePoint(point);
                }
                break;
            case LINES:
                lastLine = null;
                Line line = map.findLine(e.getPoint().x, e.getPoint().y, LINEDIST);
                if (line != null) {
                    map.removeLine(line);
                }
                repaint();
                break;
            default:
                break;
        }
    }

    private void doLeftPressed(MouseEvent e) {
        pressedPoint = map.findPoint(e.getPoint().x, e.getPoint().y, POINTDIST);
    }

    private void doMouseDragged(MouseEvent e) {
        if (pressedPoint == null)
            return;
        
        switch (mode) {
            case POINTS:
            case LINES:
                pressedPoint.setX(e.getPoint().x);
                pressedPoint.setY(e.getPoint().y);
                break;
//                int dx = e.getPoint().x - pressedPoint.getX();
//                int dy = e.getPoint().y - pressedPoint.getY();
//                List<Line> lines = map.getLines();
//                Set<Point> points = new HashSet<Point>();
//                for (Line line : lines) {
//                    if (line.getFrom() == pressedPoint) {
//                        points.add(line.getTo());
//                    } else if (line.getTo() == pressedPoint) {
//                        points.add(line.getFrom());
//                    }
//                }
//                points.add(pressedPoint);
//                for (Point point : points) {
//                    point.move(dx, dy);
//                }
//                break;
            default:
                break;
        }
        repaint();
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
        pressedPoint = null;
        lastLine = null;
        switch (mode) {
            case POINTS: 
                setCursor(Cursor.getPredefinedCursor(CROSSHAIR_CURSOR)); 
                break;
            default:
                setCursor(Cursor.getDefaultCursor());
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Image image = map.getImage();
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
            gg.drawImage(map.getImage(), 0, 0, this);

            gg.setColor(Color.BLUE);
            Stroke tmp = gg.getStroke();
            gg.setStroke(new BasicStroke(2));
            for (Line line : map.getLines()) {
                drawLine(gg, line);
            }
            gg.setStroke(tmp);

            gg.setColor(Color.RED);
            for (Point point : map.getPoints()) {
                drawPoint(gg, point);
            }
        } finally {
            gg.dispose();
        }
    }

    private void drawPoint(Graphics2D gg, Point point) {
        gg.drawOval(point.getX()-2, point.getY()-2, 4, 4);
        gg.drawOval(point.getX()-5, point.getY()-5, 10, 10);
        if (point == lastLine) {
            gg.drawOval(point.getX()-8, point.getY()-8, 16, 16);
        }
    }
    
    private void drawLine(Graphics2D gg, Line line) {
        Point f = line.getFrom();
        Point t = line.getTo();
        gg.drawLine(f.getX(), f.getY(), t.getX(), t.getY());
        
        double dist = line.getLength();
        String txt = String.format("%.0f", dist);
        if (abs(f.getX()-t.getX()) > abs(f.getY()-t.getY())) {
            gg.drawString(txt, (f.getX()+t.getX())/2-16, (f.getY()+t.getY())/2-6);
        } else {
            gg.drawString(txt, (f.getX()+t.getX())/2+6, (f.getY()+t.getY())/2+4);
        }
    }
}
