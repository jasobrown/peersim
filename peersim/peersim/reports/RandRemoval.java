package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.graph.*;
import peersim.util.IncrementalStats;
import java.util.Map;
import java.util.Iterator;

/**
* It test the network for sensiticity for random node removal.
* Of course it does not actually remove nodes, it is only an observer.
*/
public class RandRemoval implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final GraphAlgorithms ga;


// ===================== initialization ================================
// =====================================================================


public RandRemoval(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	ga = new GraphAlgorithms();
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	OverlayGraph og = new OverlayGraph(protocolID);
	PrefixSubGraph g = new PrefixSubGraph(og);
	IncrementalStats stats = new IncrementalStats();
	
	System.out.println(name+":");
	
	for( int i=og.size()/2; i>0; i-=og.size()/100)
	{
		g.setSize(i);
		Map clst = ga.weaklyConnectedClusters(g);
		stats.reset();
		Iterator it = clst.values().iterator();
		while(it.hasNext())
		{
			stats.add(((Integer)it.next()).intValue());
		}

		System.out.println(stats);
	}
	
	return false;
}

}
