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

package peersim.edsim;

import java.util.*;
import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import peersim.reports.Observer;


/**
 * Event-driven simulator. The simulator is able to run both event-driven
 * protocols {@link EDProtocol} and cycle-driven protocols {@link CDProtocol}.
 * To execute any of the cycle-based classes (observers, dynamics, and
 * protocols), the step parameter of Schedulers must be specified, to
 * define the periodicity of cycles. 
 * XXX To be completed.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class EDSimulator
{

//---------------------------------------------------------------------
// Parameters
//---------------------------------------------------------------------
	
/**
 * 
 */
public static final String PAR_ENDTIME = "simulation.endtime";	

/**
 * If this parameter is present, the order of visiting each node is shuffled
 * at each cycle. The default is no shuffle.
 */
public static final String PAR_SHUFFLE = "simulation.shuffle";

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


//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Maximum time for simulation */
protected static int endtime;

/** holds the observers of this simulation */
protected static Observer[] observers=null;

/** holds the modifiers of this simulation */
protected static Dynamics[] dynamics=null;

/** Holds the observer schedulers of this simulation */
protected static Scheduler[] obsSchedules = null;

/** Holds the dynamics schedulers of this simulation */
protected static Scheduler[] dynSchedules = null;

/** Holds the pids of the CDProtocols to be executed in this simulation */
protected static int[] cdprotocols = null;

/** Holds the protocol schedulers of this simulation */
protected static Scheduler[] protSchedules = null;


//---------------------------------------------------------------------
//Private methods
//---------------------------------------------------------------------

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
		obsSchedules[i] = new Scheduler(names[i], false);
	}
	System.err.println("EDSimulator: loaded observers "+
		Arrays.asList(names));
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
		dynSchedules[i] = new Scheduler(names[i], false);
	}
	System.err.println("EDSimulator: loaded dynamics "+
		Arrays.asList(names));
	return names;
}

//---------------------------------------------------------------------

protected static void loadCDProtocols()
{
	// CDProtocol instances are searched in Network.prototype, which
	// contains the prototype node for the network (even if the network
	// size is 0.
	String[] names = Configuration.getNames(Node.PAR_PROT);
	Node node = Network.prototype;
	int size = node.protocolSize();
	int[] pids = new int[size];
	int count = 0;
	for (int i=0; i < size; i++) {
		if (node.getProtocol(i) instanceof CDProtocol) {
			pids[count++] = i;
		}
	}
	
	// Copy the array in one with correct length
	cdprotocols = new int[count];
	System.arraycopy(pids, 0, cdprotocols, 0, count);
		
	// load protocol schedulers (only for CDProtocols, with no default
	// values to avoid "overscheduling" due to lack of step option.
	protSchedules = new Scheduler[count];
	for(int i=0; i<count; ++i)
	{
		protSchedules[i] = new Scheduler(names[pids[i]], false);
	}
}

//---------------------------------------------------------------------
//Public methods
//---------------------------------------------------------------------

/**
 * Runs an experiment
 */
public static void nextExperiment() 
{
	// Reading parameter
	endtime = Configuration.getInt(PAR_ENDTIME);
	EventHandler.setShuffle(Configuration.contains(PAR_SHUFFLE));

	// initialization
	System.err.println("EDSimulator: resetting");
	Network.reset();
	System.err.println("EDSimulator: running initializers");
	CommonState.setT(0); // needed here
	CommonState.setPhase(CommonState.PRE_DYNAMICS);
	runInitializers();
			
	// Load observer, dynamics, protocol schedules
	loadObservers();
	loadDynamics();
	loadCDProtocols();

	int[] times;
	// Schedule observers execution
	for (int i=0; i < observers.length; i++) {
		times = obsSchedules[i].getSchedule(endtime);
		for (int j=0; j < times.length; j++) {
			EventHandler.add(times[j], observers[i], null, EventHandler.OBSERVER);
		}
		System.out.println("");
	}

	// Schedule dynamics execution
	for (int i=0; i < dynamics.length; i++) {
		times = dynSchedules[i].getSchedule(endtime);
		for (int j=0; j < times.length; j++) {
			System.out.print(times[j] + ", ");
			EventHandler.add(times[j], dynamics[i], null, EventHandler.DYNAMICS);
		}
		System.out.println("");
	}

	// Schedule protocol execution
	for (int i=0; i < cdprotocols.length; i++) {
		times = protSchedules[i].getSchedule(endtime);
		for (int j=0; j < times.length; j++) {
			EventHandler.add(times[j], null, null, cdprotocols[i]);
		}
		System.out.println("");
	}
	
	// Perform the actual simulation
	boolean exit = false;
	do {
		exit = EventHandler.executeNext(endtime);
	} while (!exit);

	// analysis after the simulation
	for(int j=0; j<observers.length; ++j)
	{
		if( obsSchedules[j].fin() ) observers[j].analyze();
	}
}

}
