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
import peersim.dynamics.*;
import peersim.reports.Observer;
import peersim.util.*;

/**
* This is the executable class for performing a cycle driven simulation.
* The class is completely static as at the same time we expect to have only
* one simulation running in a virtual machine.
* The simulation is highly configurable.
*/
public class Simulator {

// ============== constants ============================================
// =====================================================================

/** Observer id */
private static final int OBS = 0;	
	
/** Dynamics id */
private static final int DYN = 1;	

/** Protocol id */
private static final int PROT = 2;	

// ============== fields ===============================================
// =====================================================================

/** 
 * Parameter representing the maximum number of cycles to be performed 
 */
public static final String PAR_CYCLES = "simulation.cycles";

/**
 * If this parameter is present, the order of visiting each node is shuffled
 * at each cycle. The default is no shuffle.
 */
public static final String PAR_SHUFFLE = "simulation.shuffle";

/**
 * If this parameter is present, the order of visiting each node is shuffled
 * before each protocol. The default is no shuffle.
 */
public static final String PAR_PSHUFFLE = "simulation.shuffle.protocol";

/**
 * Parameter representing the number of times the experiment is run.
 * Defaults to 1.
 */
public static final String PAR_EXPS = "simulation.experiments";

/**
* The type of the getPair function. Defaults to "seq".
*/
public static final String PAR_GETPAIR = "simulation.getpair";

/**
 * This is the prefix for initializers. These have to be of type
 * {@link Dynamics}.
 */
public static final String PAR_INIT = "init";

/**
 * This is the prefix for network dynamism managers. These have to be of
 * type {@link Dynamics}.
 */
public static final String PAR_DYN = "dynamics";

/**
 * This is the prefix for observers. These have to be of type
 * {@link Observer}.
 */
public static final String PAR_OBS = "observer";

/**
 * This is the prefix for order specifications.
 */
public static final String PAR_ORDER = "order";


// --------------------------------------------------------------------

/** The maximum number of cycles to be performed */
private static int cycles;

private static boolean shuffle;

private static boolean protocolshuffle;

/** The number of independent restarted simulations to be performed */
private static int exps;

private static boolean getpair_rand;

/** holds the observers of this simulation */
private static Observer[] observers=null;

/** holds the modifiers of this simulation */
private static Dynamics[] dynamics=null;

/** Holds the observer schedulers of this simulation */
private static Scheduler[] obsSchedules = null;

/** Holds the dynamics schedulers of this simulation */
private static Scheduler[] dynSchedules = null;

/** Holds the protocol schedulers of this simulation */
private static Scheduler[] protSchedules = null;

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
 * Load and run initializers, using the order specified by
 * PAR_ORDER+"."+PAR_INIT if present.
 */
protected static void runInitializers() {
	
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);
	String names[] = Configuration.getNames(PAR_INIT);
	
	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " +names[i]+ ": " + inits[i].getClass());
		((Dynamics)inits[i]).modify();
	}
}

// --------------------------------------------------------------------

protected static String[] loadObservers()
{
	// load observers
	String[] names = Configuration.getNames(PAR_OBS);
	observers = new Observer[names.length];
	obsSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		observers[i]=(Observer)Configuration.getInstance(names[i]);
		obsSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded observers "+Arrays.asList(names));
	return names;
}

//---------------------------------------------------------------------

protected static String[] loadDynamics()
{
	// load dynamism managers
	String[] names = Configuration.getNames(PAR_DYN);
	dynamics = new Dynamics[names.length];
	dynSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		dynamics[i]=(Dynamics)Configuration.getInstance(names[i]);
		dynSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded dynamics "+Arrays.asList(names));
	return names;
}

//---------------------------------------------------------------------

protected static void loadProtocolSchedules()
{
	// load protocol schedulers
	String[] names = Configuration.getNames(Node.PAR_PROT);
	protSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		protSchedules[i] = new Scheduler(names[i]);
	}
}

//---------------------------------------------------------------------

/** 
 * Execute all the protocols. 
 */
