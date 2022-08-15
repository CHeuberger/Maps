package cfh.maps.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class Solver {

    private final boolean directed = false;
    
    private final int N;

    private final Node[] nodes;
    private final int[] in;
    private final int[] out;
    
    private final List<Object>[][] edges;
    private final double[][] cost;
    private final boolean[][] defined;
    private final int[][] next;
    private final Object[][] path;
    private final int[][] connections;
    
    private int[] unbalanced = null;
    private int[][] normalized = null;
    
    private double total = 0;
    
    private boolean lowCostCalculated = false;
    
    Solver(Node... nodes) {
        if (nodes.length < 2) 
            throw new IllegalArgumentException("at least 2 nodes needed: " + nodes.length);
        
        N = nodes.length;
        this.nodes = nodes;

        in = new int[N];
        out = new int[N];
        
        edges = createEdges();
        cost = new double[N][N];
        defined = new boolean[N][N];
        next = new int[N][N];
        path = new Object[N][N];
        connections = new int[N][N];
    }

    void addEdge(Object edge, Node from, Node to, double edgeCost) {
        if (from == null) throw new IllegalArgumentException("null from");
        if (to == null) throw new IllegalArgumentException("null to");
        if (edgeCost < 0) throw new IllegalArgumentException("negative cost: " + edgeCost);
        
        int i = indexOf(from);
        int j = indexOf(to);
        if (i == j) throw new IllegalArgumentException("loop not allowed: " + from + " to " + to);
        
        out[i] += 1;
        in[j] += 1;
        if (!directed) {
            out[j] += 1;
            in[i] += 1;
        }
        if (!defined[i][j]) {
            edges[i][j] = new ArrayList<Object>();
            if (!directed && i != j) {
                edges[j][i] = edges[i][j];
            }
        }
        if (!defined[i][j] || edgeCost < cost[i][j]) {
            defined[i][j] = true;
            cost[i][j] = edgeCost;
            next[i][j] = j;
            path[i][j] = edge;
            if (!directed && i != j) {
                defined[j][i] = true;
                cost[j][i] = edgeCost;
                next[j][i] = i;
                path[j][i] = edge;
            }
        }
        edges[i][j].add(edge);
        connections[i][j] += 1;
        if (!directed && i != j) {
            connections[j][i] += 1;
        }
        total += edgeCost;
        
        lowCostCalculated = false;
        unbalanced = null;
    }
    
    double getCost(Node from, Node to) {
        if (from == null) throw new IllegalArgumentException("null from");
        if (to == null) throw new IllegalArgumentException("null to");

        if (!lowCostCalculated) {
            calcLeastCost();
            checkValid();
        }
        int i = indexOf(from);
        int j = indexOf(to);
        
        return cost[i][j];
    }
    
    Trail getTrail(Node from, Node to) {
        if (from == null) throw new IllegalArgumentException("null from");
        if (to == null) throw new IllegalArgumentException("null to");
        
        double c = getCost(from, to);
        int i = indexOf(from);
        int j = indexOf(to);
        
        Trail walk = new Trail(from, to, c);
        while (i != j) {
            walk.addPath(path[i][j]);
            i = next[i][j];
        }
        return walk;
    }
    
    Collection<Node> getUnbalanced() {
        List<Node> result = new ArrayList<Node>();
        if (unbalanced == null) {
            calcUnbalanced();
        }
        for (int i : unbalanced) {
            result.add(nodes[i]);
        }
        return Collections.unmodifiableCollection(result);
    }
    
    void doNormalize() {
        normalize();
    }
    
    private void calcLeastCost() {
        try {
            if (!directed) {
                for (int i = 0; i < N; i++) {
                    defined[i][i] = true;
                    cost[i][i] = 0;
                }
            }
            for (int k = 0; k < N; k++) {
                for (int i = 0; i < N; i++) {
                    if (defined[i][k]) {
                        for (int j = 0; j < N; j++) {
                            double c = cost[i][k] + cost[k][j];
                            if (defined[k][j] && (!defined[i][j] || c < cost[i][j])) {
                                defined[i][j] = true;
                                cost[i][j] = c;
                                next[i][j] = next[i][k];
                                path[i][j] = path[i][k];
                                if (i == j && c < 0)
                                    throw new IllegalArgumentException("cycle with negative cost");
                            }
                        }
                    }
                }
            }
        } finally {
            lowCostCalculated = true;
        }
    }
    
    private void checkValid() {
        for (int i = 0; i < N; i++) {
            if (in[i] == 0) 
                throw new IllegalStateException("no connection to node " + nodes[i]);
            if (out[i] == 0) 
                throw new IllegalStateException("no connection from node " + nodes[i]);
            for (int j = 0; j < N; j++) {
                if (!defined[i][j]) 
                    throw new IllegalStateException("no route from " + nodes[i] + " to " + nodes[j]);
            }
        }
    }
    
    private void calcUnbalanced() {
        int[] result = new int[N];  // to big, but faster
        int count = 0;
        if (directed) {
            for (int i = 0; i < N; i++) {
                if (in[i] != out[i]) {
                    result[count++] = i;
                }
            }
        } else {
            for (int i = 0; i < N; i++) {
                if (out[i] % 2 != 0) {
                    result[count++] = i;
                }
            }
        }
        unbalanced = Arrays.copyOf(result, count);
    }

    private void normalize() {
        if (!lowCostCalculated) {
            calcLeastCost();
            checkValid();
        }
        if (unbalanced == null) {
            calcUnbalanced();
        }
        normalized = new int[N][N];
        if (unbalanced.length == 0)
            return;
        
        if (directed) {
            throw new UnsupportedOperationException();
        } else {
            Set open = new Set(unbalanced.length);
            Set closed = new Set(unbalanced.length);
            
            double[] gg = new double[unbalanced.length];
            double[] hh = new double[unbalanced.length];
            double[] ff = new double[unbalanced.length];
            int[] result = new int[unbalanced.length];
            
            boolean first = true;
            open.add(0);
            gg[0] = 0;
            hh[0] = minimum(closed);
            ff[0] = hh[0];
            while (open.getSize() > 0) {
                int x = -1;
                double min = Double.MAX_VALUE;
                for (int i : open) {
                    if (ff[i] < min) {
                        min = ff[i];
                        x = i;
                    }
                }
                if (!open.remove(x)) throw new IllegalStateException("remove " + x);
                closed.add(x);
                for (int y = 0; y < unbalanced.length; y++) {
                    if (closed.contains(y))
                        continue;
//                    double testG = gg[x] + (first ? cost[unbalanced[x]][unbalanced[y]] : 0);
                    double testG = gg[x] + cost[unbalanced[x]][unbalanced[y]];
                    if (!open.contains(y)) {
                        open.add(y);
                    } else if (testG >= gg[y]) {
                        continue;
                    }
                    result[x] = y;
                    gg[y] = testG;
                    hh[y] = first ? minimum(closed, y) : minimum(closed);
                    ff[y] = gg[y] + hh[y];
                }
                first = !first;
            }
         
            System.out.println();
            for (int i = 0; i < result.length; i += 1) {
                int n1 = unbalanced[i];
                int n2 = unbalanced[result[i]];
                System.out.println(nodes[n1] + " - " + nodes[n2]);
            }
            System.out.println(gg[result[result.length-2]]);
        }
    }

    private double minimum(Set closed) {
        return minimum(closed, -1);
    }
    
    private double minimum(Set closed, int actual) {
        Set done = new Set(closed);
        if (actual != -1) {
            done.add(actual);
        }
        double expect = 0;
        for (int i = 0; i < unbalanced.length-1; i++) {
            if (done.contains(i))
                continue;
            double min = Double.MAX_VALUE;
            for (int j = i+1; j < unbalanced.length; j++) {
                if (done.contains(j))
                    continue;
                double c = cost[unbalanced[i]][unbalanced[j]];
                if (c < min) {
                    min = c;
                    done.add(i);
                    done.add(j);
                }
            }
            expect += min;
        }
        return expect;
    }
    
    private int indexOf(Node node) {
        for (int i = 0; i < nodes.length; i++) {
            if (node.equals(nodes[i]))
                return i;
        }
        throw new NoSuchElementException(node.toString());
    }
    
    @SuppressWarnings("unchecked")
    private List<Object>[][] createEdges() {
        List[][] list = new List[N][N];
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                list[i][j] = new ArrayList<String>();
//            }
//        }
        return list;
    }
    
    private static class Set implements Iterable<Integer> {
        private int size;
        private int[] data;
        
        Set(int length) {
            size = 0;
            data = new int[length];
        }
        
        Set(Set set) {
            data = set.data.clone();
            size = set.size;
        }

        boolean add(int x) {
            if (!contains(x)) {
                data[size++] = x;
                return true;
            } else {
                return false;
            }
        }
        
//        int get(int index) {
//            if (index < 0 || index >= size) 
//                throw new IndexOutOfBoundsException("size: " + size + ", index: " + index);
//            return data[index];
//        }
//        
        boolean remove(int x) {
            int i = find(x);
            if (i >= 0) {
                size -= 1;
                if (i < size) {
                    System.arraycopy(data, i+1, data, i, size-i);
                }
                return true;
            } else {
                return false;
            }
        }
        
        int find(int x) {
            for (int i = 0; i < size; i++) {
                if (data[i] == x)
                    return i;
            }
            return -1;
        }
        
        boolean contains(int x) {
            return find(x) != -1;
        }
        
        int getSize() {
            return size;
        }
        
        int[] getData() {
            return data.clone();
        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                private int next = 0;
                @Override
                public boolean hasNext() {
                    return next < size;
                }
                @Override
                public Integer next() {
                    return data[next++];
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        @Override
        public String toString() {
            String result = "(";
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    result += ",";
                }
                result += data[i];
            }
            return result + ")";
        }
    }
}
