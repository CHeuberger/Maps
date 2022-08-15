package cfh.maps.gui;

public class Point implements Comparable<Point> {

    private int x;
    private int y;
    
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    Point(Point point) {
        this(point.x, point.y);
    }
    
    Point(java.awt.Point point) {
        this(point.x, point.y);
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }
    
    public double distanceTo(int x1, int y1) {
        double dx = x - x1;
        double dy = y - y1;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    @Override
    public int compareTo(Point other) {
        if (other == null)
            return 1;
        if (y < other.y)
            return -1;
        else if (y > other.y)
            return 1;
        else {
            return (x < other.x) ? -1 : ((x > other.x) ? 1 : 0);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Point))
            return false;
        Point other = (Point) obj;
        return (other.x == x) && (other.y == y);
    }
    
    @Override
    public int hashCode() {
        return x + 17*y;
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
