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

import peersim.core.*;
import peersim.reports.Observer;
import peersim.config.Configuration;
import java.io.*;

/**
* Normalizes the values..
*/
public class ValueDumper implements Observer {

/** 
* String name of the parameter that defines the protocol to initialize.
*/
public static final String PAR_PROT = "protocol";

/**
* This is the base name of the file where the graph is saved. The
* full name will be baseName+cycleid+".vec".
*/
public static final String PAR_BASENAME = "outf";

private final String name;

/** Protocol identifier */
private final int protocolID;

private final String baseName;


//--------------------------------------------------------------------------

public ValueDumper(String prefix)
{
	name = prefix;
	protocolID = Configuration.getPid(prefix+"."+PAR_PROT);
	baseName = Configuration.getString(name+"."+PAR_BASENAME,null);
}

//--------------------------------------------------------------------------

private static String format(long l, long max) {
	
	String ss = String.valueOf(max);
	String sc = String.valueOf(l);
	StringBuffer sb = new StringBuffer(ss);
	for(int i=0; i<ss.length()-sc.length(); ++i) sb.setCharAt(i,'0');
	sb.replace(ss.length()-sc.length(),ss.length(),sc);
	return sb.toString();
}

// ---------------------------------------------------------------------

public boolean analyze() {
try {	
	System.out.print(name+": ");
	
	// initialize output streams
	FileOutputStream fos = null;
	PrintStream pstr = System.out;
	if( baseName != null )
	{
		final String filename = baseName+
			ValueDumper.format(CommonState.getTime(),10000)+"-"+
			ValueDumper.format(
				CommonState.getCycleT(),Network.size())
			+".vec";
		fos = new FileOutputStream(filename);
		System.out.println(filename);
		pstr = new PrintStream(fos);
	}
	else	System.out.println();
	
	for(int i=0; i<Network.size(); ++i)
	{
		SingleValue sv =
			(SingleValue)Network.get(i).getProtocol(protocolID);
		pstr.println(sv.getValue());
	}

	return false;
	
}
catch( IOException e )
{
	System.err.println(name+": Unable to write to file: "+e);
	return true;
}
}

}



