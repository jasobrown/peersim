package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.graph.GraphAlgorithms;
import peersim.util.IncrementalStats;
import java.util.Map;
import java.util.Iterator;

/**
 */
public class ConnectivityObserver implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/** 
* String name of the parameter used to request cluster size statistics instead
* of the usual list of clusters. By default not set.
*/
public static final String PAR_SIZESTATS = "sizestats";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;

private final boolean sizestats;

private final GraphAlgorithms ga;


// ===================== initialization ================================
// =====================================================================


public ConnectivityObserver(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
	sizestats = Configuration.contains(name+"."+PAR_SIZESTATS);
	ga = new GraphAlgorithms();
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	OverlayGraph og = new OverlayGraph(protocolID);

	if(!sizestats)
	{
		System.out.println(name+": "+ga.weaklyConnectedClusters(og));
	}
	else
	{
		Map clst = ga.weaklyConnectedClusters(og);
		IncrementalStats stats = new IncrementalStats();
		Iterator it = clst.values().iterator();
		while(it.hasNext())
		{
			stats.add(((Integer)it.next()).intValue());
		}

		System.out.println(name+": "+stats);
	}
	
	return false;
}

}
