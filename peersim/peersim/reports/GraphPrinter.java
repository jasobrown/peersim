/*
 * Copyright (c) 2003 The BISON Project
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

