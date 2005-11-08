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

import peersim.config.*;
import peersim.core.*;

/**
 * This class prints to dinstinct files both the topology wiring (with a Gnuplot
 * complaint syntax) and the out-degree distribution. In addition, it can trigger
 * a topology check performed by a {@link RobustnessEvaluator} class object.
 */
public class InetObserver implements Control {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The file to print out the topology relations.
     * 
     * @config
     */
    private static final String PAR_GRAPH_FILENAME = "graph_file";

    /**
     * The parameter flag to check for robustness.
     * 
     * @config
     */
    private static final String PAR_ROBUSTNESS = "robustness";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

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

    /**
     * Topology filename. Obtained from config property
     * {@link #PAR_GRAPH_FILENAME}.
     */
    private final String graph_filename;

    /**
     * Flag to perform or not the robustness test with a
     * {@link RobustnessEvaluator} object.
     */
    private final boolean rcheck;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
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
        rcheck = Configuration.contains(prefix + "." + PAR_ROBUSTNESS);

        try {
            graph_fileout = new PrintWriter(new FileWriter(graph_filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Control interface method.
    public boolean execute() {
        OverlayGraph ogr = new OverlayGraph(pid);
        System.out.println(prefix + ": writing to file " + graph_filename);
        graphToFile(ogr);
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
        for (int i = 0; i < g.size(); i++) {
            Node current = (Node) g.getNode(i);
            double x_to = ((InetNodeProtocol) current.getProtocol(pid)).x;
            double y_to = ((InetNodeProtocol) current.getProtocol(pid)).y;
            for(int index:g.getNeighbours(i)) {
                Node n = (Node) g.getNode(index);
                double x_from = ((InetNodeProtocol) n.getProtocol(pid)).x;
                double y_from = ((InetNodeProtocol) n.getProtocol(pid)).y;
                graph_fileout.println(x_from + " " + y_from);
                graph_fileout.println(x_to + " " + y_to);
                graph_fileout.println();
            }
        }
        graph_fileout.close();
    }
}
