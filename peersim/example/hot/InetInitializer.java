/*
 * HotInitializer.java
 *
 * Created on 7 aprile 2004, 16.32
 */

package example.hot;

import java.util.Iterator;
import java.io.*;
import peersim.config.Configuration;
import peersim.core.OverlayGraph;
import peersim.core.Node;
import peersim.core.Control;
import peersim.core.CommonState;
import peersim.util.IncrementalStats;

/**
 * This intialization class collects the simulation parameters from the config
 * file and invocates the suited factory. It collects in-degree statistics and
 * node coordinates and writes them to file; thus there is no real need of an
 * Control class. At the end of the initialization a specialized class (@see
 * RobustnessEvaluator) performs some topology robustness measurements.
 * 
 * @author Gian Paolo Jesi
 */
public class InetInitializer implements Control
{

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * String name of the parameter about the out degree value.
 * @config
 */
private static final String PAR_OUTDEGREE = "d";

/**
 * The weight. Default 0.5.
 * @config
 */
private static final String PAR_ALFA = "alfa";

/**
 * Maximum x/y coordinate. All the nodes are on a square region.
 * @config
 */
private static final String PAR_MAX_COORD = "max_coord";

private int pid; // protocol index

private int d; // out degree value

private double alfa; // alfa weight parameter

private String graph_filename = null; // file name sring to write out

private String dg_filename = null; // file name string to write out

private static final String DEBUG_STRING = "Inet.InetInitializer: ";

private static PrintWriter graph_fileout;

private static PrintWriter dg_fileout;

private double maxcoord;

/** Creates a new instance of HotInitializer */
public InetInitializer(String prefix)
{
	// super(prefix);
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	d = Configuration.getInt(prefix + "." + PAR_OUTDEGREE);
	alfa = Configuration.getDouble(prefix + "." + PAR_ALFA);
	graph_filename = "cmplxnet_d" + d + "_alfa" + alfa + ".dat";
	dg_filename = "degree_d" + d + "_alfa" + alfa + ".dat";
	maxcoord = Configuration.getDouble(prefix + "." + PAR_MAX_COORD, 1.0);
	if (!graph_filename.equals("")) {
		try {
			graph_fileout = new PrintWriter(new FileWriter(graph_filename));
			System.out.println(prefix + " filename: " + graph_filename + " selected");
		} catch (Exception e) {
			;
		}
	}
	if (!dg_filename.equals("")) {
		try {
			dg_fileout = new PrintWriter(new FileWriter(dg_filename));
			System.out.println(prefix + " filename: " + dg_filename + " selected");
		} catch (Exception e) {
			;
		}
	}
}

/** Initialize the graph according to the config file parameters */
public boolean execute()
{
	OverlayGraph ogr = new OverlayGraph(pid);
	InetFactory.InetTree(ogr, CommonState.r, pid, maxcoord, d, alfa);
	graphToFile(ogr);
	dgDistribToFile(ogr);
	// System.out.println("In-degree stats: "+degreeStats(ogr));
	RobustnessEvaluator rev = new RobustnessEvaluator(ogr);
	System.out.println("Metrica 1 : " + rev.getMetric1());
	try {
		String f1name = "fail-metric1-d" + d + "_alfa" + alfa + ".dat";
		PrintWriter fail1_fileout = new PrintWriter(new FileOutputStream(f1name));
		fail1_fileout.println(rev.getMetric1());
		fail1_fileout.close();
		String f2name = "fail-metric2-d" + d + "_alfa" + alfa + ".dat";
		PrintWriter fail2_fileout = new PrintWriter(new FileOutputStream(f2name));
		long[] m2res = rev.getMetric2();
		for (int i = 0; i < m2res.length; i++) {
			// System.out.print(m2res[i] + " ");
			fail2_fileout.println(i + "              " + m2res[i]);
		}
		fail2_fileout.close();
	} catch (Exception e) {
		System.out.println(e);
	}
	return false;
}

/**
 * Prints out statistics about the indegree distribution.
 * 
 * @param g
 *          current graph
 * @return The actual graph in-degree statistics.
 */
private String degreeStats(peersim.graph.Graph g)
{
	IncrementalStats is = new IncrementalStats();
	for (int i = 0; i < g.size(); i++) {
		Node n = (Node) g.getNode(i);
		is.add(((InetNodeProtocol) n.getProtocol(pid)).in_degree);
	}
	return is.toString();
}

/**
 * Prints out data to plot the topology using gnuplot a gnuplot style
 * 
 * @param g
 *          current graph
 */
private void graphToFile(peersim.graph.Graph g)
{
	if (graph_fileout != null) {
		try {
			for (int i = d; i < g.size(); i++) {
				Node current = (Node) g.getNode(i);
				double x_to = ((InetNodeProtocol) current.getProtocol(pid)).x;
				double y_to = ((InetNodeProtocol) current.getProtocol(pid)).y;
				Iterator it = (Iterator) g.getNeighbours(i).iterator();
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
		} catch (Exception e) {
			;
		}
	}
}

private void dgDistribToFile(peersim.graph.Graph g)
{
	if (dg_fileout != null) {
		int size = g.size();
		try {
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
				// System.out.println(k+" "+dgprob[i]);
				dg_fileout.println(k + " " + dgprob[i]);
			}
			dg_fileout.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
}
}
