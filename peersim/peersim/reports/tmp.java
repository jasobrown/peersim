package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.IncrementalStats;
import peersim.graph.*;

/**
* Observer to analyse the ball expansion, it the number of nodes that
* are accessable from a given node in at most 1, 2, etc steps.
* It works only after the simulation.
*/
public class tmp implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* Name for the parameter which defines the number of nodes to print info about.
* Defaults to size of the graph.
*/
public static final String PAR_N = "n";

/** 
* If defines, the undirected version of the graph will be analized.
* Not defined by default;
*/
public static final String PAR_UNDIR = "undir";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final int n;

private final boolean undir;

private final GraphAlgorithms ga = new GraphAlgorithms();


// ===================== initialization ================================
// =====================================================================


public tmp(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	n = Configuration.getInt(name+"."+PAR_N,Integer.MAX_VALUE);
	undir = Configuration.contains(name+"."+PAR_UNDIR);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	IncrementalStats stats = new IncrementalStats();
	Graph g = new OverlayGraph(protocolID);
	if( undir ) g = new ConstUndirGraph(g);

/*	for(int i=0; i<n && i<g.size(); ++i)
	{
		stats.add(GraphAlgorithms.clustering(g,i));
	}
*/	
	double cmean = stats.getAverage();
	stats.reset();
	
	for(int i=0; i<10 && i<g.size(); ++i)
	{
		ga.dist(g,i);
		for(int j=1; j<g.size(); ++j) stats.add(ga.d[j]);
	}
	
	System.out.println(name+": "+cmean+" "+stats.getAverage());
	
	return false;
}

}

