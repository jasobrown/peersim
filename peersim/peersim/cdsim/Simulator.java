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

import peersim.config.*;
import peersim.core.*;
import peersim.reports.Observer;
import peersim.dynamics.Dynamics;
import peersim.util.CommonRandom;
import java.util.Arrays;

/**
* This is the executable class for performing a cycle driven simulation.
* The class is completely static as at the same time we expect to have only
* one simulation running in a virtual machine.
* The simulation is highly configurable.
*/
public class Simulator {


// ============== fields ===============================================
// =====================================================================

/** Gives the number of cycles to complete */
public static final String PAR_CYCLES = "simulation.cycles";

/**
* if set, it means the order of visiting each node is shuffled in each cycle.
* The default is no shuffle
*/
public static final String PAR_SHUFFLE = "simulation.shuffle";

/**
* The number of times the experiment is run. Defaults to 1.
*/
public static final String PAR_EXPS = "simulation.experiments";

/**
* The type of the getPair function. Defaults to "seq".
*/
public static final String PAR_GETPAIR = "simulation.getpair";

public static final String PAR_INIT = "init";

/**
* This is the prefix for network dynamism managers. These have to be of
* type {@link Dynamics}.
*/
public static final String PAR_DYN = "dynamics";

public static final String PAR_OBS = "observer";

// --------------------------------------------------------------------

/** the number of independent restarted simulations to be performed */
private static int cycles;

private static boolean shuffle;

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

// XXX it would be possible to schedule protocols too the same way


// =============== protected methods ===================================
// =====================================================================


protected static void runInitializers() {
	
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);

	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " + i + ": " + inits[i].getClass());
		((Dynamics)inits[i]).modify();
	}
}

// --------------------------------------------------------------------

protected static void nextRound() {

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
		// XXX maybe should use different shuffle for each protocol?
		// (instead of running all on one node at the same time?)
		for(int k=0; k<len; ++k)
		{
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
	cycles = Configuration.getInt(PAR_CYCLES);
	shuffle = Configuration.contains(PAR_SHUFFLE);
	exps = Configuration.getInt(PAR_EXPS,1);
	// XXX this is a hack temporarily
	getpair_rand = Configuration.contains(PAR_GETPAIR);

	try {

	for(int k=0; k<exps; ++k)
	{
		System.err.println("Simulator: starting experiment "+k);
System.out.println("\n\n");

		// initialization
		System.err.println("Simulator: resetting overlay network");
		Network.reset();
		System.err.println("Simulator: running initializers");
		CommonState.setT(0); // needed here
		CommonState.setPhase(CommonState.PRE_DYNAMICS);
		runInitializers();

		// load analizers
		String[] names = Configuration.getNames(PAR_OBS);
		observers = new Observer[names.length];
		obsSchedules = new Scheduler[names.length];
		for(int i=0; i<names.length; ++i)
		{
			observers[i]=
				(Observer)Configuration.getInstance(names[i]);
			obsSchedules[i] = new Scheduler(names[i]);
		}
		System.err.println("Simulator: loaded observers "+
			Arrays.asList(names));

		// load dynamism managers
		names = Configuration.getNames(PAR_DYN);
		dynamics = new Dynamics[names.length];
		dynSchedules = new Scheduler[names.length];
		for(int i=0; i<names.length; ++i)
		{
			dynamics[i]=
				(Dynamics)Configuration.getInstance(names[i]);
			dynSchedules[i] = new Scheduler(names[i]);
		}
		System.err.println("Simulator: loaded modifiers "+
			Arrays.asList(names));

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
				if( dynSchedules[j].active(i) )
					dynamics[j].modify();
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
			nextRound();
			System.err.println("Simulator: cycle "+i+" done");
		}

		CommonState.setPhase(CommonState.POST_LAST_CYCLE);

		// analysis after the simulation
		for(int j=0; j<observers.length; ++j)
		{
			if( obsSchedules[j].fin() ) observers[j].analyze();
		}
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


