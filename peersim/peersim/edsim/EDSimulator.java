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
import peersim.util.*;


/**
 * Event-driven simulator. The simulator is able to run both event-driven
 * protocols {@link EDProtocol} and cycle-driven protocols {@link CDProtocol}.
 * To execute any of the cycle-based classes (observers, dynamics, and
 * protocols), the <code>step</code> parameter of the {@link Scheduler} 
 * associated to the class must be specified, to define the length of 
 * cycles. 
 * 
 * 
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
 * The string name of the configuration parameter that specifies
 * the ending time for simulation. No event after this value
 * will be executed.
 */
public static final String PAR_ENDTIME = "simulation.endtime";	

/**
 * If this parameter is present, the order of visiting each node for
 * cycle-based protocols is shuffled
 * at each cycle. The default is no shuffle.
 */
public static final String PAR_SHUFFLE = "simulation.shuffle";

/** 
 * String name of the configuration parameter that specifies how many
 * bits are used to order events that occurs at the same time. Defaults
 * to 8. A value smaller than 8 causes an IllegalParameterException.
 * Higher values allow for a better discrimination, but may reduce
 * the granularity of time values. 
 */	
public static final String PAR_RBITS = "simulation.timebits";

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
protected static long endtime;

/** If true, when executing a cycle-based protocols nodes are shuffled */
private static boolean shuffle;

/** Number of bits used for random */
private static int rbits;

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

/** Ordered list of events (heap) */
protected static Heap heap = new Heap();


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
	// size is 0).
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

/**
 * Adds a new event to be scheduled, specifying the number of time units
 * of delay, plust
 * 
 * @param time 
 *   The actual time at which the next event should be scheduled.
 * @param order
 *   The index used to specify the order in which cycle-based events
 *   should be executed, if they happen to be at the same time.
 * @param event 
 *   The object associated to this event
 */
static void addCycleEvent(long time, int order, Object event)
{
	time = (time << rbits) | order;
	heap.add(time, event, null, (byte) 0);
}

//---------------------------------------------------------------------

/**
 * Execute and remove the next event from the ordered event list.
 * @return true if the execution should be stopped.
 */
private static boolean executeNext()
{
	Heap.Event ev = heap.removeFirst();
	long time = ev.time >> rbits;
	if (time > endtime)
		return true;
	CommonState.setTime(time);
	int pid = ev.pid;
	if (ev.node == null) {
		// Cycle-based event; handled through a special method
		CycleEvent cycle = (CycleEvent) ev.event;
		return cycle.execute(shuffle);
	} else {
		// Check if the node is up; if not, skip this event.
		if (!ev.node.isUp()) 
			return false;
		CommonState.setPid(pid);
		try {
			EDProtocol prot = (EDProtocol) ev.node.getProtocol(pid);
			prot.processEvent(ev.node, pid, ev.event);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Protocol " + pid + 
					" does not implement EDProtocol");
		}
		return false;
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
	rbits = Configuration.getInt(PAR_RBITS, 8);	
	if (rbits < 8 && rbits >= 64) {
		throw new IllegalParameterException(PAR_RBITS, "This parameter" +
				"should be equal or large then 8 or smaller than 64");
	}
	endtime = Configuration.getLong(PAR_ENDTIME);
	shuffle = Configuration.contains(PAR_SHUFFLE);

	// initialization
	System.err.println("EDSimulator: resetting");
	Network.reset();
	System.err.println("EDSimulator: running initializers");
	CommonState.setTime(0); // needed here
	CommonState.setPhase(CommonState.PRE_DYNAMICS);
	runInitializers();
			
	// Load observer, dynamics, protocol schedules
	loadObservers();
	loadDynamics();
	loadCDProtocols();

	int[] times;
	int order = 0;
	
	// Schedule observers execution
	for (int i=0; i < observers.length; i++) {
		if (!obsSchedules[i].preCycle()) {
			CycleEvent event = new CycleEvent(observers[i], obsSchedules[i], order++, CommonState.PRE_DYNAMICS);
			if (order > ((1 << rbits)-1))
				throw new 
				IllegalArgumentException("Too many cycle-based entities");
		}
	}

	// Schedule dynamics execution
	for (int i=0; i < dynamics.length; i++) {
		CycleEvent event = 
			new CycleEvent(dynamics[i], dynSchedules[i], order++);
		if (order > ((1 << rbits)-1))
			throw new 
			IllegalArgumentException("Too many cycle-based entities");
	}

	for (int i=0; i < observers.length; i++) {
		if (obsSchedules[i].preCycle()) {
			CycleEvent event = 
				new CycleEvent(observers[i], obsSchedules[i], order++, 
						CommonState.PRE_CYCLE);
			if (order > ((1 << rbits)-1))
				throw new 
				IllegalArgumentException("Too many cycle-based entities");
		}
	}

	// Schedule protocol execution
	for (int i=0; i < cdprotocols.length; i++) {
		CycleEvent event = new CycleEvent(i, protSchedules[i], order++);
		if (order > ((1 << rbits)-1))
			throw new 
			IllegalArgumentException("Too many cycle-based entities");
	}
	
	// Perform the actual simulation; executeNext() will tell when to
	// stop.
	boolean exit = false;
	while (!exit) {
		exit = executeNext();
	}

	// analysis after the simulation
	CommonState.setPhase(CommonState.POST_LAST_CYCLE);
	for(int j=0; j<observers.length; ++j)
	{
		if( obsSchedules[j].fin() ) observers[j].analyze();
	}
}

//---------------------------------------------------------------------

/**
 * Adds a new event to be scheduled, specifying the number of time units
 * of delay, and the node and the protocol identifier to which the event
 * will be delivered.
 * 
 * @param delay 
 *   The number of time units before the event is scheduled
 * @param event 
 *   The object associated to this event
 * @param node 
 *   The node associated to the event. A value of null corresponds to a 
 *   cycle-based event and will handled opportunely    
 * @param pid 
 *   The identifier of the protocol to which the event will be delivered
 */
public static void add(long delay, Object event, Node node, int pid)
{
	if (pid > Byte.MAX_VALUE) 
		throw new IllegalArgumentException(
				"This version does not support more than " 
				+ Byte.MAX_VALUE + " protocols");
	long time = ((CommonState.getTime()+delay) << rbits) | 
		CommonRandom.r.nextInt(1 << rbits);
	heap.add(time, event, node, (byte) pid);
}

//---------------------------------------------------------------------

}
