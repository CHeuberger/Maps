package cfh.maps.gui;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

class FileMapDAO implements MapDAO {

    private static final int MAGIC = 0x00CAFE00;
    
    private static final int VERSION = 100;
    
    private final File file;
    
    FileMapDAO(File file) {
        if (file == null) throw new IllegalArgumentException("null File");
        
        this.file = file;
    }
    
    @Override
    public Map read() throws IOException {
        Map map = new Map();
        ObjectInputStream inp = new ObjectInputStream(new FileInputStream(file));
        try {
            int version = readHeader(inp);
            switch (version) {
                case VERSION: break;
                default: throw new IOException("unrecognized version " + version);
            }
            Point[] points = readPoints(inp);
            Line[] lines = readLines(inp);
            BufferedImage image = readImage(inp);
            
            map.setImage(image);
            for (Point point : points) {
                map.addPoint(point);
            }
            Arrays.sort(points);
            for (int i = 0; i < lines.length; i++) {
                int from = Arrays.binarySearch(points, lines[i].getFrom());
                if (from == -1)
                    throw new IOException("missing start point " + lines[i].getFrom() + " for line " + i);
                int to = Arrays.binarySearch(points, lines[i].getTo());
                if (to == -1)
                    throw new IOException("missing end point " + lines[i].getTo() + " for line " + i);
                Line line = new Line(points[from], points[to]);
                map.addLine(line);
            }
            
            return map;
        } finally {
            inp.close();
        }
    }

    private int readHeader(ObjectInputStream inp) throws IOException {
        int magic = inp.readInt();
        if (magic != MAGIC)
            throw new IOException("unrecognized file (magic: " + Integer.toHexString(magic) + ")");
        int version = inp.readInt();
        return version;
    }

    private Point[] readPoints(ObjectInputStream inp) throws IOException {
        int count = inp.readInt();
        Point[] points = new Point[count];
        for (int i = 0; i < count; i++) {
            points[i] = readPoint(inp);
        }
        return points;
    }

    private Line[] readLines(ObjectInputStream inp) throws IOException {
        int count = inp.readInt();
        Line[] lines = new Line[count];
        for (int i = 0; i < count; i++) {
            lines[i] = readLine(inp);
        }
        return lines;
    }

    private BufferedImage readImage(ObjectInputStream inp) throws IOException {
        return ImageIO.read(inp);
    }
    
    private Point readPoint(ObjectInputStream inp) throws IOException {
        return new Point(inp.readInt(), inp.readInt());
    }
    
    private Line readLine(ObjectInputStream inp) throws IOException {
        return new Line(readPoint(inp), readPoint(inp));
    }

    @Override
    public void save(Map map) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        try {
            writeHeader(out);
            writePoints(out, map.getPoints());
            writeLines(out, map.getLines());
            writeImage(out, map.getImage());
        } finally {
            out.close();
        }
    }

    private void writeHeader(ObjectOutputStream out) throws IOException {
        out.writeInt(MAGIC);
        out.writeInt(VERSION);
    }

    private void writePoints(ObjectOutputStream out, List<Point> points) throws IOException {
        out.writeInt(points.size());
        for (Point point : points) {
            writePoint(out, point);
        }
    }
    
    private void writeLines(ObjectOutputStream out, List<Line> lines) throws IOException {
        out.writeInt(lines.size());
        for (Line line : lines) {
            writeLine(out, line);
        }
    }

    private void writeImage(ObjectOutputStream out, RenderedImage image) throws IOException {
        if (image != null) {
            ImageIO.write(image, "png", out);
        }
    }

    private void writePoint(ObjectOutputStream out, Point point) throws IOException {
        out.writeInt(point.getX());
        out.writeInt(point.getY());
    }

    private void writeLine(ObjectOutputStream out, Line line) throws IOException {
        writePoint(out, line.getFrom());
        writePoint(out, line.getTo());
    }
}
