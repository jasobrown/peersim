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
import peersim.graph.*;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
* Prints the whole graph in a given format.
*/
public class GraphPrinter implements Observer {


// ===================== fields =======================================
// ====================================================================

/**
*  String name of the parameter used to select the protocol to operate on
*/
public static final String PAR_PROT = "protocol";

/**
* This is the base name of the file where the graph is saved. The
* full name will be baseName+cycleid.extension where extension depends
* on the format used. If no basename is given, the graph is dumped on
* the standard output.
*/
public static final String PAR_BASENAME = "outf";

/**
* The name for the format of the output. Defaults to "neighborlist",
* which is a plain dump of neighbors. The class
* {@link peersim.dynamics.WireFromFile} can read this format.
* Other supported formats are "chaco" to be used with Yehuda Koren's
* Embedder, and "netmeter" to be used with Sergi Valverde's netmeter.
*/
public static final String PAR_FORMAT = "format";

/** 
* If defined, the undirected version of the graph will be printed, otherwise
* the directed version.
* Not defined by default.
*/
public static final String PAR_UNDIR = "undirected";

private final int protocolID;

/** The name of this observer in the configuration */
private final String name;

private final String baseName;

private final String format;

private final boolean undir;

// ===================== initialization ================================
// =====================================================================


public GraphPrinter(String name) {

	this.name = name;
	protocolID = Configuration.getPid(name+"."+PAR_PROT);
	baseName = Configuration.getString(name+"."+PAR_BASENAME,null);
	format = Configuration.getString(name+"."+PAR_FORMAT,"neighborlist");
	undir = Configuration.contains(name+"."+PAR_UNDIR);
}


// ====================== methods ======================================
// =====================================================================


public boolean analyze() {
try {	
	Graph og = new OverlayGraph(protocolID);
	if( undir ) og = new ConstUndirGraph(og);
	
	System.out.print(name+": ");
	
	// initialize output streams
	FileOutputStream fos = null;
	PrintStream pstr = System.out;
	if( baseName != null )
	{
		fos = new FileOutputStream(
			baseName+CommonState.getTime()+".graph");
		System.out.println("writing to file "+
			baseName+CommonState.getTime()+".graph");
		pstr = new PrintStream(fos);
	}
	else	System.out.println();
	
	if( format.equals("neighborlist") )
		GraphIO.writeNeighborList(og, pstr);
	else if( format.equals("chaco") )
		GraphIO.writeChaco(og, pstr);
	else if( format.equals("netmeter") )
		GraphIO.writeNetmeter(og, pstr);
	else
		System.err.println(name+": unsupported format "+format);
	
	if( fos != null ) fos.close();
	
	return false;
}
catch( IOException e )
{
	System.err.println(name+": Unable to write to file: "+e);
	return true;
}
}

}

