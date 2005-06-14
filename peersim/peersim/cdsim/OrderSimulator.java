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
		
package peersim.cdsim;

import java.util.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
* This is the executable class for performing a cycle driven simulation.
* The class is completely static as at the same time we expect to have only
* one simulation running in a virtual machine.
* The simulation is highly configurable.

Using this class, ou can write an order specification in the configuration
like this:
<pre>
order observer.0,protocol.sort,observer.2,protocol.rank,dynamics.crash
</pre>
deciding the exact order in which you want to execute everything (except
initializers, that are executed at begininning, ordered by order.init,
as with the other Simulators).
You can also write:
<pre>
order 0,sort,2,rank,crash
</pre>
but all the names should be unique, otherwise it will print a message
like this:
<pre>
Parameter "order": Item 0 corresponds to observer.0, dynamics.0, protocol.0
</pre>
And clearly, the item name should exist, otherwise you will have
errors like this:
<pre>
order pippo

Parameter "order": Item pippo does not corresponds to any of the 
observers, dynamics or protocols
</pre>
You can even shuffle the network before each protocol, and not
just before each cycle, using the parameter 
<pre>
simulation.shuffle.protocol.
</pre>
*/
public class OrderSimulator extends Simulator{

// ============== constants ============================================
// =====================================================================

/** Observer id */
private static final int OBS = 0;	
	
/** Dynamics id */
private static final int DYN = 1;	

/** Protocol id */
private static final int PROT = 2;	

/**
 * If this parameter is present, the order of visiting each node is shuffled
 * before each protocol. The default is no shuffle.
 */
public static final String PAR_PSHUFFLE = "simulation.shuffle.protocol";

/**
 * This is the prefix for order specifications.
 */
public static final String PAR_ORDER = "simulation.order";

// --------------------------------------------------------------------

private static boolean protocolshuffle;

/** 
 * Holds the general order in which all objects should be executed;
 * alternative to the previous variables.
 */
private static int[] index = null;

/**
 * Holds the type of objects (OBS, DYN, PROT) corresponding to the
 * index array.
 */
private static int[] types = null;


// =============== protected methods ===================================
// =====================================================================

/**
 * Return the index of string s in the specified array.
 * If the string is not present, returns array.length.
 */
protected static int searchString(String[] array, String s)
{
	int j;
	for (j=0; j < array.length; j++)
		if (s.equals(array[j]))
			break;
	return j;
}


//---------------------------------------------------------------------

/**
 * Execute protocol pid. Used by the new kind of
 * experiment, where single protocols can be executed
 * between observer, in any order.
 */
protected static void nextRound(int cycle, int pid) {

	if( protocolshuffle )
	{
		Network.shuffle();
	}
	
	for(int j=0; j<Network.size(); ++j)
	{
		Node node = null;
		if( getpair_rand )
			node = Network.get(
			   CommonRandom.r.nextInt(Network.size()));
		else
			node = Network.get(j);
		if( !node.isUp() ) continue; 
		CommonState.setNode(node);
		CommonState.setCycleT(j);
		if (!protSchedules[pid].active(cycle))
			continue;
				
		CommonState.setPid(pid);
		Protocol protocol = node.getProtocol(pid);
		if( protocol instanceof CDProtocol )
		{
			((CDProtocol)protocol).nextCycle(node, pid);
		}
	}
}

//---------------------------------------------------------------------

/**
 * New style of simulation. Everything is specified by the 
 * PAR_ORDER parameter.
 */
public static void nextExperiment() 
{
	// Reading parameter
	cycles = Configuration.getInt(PAR_CYCLES);
	String order = Configuration.getString(PAR_ORDER);
	shuffle = Configuration.contains(PAR_SHUFFLE);
	getpair_rand = Configuration.contains(PAR_GETPAIR);
	protocolshuffle = Configuration.contains(PAR_PSHUFFLE);

	// initialization
	System.err.println("OrderSimulator: resetting");
	Network.reset();
	System.err.println("OrderSimulator: running initializers");
	// Initializers are run at 
	// cycle 0 (cycle-driven) / time 0 (event-driven)
	CommonState.setCycle(0);
	CommonState.setPhase(CommonState.PRE_DYNAMICS);
	runInitializers();
	
	String[] obsNames = loadObservers();
	String[] dynNames = loadDynamics();
	String[] protNames = Configuration.getNames(Node.PAR_PROT);
	loadProtocolSchedules();
	
	StringTokenizer token = new StringTokenizer(order, ",");
	int size = token.countTokens();
	index = new int[size];
	types = new int[size];
	for (int i=0; i < size; i++) {
		String item = token.nextToken().trim();
		if (item.startsWith(PAR_OBS)) {
			// It's an observer
			index[i] = searchString(obsNames, item);
			if (index[i] == obsNames.length) {
				throw new IllegalParameterException(PAR_ORDER,
				"observer." + item + " is not defined.");
			}
			types[i] = OBS;
		} else if (item.startsWith(PAR_DYN)) {
			// It's a dynamics
			index[i] = searchString(dynNames, item);
			if (index[i] == dynNames.length) {
				throw new IllegalParameterException(PAR_ORDER,
				"dynamics." + item + " is not defined.");
			}
			types[i] = DYN;
		} else if (item.startsWith(Node.PAR_PROT)) {
			// It's a protocol
			index[i] = searchString(protNames, item);
			if (index[i] == protNames.length) {
				throw new IllegalParameterException(PAR_ORDER,
				"protocol." + item + " is not defined.");
			}
			types[i] = PROT;
		} else {
			// Uhmmm... let's search in all of them; 
			int iobs  = searchString(obsNames, PAR_OBS+"."+item);
			int idyn  = searchString(dynNames, PAR_DYN+"."+item);
			int iprot = searchString(protNames, Node.PAR_PROT+"."+item);
			int count = 0; // Count the number of found names
			String error = "";
			if (iobs < obsNames.length) {
				index[i] = iobs;
				types[i] = OBS;
				count++;
				error = error + " " + obsNames[iobs];
			}
			if (idyn < dynNames.length) {
				index[i] = idyn;
				types[i] = DYN;
				count++;
				error = error + (count>0 ? ", ": " ") + dynNames[idyn];
			}
			if (iprot < protNames.length) {
				index[i] = iprot;
				types[i] = PROT;
				count++;
				error = error + (count>0 ? ", ": " ") + protNames[iprot];
			}
			if (count == 0) {
				throw new IllegalParameterException(PAR_ORDER,
						"Item " + item + " does not corresponds to any of the " +
						"observers, dynamics or protocols");
			} else if (count > 1) {
				throw new IllegalParameterException(PAR_ORDER,
						"Item " + item + " corresponds to" + error);
			}
		}		
	}

	System.err.println("OrderSimulator: starting simulation");
	boolean stop = false;
	for(int i=0; i<cycles && !stop; ++i) {

		// In cycle-driven simulations, the concept of "time" corresponds
		// the concept of cycle; so both the cycle and the time are set
		// to the equal value.
		CommonState.setCycle(i);

		// Shuffling pre-cycle
		if( shuffle )
		{
			Network.shuffle();
		}
		
		for (int j=0; j < size && !stop; j++) {
			int l=index[j];
			switch (types[j]) {
				case OBS:
					if( obsSchedules[l].active(i) &&
					    !obsSchedules[l].preCycle() )
						stop = stop || observers[l].analyze();
					break;
			  case DYN:
					if( dynSchedules[l].active(i) ) dynamics[l].modify();
			  	break;
			  case PROT:
			  	nextRound(i, l);
			  	break;
			}
		}
		System.err.println("OrderSimulator: cycle "+i+" done");
	}

	CommonState.setPhase(CommonState.POST_LAST_CYCLE);

	// analysis after the simulation
	for(int j=0; j<size; ++j)
	{
		if (types[j] == OBS) {
			int l = index[j];
			if( obsSchedules[l].fin() ) 
				observers[l].analyze();
		}
		if (types[j] == DYN) {
			int l = index[j];
			if( dynSchedules[l].fin() ) 
				dynamics[l].modify();
		}
	}
}

}


