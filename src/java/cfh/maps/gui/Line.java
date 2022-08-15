package cfh.maps.gui;

import static java.lang.Math.abs;

public class Line {

    private final Point from;
    private final Point to;
    
    Line(Point from, Point to) {
        assert from != null;
        assert to != null;
        
        this.from = from;
        this.to = to;
    }
    
    public Point getFrom() {
        return from;
    }
    
    public Point getTo() {
        return to;
    }
    
    public double getLength() {
        return to.distanceTo(from.getX(), from.getY()) * 1.11;
    }
    
    public double distanceTo(int x, int y) {
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();
        double l = getLength();
        double u = ((x-x1)*(x2-x1) + (y-y1)*(y2-y1)) / (l*l);
        if (0.0 <= u && u <= 1.0)
            return abs((x2-x1)*(y1-y) - (x1-x)*(y2-y1)) / l;
        else
            return Double.NaN;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Line))
            return false;
        Line other = (Line) obj;
        return other.from.equals(from) && other.to.equals(to);
    }
    
    @Override
    public int hashCode() {
        return from.hashCode()*31 + to.hashCode();
    }
    
    @Override
    public String toString() {
        return from.toString() + "-" + to.toString();
    }
}
