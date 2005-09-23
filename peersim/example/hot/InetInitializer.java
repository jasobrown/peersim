/*
 * HotInitializer.java
 *
 * Created on 7 aprile 2004, 16.32
 */

package example.hot;

import java.util.ArrayList;
import java.util.Random;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.graph.Graph;

/**
 * This intialization class collects the simulation parameters from the config
 * file and invocates the suited factory. It collects in-degree statistics and
 * node coordinates and writes them to file; thus there is no real need of a
 * Control class. At the end of the initialization a specialized class (@see
 * RobustnessEvaluator) performs some topology robustness measurements.
 * 
 * @author Gian Paolo Jesi
 */
public class InetInitializer implements Control {

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * String name of the parameter about the out degree value.
     * 
     * @config
     */
    private static final String PAR_OUTDEGREE = "d";

    /**
     * Maximum x/y coordinate. All the nodes are on a square region.
     * 
     * @config
     */
    private static final String PAR_MAX_COORD = "max_coord";

    private static int pid; // protocol index

    private int d; // out degree value

    private static final String DEBUG_STRING = "Inet.InetInitializer: ";

    private double maxcoord;

    /** Creates a new instance of HotInitializer */
    public InetInitializer(String prefix) {
        // super(prefix);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        d = Configuration.getInt(prefix + "." + PAR_OUTDEGREE);
        maxcoord = Configuration.getDouble(prefix + "." + PAR_MAX_COORD, 1.0);
    }

    /**
     * Initialize the node coordinates and the current in-degree. The wiring is
     * not performed here.
     */
    public boolean execute() {
        Random rnd = CommonState.r;
        System.out.println(DEBUG_STRING + "size: " + Network.size()
                + " outdegree: " + d);

        // build outdegree roots
        System.out.println(DEBUG_STRING + "Generating " + d
                + " root(s), means out degree " + d + "...");
        for (int i = 0; i < d; ++i) {
            Node n = (Node) Network.get(i);
            InetNodeProtocol prot = (InetNodeProtocol) n.getProtocol(pid);
            prot.isroot = true;
            prot.hops = 0;
            prot.in_degree = 0;
            if (d == 1) {
                prot.x = maxcoord / 2;
                prot.y = maxcoord / 2;
            } else { // more than one root
                if (rnd.nextBoolean()) {
                    prot.x = maxcoord / 2 + (rnd.nextDouble() * 0.1);
                } else {
                    prot.x = maxcoord / 2 - (rnd.nextDouble() * 0.1);
                }
                if (rnd.nextBoolean()) {
                    prot.y = maxcoord / 2 + (rnd.nextDouble() * 0.1);
                } else {
                    prot.y = maxcoord / 2 - (rnd.nextDouble() * 0.1);
                }
                System.out.println("root coord: " + prot.x + " " + prot.y);
            }
        }

        // Set coordinates x,y and set indegree 0
        System.out.println(DEBUG_STRING
                + "Generating random cordinates for nodes...");
        for (int i = d; i < Network.size(); i++) {
            Node n = (Node) Network.get(i);
            InetNodeProtocol prot = (InetNodeProtocol) n.getProtocol(pid);
            if (maxcoord == 1.0) {
                prot.x = rnd.nextDouble();
                prot.y = rnd.nextDouble();
            } else {
                prot.x = rnd.nextInt((int) maxcoord);
                prot.y = rnd.nextInt((int) maxcoord);
            }
            prot.in_degree = 0;
        }
        return false;
    }

    // Default method for wiring.
    public static void wire(Graph g, int outDegree, double alfa) {
        wireHOTOverlay(g, outDegree, alfa);
    }

