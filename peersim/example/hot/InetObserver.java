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

import java.io.*;
import java.util.*;

import peersim.config.*;
import peersim.core.*;

/**
 * This class prints to dinstinct files both the topology wiring (with a Gnuplot
 * complaint syntax) and the out-degree distribution. In addition, it can trigger
 * a topology check performed by a {@link RobustnessEvaluator} class object.
 */
public class InetObserver implements Control {
    // --------------------------------------------------------------------------
    // Parameters
    // --------------------------------------------------------------------------

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private final String PAR_PROT = "protocol";

    /**
     * The file to print out the topology relations.
     * 
     * @config
     */
    private final String PAR_GRAPH_FILENAME = "graph_file";

    /**
     * The file to print out the graph out-degree distribution.
     * 
     * @config
     */
    private final String PAR_GRAPH_DEGREE_FILENAME = "graph_degree";

    /**
     * The parameter flag to check for robustness.
     * 
     * @config
     */
    private final String PAR_ROBUSTNESS = "robustness";

    // --------------------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------------------

    /**
     * The name of this observer in the configuration file. Initialized by the
     * constructor parameter.
     */
    private final String prefix;

    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /**
     * Printer for the graph topology dump. Gnuplot plottable syntax.
     */
    private final PrintWriter graph_fileout;

    /** Printer for the graph out-degree data. */
    private final PrintWriter dg_fileout;

    /**
     * Topology filename. Obtained from config property
     * {@link #PAR_GRAPH_FILENAME}.
     */
    private final String graph_filename;

    /**
     * Degree statistics filename. Obtained from config property
     * {@link #PAR_GRAPH_DEGREE_FILENAME}.
     */
    private final String dg_filename;

    /**
     * Flag to perform or not the robustness test with a
     * {@link RobustnessEvaluator} object.
     */
    private final boolean rcheck;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public InetObserver(String prefix) {
        this.prefix = prefix;
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        graph_filename = Configuration.getString(prefix + "."
                + PAR_GRAPH_FILENAME, "graph.dat");
        dg_filename = Configuration.getString(prefix + "."
                + PAR_GRAPH_DEGREE_FILENAME, "degree_graph.dat");
        rcheck = Configuration.contains(prefix + "." + PAR_ROBUSTNESS);

        try {
            graph_fileout = new PrintWriter(new FileWriter(graph_filename));
            dg_fileout = new PrintWriter(new FileWriter(dg_filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Control interface method.
    public boolean execute() {
        OverlayGraph ogr = new OverlayGraph(pid);
        System.out.println(prefix + ": writing to files " + graph_filename
                + "and " + dg_filename);
        graphToFile(ogr);
        dgDistribToFile(ogr);
        if (rcheck) {
            RobustnessEvaluator rev = new RobustnessEvaluator(ogr);

            System.out.println("Metric 1 " + rev.getMetric1());

            long[] m2res = rev.getMetric2();
            for (int i = 0; i < m2res.length; i++) {
                System.out.println("Metric 2 " + i + " " + m2res[i]);
            }
        }

        return false;
    }

    /**
     * Prints out data to plot the topology using gnuplot a gnuplot style
     * 
     * @param g
     *            current graph
     */
    private void graphToFile(peersim.graph.Graph g) {
        // Starts from 1 because for sure node 0 is a root
        for (int i = 1; i < g.size(); i++) {
            Node current = (Node) g.getNode(i);
            double x_to = ((InetNodeProtocol) current.getProtocol(pid)).x;
            double y_to = ((InetNodeProtocol) current.getProtocol(pid)).y;
            Collection col = g.getNeighbours(i);
            if (col.isEmpty())
                continue; // another root is found, skip!
            Iterator it = col.iterator();
            while (it.hasNext()) {
                int index = ((Integer) it.next()).intValue();
                Node n = (Node) g.getNode(index);
                double x_from = ((InetNodeProtocol) n.getProtocol(pid)).x;
                double y_from = ((InetNodeProtocol) n.getProtocol(pid)).y;
                graph_fileout.println(x_from + " " + y_from);
                graph_fileout.println(x_to + " " + y_to);
                graph_fileout.println("");
            }
        }
        graph_fileout.close();
    }

    /**
     * Prints out statics about out-degree distribution.
     * 
     * @param g
     *            current graph
     */
    private void dgDistribToFile(peersim.graph.Graph g) {
        int size = g.size();
        int[] dgfrq = new int[size];
        double[] dgprob = new double[size];
        for (int i = 0; i < size; i++) { // do not plot leaves
            Node n = (Node) g.getNode(i);
            InetNodeProtocol protocol = (InetNodeProtocol) n.getProtocol(pid);
            int degree = protocol.in_degree;
            dgfrq[degree]++;
        }
        double sum = 0;
        for (int i = size - 1; i > 0; i--) {
            dgprob[i] = (dgfrq[i] + sum) / size;
            sum += dgfrq[i];
        }
        // do not count index 0: 'cos the leafs degree is clearly 0!
        for (int i = 0; i < dgprob.length; i++) {
            double k = (double) i / size;
            dg_fileout.println(k + " " + dgprob[i]);
        }
        dg_fileout.close();
    }

}
