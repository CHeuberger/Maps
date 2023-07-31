package cfh.maps.graph;

import static cfh.maps.graph.GraphPanel.Mode.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class GraphDialog extends JDialog implements ActionListener, PropertyChangeListener {
    
    private static final String CMD_COMPACT = "COMPACT";
    private static final String CMD_START = "START";
    private static final String CMD_COST = "COST";
    private static final String CMD_SOLVE = "SOLVE";
    private static final String CMD_WALK = "WALK";
    private static final String CMD_STAT = "STAT";
    
    private final JLabel statusMode;
    private final JLabel statusMsg;
    private final JLabel statusTrail;
    
    private final GraphPanel graphPanel;
    private final ManualWalker manualWalker;
    
    public GraphDialog(Frame owner, Graph graph, Image image) {
        super(owner, "GRAPH");
        
        Box buttons = Box.createHorizontalBox();
        buttons.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(newJButton(CMD_COMPACT, "Compact", "Remove unneded points (no or exactly 2 lines)"));
        buttons.add(Box.createHorizontalStrut(20));
        buttons.add(newJButton(CMD_START, "Start", "<html>Activates Start mode:<dl>"
            + "<dt>left-click</dt><dd>set start point</dd>"
            + "<dt>right-click</dt><dd>clear start point</dd>"
            + "</dl>"));
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(newJButton(CMD_COST, "Cost", "<html>Activates Cost mode:<dl>"
            + "<dt>left-click</dt><dd>show best path from start to clicked point (or set start if not yet set</dd>"
            + "<dt>right-click</dt><dd>clear actual path or disable cost mode</dd>"
            + "</dl>"));
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(newJButton(CMD_SOLVE, "Solve", "<html>Activates Solver:<dl>"
            + "<dt>left-click</dt><dd>1st click calculate unbalanced points; further clicks do normalization</dd>"
            + "<dt>right-click</dt><dd>clear unbalanced points</dd>"
            + "</dl>"));
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(newJButton(CMD_WALK, "Walk","<html>Activates Manual Walk mode:<dl>"
            + "<dt>left-click</dt><dd>add line to last point</dd>"
            + "<dt>right-click</dt><dd><back one line/dd>"
            + "</dl>"));
        buttons.add(Box.createHorizontalStrut(20));
        buttons.add(newJButton(CMD_STAT, "Stat", null));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(Box.createHorizontalStrut(10));
        
        graphPanel = new GraphPanel(graph, image);
        graphPanel.addGraphChangeListener(this);
        
        JPanel panel = new JPanel();
        panel.add(graphPanel);    // center image

        statusMode = newJLabel("mode", "Mode", 100);
        statusMsg = newJLabel("", "message", 100);
        statusTrail = newJLabel("trail", "Trail distance", 100);
        
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(new CompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            new EmptyBorder(0, 2, 2, 2)));
        status.add(statusMode, BorderLayout.BEFORE_LINE_BEGINS);
        status.add(statusMsg, BorderLayout.CENTER);
        status.add(statusTrail, BorderLayout.AFTER_LINE_ENDS);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(panel), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setSize(1000, 700);
        validate();
        setLocationRelativeTo(null);
        
        manualWalker = new ManualWalker();
        manualWalker.addChangeListener(this);
    }
    
    private JLabel newJLabel(String text, String tooltip, int width) {
        JLabel label = new JLabel(text);
        label.setBorder(
            new CompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                new EmptyBorder(2, 4, 2, 4)));
        label.setFont(new Font("Tahoma", Font.PLAIN, 12));
        label.setEnabled(false);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setToolTipText(tooltip);
        Dimension dim = label.getPreferredSize();
        dim.width = width;
        label.setPreferredSize(dim);
        return label;
    }
    
    private JButton newJButton(String command, String text, String tooltip) {
        JButton button = new JButton(text);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }
        button.addActionListener(this);
        button.setActionCommand(command);
        button.setMargin(new Insets(2, 2, 2, 2));
        return button;
    }
    
    private void doCompact() {
        graphPanel.getGraph().compact();
        graphPanel.repaint();
    }
    
    private void doStart() {
        graphPanel.setMode(SET_START);
    }
    
    private void doCost() {
        CostWalker walker = new CostWalker();
        walker.addChangeListener(this);
        graphPanel.setWalker(walker);
    }

    private void doSolve() {
        SolveWalker walker = new SolveWalker();
        walker.addChangeListener(this);
        graphPanel.setWalker(walker);
    }

    private void doWalk() {
        graphPanel.setWalker(manualWalker);
    }
    
    private void doStat() {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("monospaced", Font.PLAIN, 12));
        
        Graph graph = graphPanel.getGraph();
        Collection<Node> nodes = graph.getNodes();
        Collection<Edge> edges = graph.getEdges();
        
        text.append(String.format("nodes: %d%n", nodes.size()));
        
        int max = 0;
        for (Node node : nodes) {
            int n = node.getEdgeCount();
            if (n > max) {
                max = n;
            }
        }
        int[] hist = new int[max+1];
        for (Node node : nodes) {
            hist[node.getEdgeCount()] += 1;
        }
        for (int i = 0; i < hist.length; i++) {
            text.append(String.format("  [%d] = %d%n", i, hist[i]));
        }
        
        text.append("\n");
        text.append(String.format("edges: %d%n", edges.size()));
        
        double length;
        length = 0;
        for (Edge edge : edges) {
            length += edge.getLength();
        }
        text.append(String.format("total:    %7.1f%n", length));
        text.append("\n");
        
        length = 0;
        double repeated = 0;
        HashSet<Edge> open = new HashSet<Edge>(edges);
        for (Edge edge : manualWalker.getTrail()) {
            length += edge.getLength();
            if (!open.remove(edge)) {
                repeated += edge.getLength();
            }
        }
        double opLength = 0;
        for (Edge edge : open) {
            opLength += edge.getLength();
        }
        text.append(String.format("open:     %7.1f%n", opLength));
        text.append(String.format("trail:    %7.1f%n", length));
        text.append(String.format("repeated: %7.1f%n", repeated));
        
        text.append(String.format("%n"));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(text));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == CMD_COMPACT) {
            doCompact();
        } else if (command == CMD_START) {
            doStart();
        } else if (command == CMD_COST) {
            doCost();
        } else if (command == CMD_SOLVE) {
            doSolve();
        } else if (command == CMD_WALK) {
            doWalk();
        } else if (command == CMD_STAT) {
            doStat();
        } else {
            new Exception("unrecognized command \"" + command + "\"").printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(GraphPanel.PROP_MODE)) {
            statusMode.setText(objectToString(e.getNewValue()));
            statusMode.setEnabled(true);
        } else if (e.getPropertyName().equals(GraphPanel.PROP_MSG)) {
            statusMsg.setText(objectToString(e.getNewValue()));
            statusMsg.setEnabled(true);
        } else if (e.getPropertyName().equals(GraphPanel.PROP_TRAIL)) {
            statusTrail.setText(objectToString(e.getNewValue()));
            statusTrail.setEnabled(true);
        } else {
            return;
        }
    }
    
    private static final String objectToString(Object obj) {
        return (obj != null) ? obj.toString() : null;
    }
}