    // Do the actual wiring.
    public static void wireHOTOverlay(Graph g, int outDegree, double alfa) {
        // Connect the roots in a ring if needed (thus, if there are more than 1
        // root nodes.
        if (outDegree > 1) {
            System.out.println(DEBUG_STRING + "Putting roots in a ring...");
            for (int i = 0; i < outDegree; i++) {
                Node n = (Node) g.getNode(i);
                ((InetNodeProtocol) n.getProtocol(pid)).in_degree++;
                n = (Node) g.getNode(i + 1);
                ((InetNodeProtocol) n.getProtocol(pid)).in_degree++;

                g.setEdge(i, i + 1);
                g.setEdge(i + 1, i);
            }
            Node n = (Node) g.getNode(0);
            ((InetNodeProtocol) n.getProtocol(pid)).in_degree++;
            n = (Node) g.getNode(outDegree);
            ((InetNodeProtocol) n.getProtocol(pid)).in_degree++;
            g.setEdge(0, outDegree);
            g.setEdge(outDegree, 0);
        }

        // for all the nodes other than root(s), connect them!
        for (int i = outDegree; i < Network.size(); ++i) {
            // System.out.println(DEBUG_STRING+"Inserting node "+i);
            Node n = (Node) g.getNode(i);
            InetNodeProtocol prot = (InetNodeProtocol) n.getProtocol(pid);

            prot.isroot = false;

            // look for a suitable parent node between those allready part of
            // the overlay topology: alias FIND THE MINIMUM!
            Node candidate = null;
            int candidate_index = 0;
            double min = Double.POSITIVE_INFINITY;
            if (outDegree > 1) {
                int candidates[] = getParents(g, pid, i, outDegree, alfa);
                // System.out.print(DEBUG_STRING+"setting node "+i+" pointing to
                // nodes: ");
                for (int s = 0; s < candidates.length; s++) {
                    g.setEdge(i, candidates[s]);
                    Node nd = (Node) g.getNode(candidates[s]);
                    InetNodeProtocol prot_parent = (InetNodeProtocol) nd
                            .getProtocol(pid);
                    prot_parent.in_degree++;
                    // System.out.print(i+", ");
                }
                // sets hop
                prot.hops = minHop(g, candidates, pid) + 1;
                // System.out.println();
            } else { // degree 1:
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
                prot.hops = ((InetNodeProtocol) candidate.getProtocol(pid)).hops + 1;
                g.setEdge(i, candidate_index);
                ((InetNodeProtocol) candidate.getProtocol(pid)).in_degree++;
                // System.out.println(DEBUG_STRING+"setting node "+i+" pointing
                // to
                // node "+candidate_index+" with value: "+min);
            }
        }
    }

    /**
     * Return the array of node indexes suitable for the current node to be
     * connected to. This function is useful when the outdegree is > 1 and thus
     * we need more than one (exactly as outdegree in our model) outbound
     * connection for each new node.
     * 
     * @param g
     *            the Graph object inteface to deal with
     * @param pid
     *            the protocol index identifier
     * @param cur_node_index
     *            the index of the current node to insert in the topology
     * @param how_many
     *            howmany candidates are needed (the out degree)
     * @param alfa
     *            the weight formula parameter
     * @return an array of node indexes
     */
    private static int[] getParents(Graph g, int pid, int cur_node_index,
            int how_many, double alfa) {
        int result[] = new int[how_many];
        ArrayList net_copy = new ArrayList(cur_node_index);
        // fill up the sub net copy:
        for (int j = 0; j < cur_node_index; j++) {
            net_copy.add(j, (Node) g.getNode(j));
        }

        // it needs exactly how_many minimums!
        for (int k = 0; k < how_many; k++) {
            int candidate_index = 0;
            double min = Double.POSITIVE_INFINITY;
            // for all the elements in the copy...
            for (int j = 0; j < net_copy.size(); j++) {
                Node parent = (Node) net_copy.get(j);
                double value = hops(parent, pid)
                        + (alfa * distance((Node) g.getNode(cur_node_index),
                                parent, pid));

                if (value < min) {
                    min = value;
                    candidate_index = j;
                }
            }
            result[k] = candidate_index; // collect the parent node
            net_copy.remove(candidate_index); // delete the min from the net
                                                // copy
        }
        return result;
    }

    /**
     * Return the graph distance in term of hops from the root(s). The distance
     * value is collected into the node itself.
     * 
     * @param node
     *            the node to inspect to get its graph distance we are
     *            interested in
     * @param pid
     *            protocol identifier index
     * @return the graph hops distance
     */
    private static int hops(Node node, int pid) {
        return ((InetNodeProtocol) node.getProtocol(pid)).hops;
    }

    /**
     * Return the minimum hop valued node between the specified nodes.
     * 
     * @param g
     *            The Graph inteface to get access to nodes and node protocols
     * @param indexes
     *            array of node indexes to use with the Graph interface
     * @param pid
     *            protocol identifier index
     * @return the hop minimum value
     */
    private static int minHop(Graph g, int[] indexes, int pid) {
        int min = Integer.MAX_VALUE;
        for (int s = 0; s < indexes.length; s++) {
            Node parent = (Node) g.getNode(indexes[s]);
            int value = ((InetNodeProtocol) parent.getProtocol(pid)).hops;
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Return the Euclidean distance based on the x,y coordinates of a node.
     * 
     * @param new_node
     *            the node to insert in the topology
     * @param old_node
     *            a node allready part of the topology
     * @param protocol
     *            identifier index
     * @return the distance value
     */
    private static double distance(Node new_node, Node old_node, int pid) {
        double x1 = ((InetNodeProtocol) new_node.getProtocol(pid)).x;
        double x2 = ((InetNodeProtocol) old_node.getProtocol(pid)).x;
        double y1 = ((InetNodeProtocol) new_node.getProtocol(pid)).y;
        double y2 = ((InetNodeProtocol) old_node.getProtocol(pid)).y;

        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }
}
