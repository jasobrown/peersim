package scamp;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.reports.Observer;

/**
 */
public class HealthTest implements Observer {


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


public HealthTest(String name) {

	this.name = name;
	protocolID = Configuration.getInt(name+"."+PAR_PROT);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
	
	System.out.println(name+": "+Scamp.test(protocolID));

	return false;
}

// ---------------------------------------------------------------------

public void finalAnalyze() { analyze(); }

}

