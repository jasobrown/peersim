/*
 * Copyright (c) 2003-2005 The BISON Project
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

package peersim.extras.am.transport;

import java.io.*;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.*;


/**
 * This class uses static data structures; in a single configuration
 * file, it can appear at most once.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class TraceTransport implements Transport
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/**
 * This configuration parameter identifies the filename of the file
 * containing the measurements. First, the file is used as a pathname 
 * in the local file system. If no file can be identified in this way, 
 * the file is searched in the local classpath. If the file cannot be 
 * identified again, an error message is reported.
 * @config
 */
private static final String PAR_FILE = "file";

/**
 * The time unit contained in the loaded file and the time unit used
 * during the simulation may be different. This parameter express
 * how many "simulation" time units are contained in a "file" time
 * unit.
 * 
 * @config
 */
private static final String PAR_RATIO = "unit";

//---------------------------------------------------------------------
// Static fields
//---------------------------------------------------------------------

/** Identifier of this transport protocol */
private static int tid;

/** Container for PAR_RATIO */
private static double ratio;

/**
 * True if latency between nodes is considered symmetric. False otherwise.
 */
private static boolean symm;	
	
/**
 * Size of the router network. 
 */
private static int size;

/**
 * Latency distances between nodes. To save memory, we use integers
 * instead of longs.
 */
private static int[][][] array;

/** 
 * Name of the file containing the measurements. 
 */
private static String filename;

/** 
 * Prefix for reading parameters 
 */
private static String prefix;

	
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Identifier of the internal node */
private int router1 = -1;

private int router2 = -1;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters.
 */
public TraceTransport(String prefix)
{
	tid = CommonState.getPid();
	filename = Configuration.getString(prefix + "." + PAR_FILE);
	ratio = Configuration.getDouble(prefix + "." + PAR_RATIO);

	try {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(filename)));
			System.err.println("TraceTransport: Reading " + filename + " from local file system");
		} catch (FileNotFoundException e) {
			in = new ObjectInputStream(
					new BufferedInputStream(
							ClassLoader.getSystemResourceAsStream(filename)));
			System.err.println("TraceTransport: Reading " + filename + " through the class loader");
		}
	
		// Read the number of nodes in the file (first four bytes).
	  int size = in.readShort();
		System.err.println("TraceTransport: reading " + size + " rows");
	  
		// If the file format is not correct, data will be read 
		// uncorrectly. Probably a good way to spot this is the 
		// presence of negative delays, or an end of file.
	
		// Reading data
		int count = 0;
		array = new int[size][][];
		for (int r=0; r < size; r++) {
			array[r] = new int[r][];
			for (int c = 0; c < r; c++) {
				int len = in.readShort();
				count++;
				array[r][c] = new int[len];
				for (int i=0; i < len; i++) {
					array[r][c][i] = Math.max(1, (int) ratio*in.readShort());
				}
			}
		}
		System.err.println("TraceTransport: Read " + count + " entries");
	} catch (IOException e) {
		throw new RuntimeException(e.getMessage());
	}
}

//---------------------------------------------------------------------

/**
 * Clones the object.
 */
public Object clone()
{
	try { 
		TraceTransport tt = (TraceTransport) super.clone();
		int r1 = CommonState.r.nextInt(array.length);
		int r2 = r1;
		while (r1 == r2) 
			r2 = CommonState.r.nextInt(array.length);
		tt.setRouter(r1, r2);
		return tt;
	} catch( CloneNotSupportedException e ) { 
		return null; // never happens 
	} 
}

//---------------------------------------------------------------------
//Methods inherited by Transport
//---------------------------------------------------------------------

/**
* Delivers the message reliably, with the latency calculated by
* {@link #getLatency}.
*/
public void send(Node src, Node dest, Object msg, int pid)
{
	long latency = getLatency(src,dest);
	if (latency > 0) {
		EDSimulator.add(latency, msg, dest, pid);
//		System.out.println(latency/1000);
	} 
//	else
//		System.out.println("+");
}

//---------------------------------------------------------------------

/**
 * 
 */
public long getLatency(Node src, Node dest)
{
	/* Assuming that the sender corresponds to the source node */
	TraceTransport sender = (TraceTransport) src.getProtocol(tid);
	TraceTransport receiver = (TraceTransport) dest.getProtocol(tid);
	long latency;
	int r1 = Math.max(sender.router1, receiver.router1);
	int r2 = Math.min(sender.router1, receiver.router1);
	if (r1 == r2) {
		r1 = Math.max(sender.router2, receiver.router2);
		r2 = Math.min(sender.router2, receiver.router2);
	}
	if (r1 == r2 || array[r1][r2].length == 0) {
		r1 = Math.max(sender.router2, receiver.router1);
		r2 = Math.min(sender.router2, receiver.router1);
	}
	if (r1 == r2 || array[r1][r2].length == 0) {
		r1 = Math.max(sender.router1, receiver.router2);
		r2 = Math.min(sender.router1, receiver.router2);
	}
	
	
	// Not nice, just a try
	
//	while (r1 == r2 || array[r1][r2].length == 0) {
//		r1 = (r1+1)%array.length;
//	}
	
	if (r1 == r2 || array[r1][r2].length == 0) {
		return -1;
	} else {
		int r = CommonState.r.nextInt(array[r1][r2].length);
		return array[r1][r2][r];
	}
}


//---------------------------------------------------------------------
//Methods inherited by RouterInfo
//---------------------------------------------------------------------

// Javadoc inherited
public void setRouter(int r1, int r2)
{
	this.router1 = r1;
	this.router2 = r2;
}

}
