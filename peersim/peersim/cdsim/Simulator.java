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
*/
public class Simulator {


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
* The type of the getPair function. Defaults to "seq".
*/
public static final String PAR_GETPAIR = "simulation.getpair";

/**
 * This is the prefix for initializers. These have to be of type
 * {@link Dynamics}.
 */
public static final String PAR_INIT = "init";

/**
 * This is the prefix for controls.
 */
public static final String PAR_CTRL = "control";


// --------------------------------------------------------------------

/** The maximum number of cycles to be performed */
protected static int cycles;

protected static boolean shuffle;

protected static boolean getpair_rand;

/** holds the modifiers of this simulation */
protected static Control[] controls=null;

/** Holds the control schedulers of this simulation */
protected static Scheduler[] ctrlSchedules = null;

/** Holds the protocol schedulers of this simulation */
protected static Scheduler[] protSchedules = null;


// =============== protected methods ===================================
// =====================================================================

/**
 * Load and run initializers.
 */
protected static void runInitializers() {
	
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);
	String names[] = Configuration.getNames(PAR_INIT);
	
	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " +names[i]+ ": " + inits[i].getClass());
		((Control)inits[i]).execute();
	}
}

// --------------------------------------------------------------------

protected static String[] loadControls() {

	// load controls
	String[] names = Configuration.getNames(PAR_CTRL);
	controls = new Control[names.length];
	ctrlSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		controls[i]=(Control)Configuration.getInstance(names[i]);
		ctrlSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded controls "+Arrays.asList(names));
	return names;
}

//---------------------------------------------------------------------

protected static void loadProtocolSchedules() {

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
			   CDState.r.nextInt(Network.size()));
		else
			node = Network.get(j);
		if( !node.isUp() ) continue; 
		int len = node.protocolSize();
		CDState.setNode(node);
		CDState.setCycleT(j);
		// XXX maybe should use different shuffle for each protocol?
		// (instead of running all on one node at the same time?)
		for(int k=0; k<len; ++k)
		{
			// Check if the protocol should be executed, given the
			// associated scheduler.
			if (!protSchedules[k].active(cycle))
				continue;
				
			CDState.setPid(k);
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
 * Runs an experiment
 */
public static void nextExperiment()  {

	// Reading parameter
	cycles = Configuration.getInt(PAR_CYCLES);
	shuffle = Configuration.contains(PAR_SHUFFLE);
	getpair_rand = Configuration.contains(PAR_GETPAIR);

	// initialization
	System.err.println("Simulator: resetting");
	Network.reset();
	System.err.println("Simulator: running initializers");
	// Initializers are run at 
	// cycle 0 (cycle-driven) / time 0 (event-driven)
	CDState.setCycle(0);
	runInitializers();
			
	loadControls();
	loadProtocolSchedules();
	
	// main cycle
	System.err.println("Simulator: starting simulation");
	for(int i=0; i<cycles; ++i)
	{
		CDState.setCycle(i);

		// analizer pre_dynamics
		boolean stop = false;
		for(int j=0; j<controls.length; ++j)
		{
			if( ctrlSchedules[j].active(i) )
				stop = stop || controls[j].execute();
		}
		if( stop ) break;

		// do one cycle
		nextRound(i);
		System.err.println("Simulator: cycle "+i+" done");
	}

	CDState.setPhase(CDState.POST_SIMULATION);

	// analysis after the simulation
	for(int j=0; j<controls.length; ++j)
	{
		if( ctrlSchedules[j].fin() ) controls[j].execute();
	}
}

}


