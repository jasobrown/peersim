/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package example.hot;

import java.util.*;

import peersim.core.*;
import peersim.graph.*;

/**
 * This class perform some robustness checks. Two checks are available:
 * <ul>
 * <li>prints the average number of nodes that have to fail before a random
 * pair of nodes becomes disconnected.</li>
 * <li>prints the number of unconnected components as nodes crash.
 * </li>
 * </ul>
 * 
 */
public class RobustnessEvaluator {

    class NodesPair {

        public NodesPair(Graph g) {
            GraphAlgorithms gal = new GraphAlgorithms();
            do {
                src = lcg.nextInt(g.size());
                dst = lcg.nextInt(g.size());
                gal.dist(g, src);
            } while (src == dst || gal.d[dst] == -1);
        }

        protected int src;

        protected int dst;

        public boolean equals(Object o) {
            if (!(o instanceof NodesPair))
                return false;
            else {
                NodesPair pair = (NodesPair) o;
                return (src == pair.src) && (dst == pair.dst);
            }
        }
    }

    protected Random lcg = new Random();

    private final int pairsNo = 100;

    private Set initPairs(Graph g) {
        HashSet<NodesPair> pairs = new HashSet<NodesPair>(pairsNo);
        while (pairs.size() < pairsNo)
            pairs.add(new NodesPair(g));
        return pairs;
    }

    public int[] computeStamps(Graph g) {
        Set pairs = initPairs(g);
        int[] stamps = new int[pairsNo];
        boolean[] alreadyRemoved = new boolean[g.size()];
        for (int j = 0; j < alreadyRemoved.length; j++)
            alreadyRemoved[j] = false;
        for (int j = 0; j < stamps.length; j++)
            stamps[j] = -1;
        int residualPairs = pairsNo;
        time = 0;
        Object[] objPairs = pairs.toArray();
        GraphAlgorithms gal = new GraphAlgorithms();
        while (residualPairs > 0) {
            if ((time % 10) == 0)
                System.out.print(".");
            int i = lcg.nextInt(g.size());
            if (alreadyRemoved[i])
                continue;
            alreadyRemoved[i] = true;
            ((Node) g.getNode(i)).setFailState(peersim.core.Fallible.DOWN);

            for (int j = 0; j < pairsNo; j++) {
                if (stamps[j] != -1)
                    continue;
                NodesPair n = (NodesPair) objPairs[j];
                gal.dist(g, n.src);
                if (gal.d[n.dst] == -1) {
                    stamps[j] = time;
                    residualPairs--;
                }
            }
            time++;
        }

        for (int i = 0; i < g.size(); i++) {
            ((Node) g.getNode(i)).setFailState(peersim.core.Fallible.OK);
        }

        return stamps;
    }

    private float avg;

    private int[] stamps;

    private int time;

    private Graph g;

    /**
     * Standard constructor.
     * 
     * @param g
     *            the {@link Graph} structure to evaluate.
     */
    public RobustnessEvaluator(Graph g) {
        this.g = g;
        stamps = computeStamps(g);
        avg = 0;
        for (int i = 0; i < stamps.length; i++)
            avg += stamps[i];
        avg /= pairsNo;
    }

    /**
     * @return Average number of nodes that have to fail before a random pair of
     *         nodes becomes disconnected.
     */
    public float getMetric1() {
        return avg;
    }

    /**
     * @return An array filled with the number of unconnected components as
     *         nodes crash.
     * 
     */
    public long[] getMetric2() {
        long[] a = new long[g.size()];

        for (int i = 0; i < stamps.length; i++) {
            a[stamps[i]]++;
        }

        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i] + sum;
            sum += a[i];
        }
        return a;
    }
}
