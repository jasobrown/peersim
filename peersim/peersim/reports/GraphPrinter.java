package peersim.reports;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.graph.GraphIO;

/**
 */
public class GraphPrinter implements Observer {


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


public GraphPrinter(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {

	System.out.println(name+":");
	OverlayGraph og = new OverlayGraph(protocolID);
	//GraphIO.writeUCINET_DL(og,System.out);
	GraphIO.writeEdgeList(og,System.out);
	return false;
}
}

