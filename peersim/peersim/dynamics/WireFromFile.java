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
		
package peersim.dynamics;


import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import peersim.graph.Graph;
import peersim.core.*;
import peersim.config.Configuration;

/**
* Takes a {@link Linkable} protocol and adds connections that are stored in a
* file. Note that no
* connections are removed, they are only added. So it can be used in
* combination with other initializers.
* The format of the file is as follows. Each line begins with a node ID
* (IDs start from 0) followed by a list of neighbors, separated by whitespace.
* All node IDs larger than the actual network size will be discarded, but
* it does not trigger an error. Lines starting with a "#" character and
* empty lines are ignored.
*/
public class WireFromFile implements Dynamics {


// ========================= fields =================================
// ==================================================================


/** 
*  String name of the parameter used to select the protocol to operate on.
*/
public static final String PAR_PROT = "protocol";

/** 
*  String name of the parameter used to select the filename to load links from.
*/
public static final String PAR_FILE = "file";

private final int protocolID;

private final String file;


// ==================== initialization ==============================
// ==================================================================


public WireFromFile(String prefix) {

	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	file = Configuration.getString(prefix+"."+PAR_FILE);
}


// ===================== public methods ==============================
// ===================================================================


public void modify() {
try
{
	FileReader fr = new FileReader(file);
	LineNumberReader lnr = new LineNumberReader(fr);
	Graph g = new OverlayGraph(protocolID);
	String line;
	boolean wasOutOfRange=false;
	while((line=lnr.readLine()) != null)
	{
		if( line.startsWith("#") ) continue;
		StringTokenizer st = new StringTokenizer(line);
		if(!st.hasMoreTokens()) continue;
		int from = Integer.parseInt(st.nextToken());
		if( from < 0 || from >= Network.size() )
		{
			wasOutOfRange = true;
			continue;
		}
		while(st.hasMoreTokens())
		{
			int to = Integer.parseInt(st.nextToken());
			if( to < 0 || to >= Network.size() )
			{
				wasOutOfRange = true;
				continue;
			}
			g.setEdge(from,to);
		}
	}
	if( wasOutOfRange )
		System.err.println("WireFromFile warning: in "+file+" "+
			"some nodes were out of range and so ignored.");
}
catch( Exception e )
{
	throw new RuntimeException(e);
}

}

}
