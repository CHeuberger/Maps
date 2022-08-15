package cfh.maps.gui;

import static cfh.maps.gui.MapPanel.Mode.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import cfh.maps.graph.Graph;
import cfh.maps.graph.GraphDialog;

class MapFrame extends JFrame {

    private static final String PREF_DIR = "directory";
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());
    
    private Map map;
    private final MapPanel mapPanel;
    
    MapFrame() {
        super("MAP");
        
        JButton clear = newJButton("CLEAR");
        clear.setForeground(Color.RED.darker());
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClear();
            }
        });
        
        JButton image = newJButton("Map");
        image.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doMap();
            }
        });
        
        JButton load = newJButton("Load");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLoad();
            }
        });
        
        JButton join = newJButton("Join");
        join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doJoin();
            }
        });
        
        JButton save = newJButton("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        
        final JToggleButton points = newJToggleButton("Points");
        points.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPoints(points.isSelected());
            }
        });
        
        final JToggleButton lines = newJToggleButton("Lines");
        lines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLines(lines.isSelected());
            }
        });
        
        JButton stat = newJButton("Stat");
        stat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStat();
            }
        });
        
        JButton graph = new JButton("Graph");
        graph.setForeground(Color.GREEN.darker());
        graph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doGraph();
            }
        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(points);
        group.add(lines);
        
        Box buttons = Box.createHorizontalBox();
        buttons.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(clear);
        buttons.add(Box.createHorizontalStrut(20));
        buttons.add(image);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(load);
        buttons.add(join);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(save);
        buttons.add(Box.createHorizontalStrut(40));
        buttons.add(points);
        buttons.add(lines);
        buttons.add(Box.createHorizontalStrut(40));
        buttons.add(stat);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(graph);
        buttons.add(Box.createHorizontalStrut(10));
        
        map = new Map();
        mapPanel = new MapPanel(map);

        JPanel panel = new JPanel();
        panel.add(mapPanel);    // center image
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(panel), BorderLayout.CENTER);
        setSize(1000, 700);
        validate();
        setLocationRelativeTo(null);
    }
    
    private JButton newJButton(String text) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(2, 2, 2, 2));
        return button;
    }
    
    private JToggleButton newJToggleButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setMargin(new Insets(2, 2, 2, 2));
        return button;
    }
    
    private void doClear() {
        map = new Map();
        mapPanel.setMap(map);
        repaint();
    }
    
    private void doMap() {
        String dir = prefs.get(PREF_DIR, null);
        JFileChooser chooser = new JFileChooser(dir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        dir = chooser.getCurrentDirectory().getAbsolutePath();
        prefs.put(PREF_DIR, dir);
        
        File file = chooser.getSelectedFile();
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex);
            return;
        }
        
        map.setImage(image);
    }
    
    private void doLoad() {
        String dir = prefs.get(PREF_DIR, null);
        JFileChooser chooser = new JFileChooser(dir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        dir = chooser.getCurrentDirectory().getAbsolutePath();
        prefs.put(PREF_DIR, dir);
        
        File file = chooser.getSelectedFile();
        FileMapDAO dao = new FileMapDAO(file);
        try {
            map = dao.read();
            mapPanel.setMap(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex);
        }
    }
    
    private void doJoin() {
        String dir = prefs.get(PREF_DIR, null);
        JFileChooser chooser = new JFileChooser(dir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        dir = chooser.getCurrentDirectory().getAbsolutePath();
        prefs.put(PREF_DIR, dir);
        
        File file = chooser.getSelectedFile();
        FileMapDAO dao = new FileMapDAO(file);
        try {
            Map tmp = dao.read();
            for (Point point : tmp.getPoints()) {
                map.addPoint(point);
            }
            for (Line line : tmp.getLines()) {
                map.addLine(line);
            }
//            mapPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex);
        }
    }
    
    private void doSave() {
        String dir = prefs.get(PREF_DIR, null);
        JFileChooser chooser = new JFileChooser(dir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        dir = chooser.getCurrentDirectory().getAbsolutePath();
        prefs.put(PREF_DIR, dir);
        
        File file = chooser.getSelectedFile();
        FileMapDAO dao = new FileMapDAO(file);
        try {
            dao.save(map);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex);
        }
    }
    
    private void doPoints(boolean selected) {
        if (selected) {
            mapPanel.setMode(POINTS);
        }
    }
    
    private void doLines(boolean selected) {
        if (selected) {
            mapPanel.setMode(LINES);
        }
    }
    
    private void doStat() {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("monospaced", Font.PLAIN, 12));
        
        class Counter {
            int count = 0;
            int increment() {
                count += 1;
                return count;
            }
        }
        java.util.Map<Point, Counter> counters = new HashMap<Point, Counter>();
        List<Point> points = map.getPoints();
        List<Line> lines = map.getLines();
        for (Point point : points) {
            counters.put(point, new Counter());
        }
        int max = 0;
        for (Line line : lines) {
            int c;
            c = counters.get(line.getFrom()).increment();
            if (c > max) max = c;
            c = counters.get(line.getTo()).increment();
            if (c > max) max = c;
        }
        int[] hist = new int[max+1];
        for (Counter counter : counters.values()) {
            hist[counter.count] += 1;
        }
        
        text.append(String.format("number of points: %d%n", points.size()));
        for (int i = 0; i <= max; i++) {
            text.append(String.format("  [%d]: %d%n", i, hist[i]));
        }
        text.append("\n");
        
        text.append(String.format("number of lines:  %d%n", lines.size()));
        text.append("\n");
        
        double distance = 0;
        for (Line line : lines) {
            distance += line.getLength();
        }
        text.append(String.format("distance: %.1f%n", distance));
        
        
        JOptionPane.showMessageDialog(this, new JScrollPane(text));
    }
    
    private void doGraph() {
        Graph graph = Graph.create(map);
        GraphDialog dialog = new GraphDialog(this, graph, map.getImage());
        dialog.setModal(true);
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        MapFrame frame = new MapFrame();
        frame.setVisible(true);
    }
}
