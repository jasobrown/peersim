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

package traces;

import java.io.*;
import java.util.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.cdsim.CDState;

/**
 * This class reads a trace with the following format: <br>
 * <br>
 * <node id> <n° of sessions> <start-session-time1> <end-session-time1>
 * <start-session-time2> <end-session-time2> .... < br>
 * <br>
 * This format has been used in Saroiu, S., Gummadi, P. K., and Gribble, 
 * S. D. 2003. Measuring and analyzing the characteristics of Napster and 
 * Gnutella hosts. Multimedia Systems Journal 9, 2 (August), 170-184.
 * <br>
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class AvailabilityTraceDynamics implements Control
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/**
 * The file containing the session data. Currently based on the Saroiu
 * format.
 * @config
 */
private static final String PAR_FILE = "file";

/**
 * The length of a cycle.
 * @config
 */
private static final String PAR_CYCLE = "cycle";

/**
 * The number of time units in which a second is subdivided.
 * @config
 */
private static final String PAR_UNITS = "units";

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------
	
/** Name of the file containing the King measurements. */  
private String filename;

/** Cycle length */
private int cycleLength;

/** Number of time units contained in one second */
private int units;

/** 
 * Events related to nodes; if the node is currently active, it
 * is 
 */
private Node[][] events;

/** The prefix of this control */
private final String prefix;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * 
 */
public AvailabilityTraceDynamics(String prefix)
{
	this.prefix = prefix;
	filename = Configuration.getString(prefix+"."+PAR_FILE);
	cycleLength = Configuration.getInt(prefix+"."+PAR_CYCLE);
	units = Configuration.getInt(prefix+"."+PAR_UNITS, 1000);

	BufferedReader in = null;
	try {
		in = new BufferedReader(new FileReader(filename));
	} catch (FileNotFoundException e) {
		throw new IllegalParameterException(
			prefix+"."+PAR_FILE, filename + " does not exist");
	}
	String line = null;
	// Skip initial lines
	ArrayList<long[]> traces = new ArrayList<long[]>();
	IncrementalStats sessions = new IncrementalStats();
	IncrementalStats length = new IncrementalStats();
	int zn = 0;
	long max = 0;
	try { 
		while((line=in.readLine()) != null) {
			StringTokenizer tok = new StringTokenizer(line);
			tok.nextToken();
			int n = Integer.parseInt(tok.nextToken());
			long[] trace = new long[n*2];
			for (int i=0; i < n; i++) {
				long start = (long) (Double.parseDouble(tok.nextToken())*units);
				long stop = (long) (Double.parseDouble(tok.nextToken())*units);
				trace[i*2]   = start;
				trace[i*2+1] = stop;
				if (stop > max) {
					max = stop;
				}											
				long diff = stop-start;
				length.add(diff); 
			}
			if (n > 0) {
				sessions.add(n);
				traces.add(trace);
			} else {
				zn++;
			}
		}
	} catch (IOException e ) {
		e.printStackTrace(System.err);
		System.exit(0);	
	}
  System.err.println("ZERO " + zn + " SESSIONS " + sessions + " LENGTH " + length);
  
  
  /** 
   * Randomly assign one of the traces read from the file to
   * the existing nodes.
   */
  int[] indexes = new int[Network.size()];
	int size = 0;
	int[] selection = null;
	for (int i=0; i < indexes.length; i++) {
		if (size == 0) {
			size = traces.size();
			selection = new int[size];
			for (int j=0; j < size; j++) 
				selection[j] = j;
		}
		int r = CommonState.r.nextInt(size);
		indexes[i] = selection[r];
		selection[r] = selection[--size];
	}
  
  // From a trace array to an array of cycles
  ArrayList<Node>[] cycles = new ArrayList[(int) (max/cycleLength+1)];
  for (int i=0; i < cycles.length; i++) {
    cycles[i] = new ArrayList<Node>();
  }
  
  for (int i=0; i < Network.size(); i++) {
  	long[] trace = traces.get(indexes[i]);
  	for (int j=0; j < trace.length; j++) {
  		cycles[(int) (trace[j]/cycleLength)].add(Network.get(i)); 
  	}
  }
  
  // From ArrayList[] to Node[][]
  events = new Node[cycles.length][];
  for (int i=0; i < cycles.length; i++) {
  	if (cycles[i].size() != 0) {
  		events[i] = cycles[i].toArray(new Node[cycles[i].size()]);
  	}
  }
}

static int size = 0;

// Comment inherited from interface
public boolean execute()
{
	int cycle = CDState.getCycle();
	if (cycle == 0) {
		for (int i=0; i < Network.size(); i++) {
			Network.get(i).setFailState(Fallible.DOWN);
		}
		size = 0;
	}
	if (cycle < 0 )
	{
		// XXX make this class conform to all simulation models
		// panic: the wrong simulation model
		System.err.println("To use AvailibilityTraceDynamics, "+
		"you need to run a cycle drive simulation.");
		System.exit(1);
	}
	int down = 0;
	int up = 0;
	if (events[cycle] != null) {
		int size = events[cycle].length;
		for (int i=0; i < size; i++) {
			Node node = events[cycle][i];
			if (node.isUp()) {
				node.setFailState(Fallible.DOWN);
				down++;
			} else {
				node.setFailState(Fallible.OK);
				up++;
			}
		}
	}
	size += up - down;
	Log.println(prefix,  
			" TIME " + CDState.getCycle() +
			" EPOCH " + CDState.getCycle()/30 +
			" SIZE " + size +
			" DOWN " + down + 
			" UP " + up); 

	return false;
}

}
