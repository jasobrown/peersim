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

package peersim.vector;

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Initializes nodes from data read from a file.
 * Assumes nodes implement {@link SingleValue}.
 */
public class InitVectFromFile implements Dynamics {

// ==================== fields ===========================================
// =======================================================================


/** 
 * String name of the parameter that defines the protocol to initialize.
 * Parameter read will has the full name <tt>prefix+"."+PAR_PROT</tt>
 */
public static final String PAR_PROT = "protocol";

/** 
*  String name of the parameter used to select the filename to load links from.
*/
public static final String PAR_FILE = "file";

private final int protocolID;

private final String file;


// ===================== initialization ==================================
// =======================================================================


/**
 * Reads configuration parameters.
 */
public InitVectFromFile(String prefix)
{
	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	file = Configuration.getString(prefix+"."+PAR_FILE);
}


// ===================== methods =========================================
// =======================================================================


/**
* Initializes values from a file. The file has to contain one value per line.
* Lines starting with # or lines that contain only whitespace are ignored.
* The file can contain more values than necessary but enough values must be
* present.
*/
public void modify() {
	
	int i = 0;
	
	try
	{
		FileReader fr = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(fr);
		String line;
		while( (line=lnr.readLine()) != null && i < Network.size() )
		{
			if( line.startsWith("#") ) continue;
			StringTokenizer st = new StringTokenizer(line);
			if(!st.hasMoreTokens()) continue;
			double val = Double.parseDouble(st.nextToken());
			((SingleValue)Network.get(i).getProtocol(protocolID)
				).setValue(val);
			i++;
		}
	}
	catch( Exception e )
	{
		throw new RuntimeException(e);
	}
	
	if(i<Network.size()) throw new RuntimeException(
		"Too few values in file '"+file+"', only "+i+", needed "+
		Network.size()+".");
}

}