protected static void nextRound(int cycle) {

	if( shuffle )
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
		int len = node.protocolSize();
		CommonState.setNode(node);
		CommonState.setCycleT(j);
		// XXX maybe should use different shuffle for each protocol?
		// (instead of running all on one node at the same time?)
		for(int k=0; k<len; ++k)
		{
			// Check if the protocol should be executed, given the
			// associated scheduler.
			if (!protSchedules[k].active(cycle))
				continue;
				
			CommonState.setPid(k);
			Protocol protocol = node.getProtocol(k);
			if( protocol instanceof CDProtocol )
			{
				((CDProtocol)protocol).nextCycle(node, k);
				if( !node.isUp() ) break;
			}
		}
	}
}

//---------------------------------------------------------------------

/**
 * "Old" style for protocol execution. It is possible to specify
 * the order using PAR_ORDER+"."+PAR_INIT, PAR_ORDER+"."+PAR_OBS, 
 * PAR_ORDER+"."+PAR_DYN, PAR_ORDER+"."+PAR_PROT. Pre-dynamics
 * and post-dynamics are considered.
 */
protected static void oldExperiment() 
{
	// Load observer, dynamics, protocol schedules
	loadObservers();
	loadDynamics();
	loadProtocolSchedules();
	
	// main cycle
	System.err.println("Simulator: starting simulation");
	for(int i=0; i<cycles; ++i)
	{
		CommonState.setT(i);
		CommonState.setPhase(CommonState.PRE_DYNAMICS);

		// analizer pre_dynamics
		boolean stop = false;
		for(int j=0; j<observers.length; ++j)
		{
			if( obsSchedules[j].active(i) &&
			    !obsSchedules[j].preCycle() )
				stop = stop || observers[j].analyze();
		}
		if( stop ) break;

		// dynamism
		for(int j=0; j<dynamics.length; ++j)
		{
			if( dynSchedules[j].active(i) ) dynamics[j].modify();
		}

		CommonState.setPhase(CommonState.PRE_CYCLE);

		// analizer pre_cycle
		for(int j=0; j<observers.length; ++j)
		{
			if( obsSchedules[j].active(i) &&
			    obsSchedules[j].preCycle() )
				stop = stop || observers[j].analyze();
		}
		if( stop ) break;

		// do one cycle
		nextRound(i);
		System.err.println("Simulator: cycle "+i+" done");
	}

	CommonState.setPhase(CommonState.POST_LAST_CYCLE);

	// analysis after the simulation
	for(int j=0; j<observers.length; ++j)
	{
		if( obsSchedules[j].fin() ) observers[j].analyze();
	}
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
protected static void newExperiment(String order) 
{
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

	System.err.println("Simulator: starting simulation");
	boolean stop = false;
	for(int i=0; i<cycles && !stop; ++i) {

		CommonState.setT(i);

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
		System.err.println("Simulator: cycle "+i+" done");
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
	}
	
	
}

//---------------------------------------------------------------------

protected static void nextExperiment() {
	
	// Reading parameter
	cycles = Configuration.getInt(PAR_CYCLES);
	shuffle = Configuration.contains(PAR_SHUFFLE);
	protocolshuffle = Configuration.contains(PAR_PSHUFFLE);
	getpair_rand = Configuration.contains(PAR_GETPAIR);

	// initialization
	System.err.println("Simulator: resetting");
	Network.reset();
	System.err.println("Simulator: running initializers");
	CommonState.setT(0); // needed here
	CommonState.setPhase(CommonState.PRE_DYNAMICS);
	runInitializers();

	// Old or new?
	String order = Configuration.getString(PAR_ORDER, null);
	if (order == null)
		oldExperiment();
	else
		newExperiment(order);
}

// =============== public methods ======================================
// =====================================================================

/**
*  Loads configuration and executes the simulation.
*/
public static void main(String[] pars) throws Exception {
	
	long time = System.currentTimeMillis();	

	// loading config
	// XXX we assume here that config is properties format
	System.err.println("Simulator: loading configuration");
	Configuration.setConfig( new ConfigProperties(pars) );

	int exps = Configuration.getInt(PAR_EXPS,1);

	try {

		for(int k=0; k<exps; ++k)
		{
			System.err.println("Simulator: starting experiment "+k);
			System.out.println("\n\n");
			nextExperiment();
		}
	
	} catch (MissingParameterException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	} catch (IllegalParameterException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	}


	// undocumented testing capabilities
	if(Configuration.contains("__t")) 
		System.out.println(System.currentTimeMillis()-time);
	if(Configuration.contains("__x")) Network.test();


}

}


