package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.util.IncrementalStats;

/**
 */
public class DegreeStats implements Observer {


// ===================== fields =======================================
// ====================================================================

/** 
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";
  
/** The name of this observer in the configuration */
private final String name;

private final int protocolID;


// ===================== initialization ================================
// =====================================================================


public DegreeStats(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	IncrementalStats stats = new IncrementalStats();
	OverlayGraph og = new OverlayGraph(protocolID);

	for(int i=0; i<og.size(); ++i)
		stats.add(og.getNeighbours(i).size());
	
	System.out.println(name+": "+stats);

	return false;
}

}

