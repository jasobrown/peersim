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

import peersim.config.*;
import peersim.core.*;
import peersim.graph.*;

/**
 * <p>
 * This intialization class collects the simulation parameters from the config
 * file and invocates the suited factory. It collects in-degree statistics and
 * node coordinates and writes them to file; thus there is no real need of a
 * Control class. At the end of the initialization a specialized class (@link
 * example.hot.RobustnessEvaluator) performs some topology robustness
 * measurements.
 * </p>
 * <p>
 * This class obeys to the {@link peersim.dynamics.WireByMethod} class contract.
 * </p>
 * 
 * @author Gian Paolo Jesi
 */
public class InetInitializer implements Control {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private static int pid;

    private static final String DEBUG_STRING = "Inet.InetInitializer: ";

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
    public InetInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initialize the node coordinates and the current in-degree. The wiring is
     * not performed here.
     */
    public boolean execute() {

        // set the root
        Node n = Network.get(0);
        InetNodeProtocol prot = (InetNodeProtocol) n.getProtocol(pid);
        prot.isroot = true;
        prot.hops = 0;
        prot.x = 0.5;
        prot.y = 0.5;

        // Set coordinates x,y and set indegree 0
        for (int i = 1; i < Network.size(); i++) {
            n = Network.get(i);
            prot = (InetNodeProtocol) n.getProtocol(pid);
            prot.x = CommonState.r.nextDouble();
            prot.y = CommonState.r.nextDouble();
        }
        return false;
    }

    /**
     * Performs the actual wiring.
     * 
     * @param g
     *            a {@link peersim.graph.Graph} interface object to work on.
     * @param alfa
     *            a parameter that affects the distance importance.
     */
    public static void wire(Graph g, double alfa) {

        // connect all the nodes other than roots
        for (int i = 1; i < Network.size(); ++i) {
            Node n = (Node) g.getNode(i);
            InetNodeProtocol prot = (InetNodeProtocol) n.getProtocol(pid);

            prot.isroot = false;

            // look for a suitable parent node between those allready part of
            // the overlay topology: alias FIND THE MINIMUM!
            Node candidate = null;
            int candidate_index = 0;
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < i; j++) {
                Node parent = (Node) g.getNode(j);

                double value = hops(parent, pid)
                        + (alfa * distance(n, parent, pid));
                if (value < min) {
                    candidate = parent; // best parent node to connect to
                    min = value;
                    candidate_index = j;
                }
            }
            prot.hops = ((InetNodeProtocol) candidate.getProtocol(pid)).hops+1;
            g.setEdge(i, candidate_index);
        }
    }

    /**
     * Return the graph distance in term of hops from the root(s). The distance
     * value is collected into the node itself.
     * 
     * @param node
     *            the node to inspect to get its graph distance we are
     *            interested in.
     * @param pid
     *            protocol identifier index.
     * @return the graph hops distance.
     */
    private static int hops(Node node, int pid) {
        return ((InetNodeProtocol) node.getProtocol(pid)).hops;
    }

    /**
     * Return the Euclidean distance based on the x,y coordinates of a node.
     * 
     * @param new_node
     *            the node to insert in the topology.
     * @param old_node
     *            a node allready part of the topology.
     * @param protocol
     *            identifier index.
     * @return the distance value.
     */
    private static double distance(Node new_node, Node old_node, int pid) {
        double x1 = ((InetNodeProtocol) new_node.getProtocol(pid)).x;
        double x2 = ((InetNodeProtocol) old_node.getProtocol(pid)).x;
        double y1 = ((InetNodeProtocol) new_node.getProtocol(pid)).y;
        double y2 = ((InetNodeProtocol) old_node.getProtocol(pid)).y;

        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}
