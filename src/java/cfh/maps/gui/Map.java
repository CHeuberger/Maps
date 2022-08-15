package cfh.maps.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Map {

    public static final String PROP_IMAGE = "image";
    public static final String PROP_POINTS = "points";
    public static final String PROP_LINES = "lines";
    
    private BufferedImage image;
    
    private final List<Point> points = new ArrayList<Point>();
    private final List<Line> lines = new ArrayList<Line>();
    
    private final transient PropertyChangeSupport support = new PropertyChangeSupport(this);

    Map() {
    }
    
    // TODO delete
//    void copyFrom(Map map) {
//        setImage(map.image);
//        points.clear();
//        points.addAll(map.points);
//        lines.addAll(map.lines);
//        support.firePropertyChange(PROP_POINTS, -1, points.size());
//    }
   
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        if (image == null) throw new IllegalArgumentException("null image");
        
        Image old = this.image;
        this.image = image;
        support.firePropertyChange(PROP_IMAGE, old, image);
    }
    
    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }
    
    public void clearPoints() {
        int old = points.size();
        points.clear();
        support.firePropertyChange(PROP_POINTS, old, 0);
    }
    
    public boolean addPoint(Point point) {
        if (point == null) throw new IllegalArgumentException("null point");
        
        int old = points.size();
        if (points.add(point)) {
            support.firePropertyChange(PROP_POINTS, old, points.size());
            return true;
        } else {
            return false;
        }
    }
    
    public boolean removePoint(Point point) {
        if (point == null) throw new IllegalArgumentException("null point");
        
        int old = points.size();
        if (points.remove(point)) {
            support.firePropertyChange(PROP_POINTS, old, points.size());
            return true;
        } else {
            return false;
        }
    }
    
    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    public void clearLines() {
        int old = lines.size();
        lines.clear();
        support.firePropertyChange(PROP_LINES, old, 0);
    }
    
    public boolean addLine(Line line) {
        if (line == null) throw new IllegalArgumentException("null line");
        if (!points.contains(line.getFrom())) throw new IllegalArgumentException("invalid from point");
        if (!points.contains(line.getTo())) throw new IllegalArgumentException("invalid to point");
        
        int old = lines.size();
        if (lines.add(line)) {
            support.firePropertyChange(PROP_LINES, old, lines.size());
            return true;
        } else {
            return false;
        }
    }
    
    public boolean removeLine(Line line) {
        if (line == null) throw new IllegalArgumentException("null line");
        
        int old = lines.size();
        if (lines.remove(line)) {
            support.firePropertyChange(PROP_LINES, old, lines.size());
            return true;
        } else {
            return false;
        }
    }
    
    public Point findPoint(int x, int y, int minDistance) {
        Point found = null;
        double dist = minDistance;
        for (Point point : points) {
            double d = point.distanceTo(x, y);
            if (d <= dist) {
                found = point;
                dist = d;
            }
        }
        return found;
    }
    
    public Line findLine(int x, int y, int minDistance) {
        Line found = null;
        double dist = minDistance;
        for (Line line : lines) {
            double d = line.distanceTo(x, y);
            if (d <= dist) {
                found = line;
                dist = d;
            }
        }
        return found;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
