package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.IncrementalStats;
import peersim.graph.*;

/**
* Prints reports on the graph like average clustering and average path length,
* based on random sampling of the nodes.
*/
public class GraphStats implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* Name for the parameter which defines the number of nodes to use fore
* sampling average path length.
* If zero is given, than no statistics
* will be printed about path length. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
*/
public static final String PAR_NL = "nl";

/** 
* Name for the parameter which defines the number of nodes to use to sample
* average clustering.
* If zero is given, than no statistics
* will be printed about clustering. If a negative value is given then
* the value is the full size of the graph.
* Defaults to zero.
*/
public static final String PAR_NC = "nc";

/** 
* If defined, the directed version of the graph will be analized, otherwise
* the undirected version.
* Not defined by default.
*/
public static final String PAR_DIR = "directed";

/** 
* If defined, the directed version of the graph will be analized, otherwise
* the undirected version.
* Not defined by default.
*/
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final int nc;

private final int nl;

private final boolean dir;

private final GraphAlgorithms ga = new GraphAlgorithms();


// ===================== initialization ================================
// =====================================================================


public GraphStats(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	nl = Configuration.getInt(name+"."+PAR_NL,0);
	nc = Configuration.getInt(name+"."+PAR_NC,0);
	dir = Configuration.contains(name+"."+PAR_DIR);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	System.out.print(name+": ");
	
	IncrementalStats stats = new IncrementalStats();
	Graph g = new OverlayGraph(protocolID);
	if( !dir && (nc!=0 || nl!=0)) g = new ConstUndirGraph(g);
	
	if( nc != 0 )
	{
		stats.reset();
		final int n = ( nc<0 ? g.size() : nc );
		for(int i=0; i<n && i<g.size(); ++i)
		{
			stats.add(GraphAlgorithms.clustering(g,i));
		}
		System.out.print(stats.getAverage()+" ");
	}
	
	if( nl != 0 )
	{
		stats.reset();
		final int n = ( nl<0 ? g.size() : nl );
		for(int i=0; i<n && i<g.size(); ++i)
		{
			ga.dist(g,i);
			for(int j=0; j<g.size(); ++j)
			{
				if( j==i ) continue;
				stats.add(ga.d[j]);
			}
		}
		System.out.print(stats.getAverage()+" ");
	}
	
	System.out.println();
	return false;
}

}

