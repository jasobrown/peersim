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

package peersim.edsim;

import java.util.*;
import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;


/**
 * Event-driven simulator.
 */
public class EDSimulator
{

//---------------------------------------------------------------------
// Parameters
//---------------------------------------------------------------------
	
/**
 * The ending time for simulation. Only events that have a strictly smaller
 * value are executed..
 * @config
 */
private static final String PAR_ENDTIME = "simulation.endtime";	

/**
 * This parameter specifies
 * how often the simulator should log the current time on the
 * console. Standard error is used for such logs.
 * @config
 */
private static final String PAR_LOGTIME = "simulation.logtime";	

/** 
 * This parameter specifies how many
 * bits are used to order events that occurs at the same time. Defaults
 * to 8. A value smaller than 8 causes an IllegalParameterException.
 * Higher values allow for a better discrimination, but may reduce
 * the granularity of time values.
 * @config 
 */	
private static final String PAR_RBITS = "simulation.timebits";

/**
 * This is the prefix for initializers.
 * @config
 */
private static final String PAR_INIT = "init";

/**
 * This is the prefix for network dynamism managers.
 * @config
 */
private static final String PAR_CTRL = "control";


//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Maximum time for simulation */
protected static long endtime;

/** Log time */
protected static long logtime;

/** Number of bits used for random */
private static int rbits;

/** holds the modifiers of this simulation */
protected static Control[] controls=null;

/** Holds the control schedulers of this simulation */
protected static Scheduler[] ctrlSchedules = null;

/** Ordered list of events (heap) */
protected static Heap heap = new Heap();

protected static long nextlog = 0;

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
		((Control)inits[i]).execute();
	}
}

// --------------------------------------------------------------------

protected static void scheduleControls()
{
	// load controls
	String[] names = Configuration.getNames(PAR_CTRL);
	controls = new Control[names.length];
	ctrlSchedules = new Scheduler[names.length];
	for(int i=0; i<names.length; ++i)
	{
		controls[i]=(Control)Configuration.getInstance(names[i]);
		ctrlSchedules[i] = new Scheduler(names[i], false);
	}
	System.err.println("EDSimulator: loaded controls "+
		Arrays.asList(names));

	// Schedule controls execution
	int order = 0;
	for (int i=0; i < controls.length; i++) {
		ControlEvent event = new ControlEvent(
			controls[i], ctrlSchedules[i], order++);
		if (order > ((1 << rbits)-1))
			throw new IllegalArgumentException(
			"Too many control objects");
	}
}

//---------------------------------------------------------------------

/**
 * Adds a new event to be scheduled, specifying the number of time units
 * of delay, and the execution order parameter.
 * 
 * @param time 
 *   The actual time at which the next event should be scheduled.
 * @param order
 *   The index used to specify the order in which control events
 *   should be executed, if they happen to be at the same time, which is
 *   typically the case.
 * @param event 
 *   The object associated to this event
 */
static void addControlEvent(long time, int order, Object event)
{
	if (time >= endtime) return;
	time = (time << rbits) | order;
	heap.add(time, event, null, (byte) 0);
}

//---------------------------------------------------------------------

/**
 * This method is used to check whether the current configuration can
 * be used for cycle-driven simulations. 
 */
public static final boolean isConfigurationEventDriven()
{
	return Configuration.getInt(PAR_ENDTIME,-132)
			!= -132;
}

//---------------------------------------------------------------------

/**
 * Execute and remove the next event from the ordered event list.
 * @return true if the execution should be stopped.
 */
private static boolean executeNext() {

	Heap.Event ev = heap.removeFirst();
	if( ev == null )
	{
		System.err.println("EDSimulator: queue is empty, quitting"+
		" at time "+CommonState.getTime());
		return true;
	}
	
	long time = ev.time >> rbits;
	if (time >= nextlog)
	{
		System.err.println("Current time: " + time);
		do { nextlog+=logtime; }
		while(time >= nextlog);
	}
	if (time >= endtime)
	{
		System.err.println("EDSimulator: reached end time, quitting,"+
		" leaving "+heap.size()+" unprocessed events in the queue");
		return true;
	}
	
	CommonState.setTime(time);
	int pid = ev.pid;
	if (ev.node == null)
	{
		// control event; handled through a special method
		ControlEvent ctrl = (ControlEvent) ev.event;
		return ctrl.execute();
	}
	else if (ev.node != Network.prototype && ev.node.isUp() )
	{
		CommonState.setPid(pid);
		CommonState.setNode(ev.node);
		if( ev.event instanceof NextCycleEvent )
		{
			NextCycleEvent nce = (NextCycleEvent) ev.event;
			nce.execute();
		}
		else
		{try
		{
			EDProtocol prot = (EDProtocol) ev.node.getProtocol(pid);
			prot.processEvent(ev.node, pid, ev.event);
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException("Protocol " +
				Configuration.lookupPid(pid) + 
				" does not implement EDProtocol");
		}}
	}
	
	return false;
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
		throw new IllegalParameterException(PAR_RBITS, "This parameter"
		+" should be >= 8 or <= 64");
	}
	endtime = Configuration.getLong(PAR_ENDTIME);
	CommonState.setEndTime(endtime);
	logtime = Configuration.getLong(PAR_LOGTIME, Long.MAX_VALUE);

	// initialization
	System.err.println("EDSimulator: resetting");
	Network.reset();
	System.err.println("EDSimulator: running initializers");
	CommonState.setTime(0); // needed here
	runInitializers();
	scheduleControls();

	// Perform the actual simulation; executeNext() will tell when to
	// stop.
	boolean exit = false;
	while (!exit) {
		exit = executeNext();
	}

	// analysis after the simulation
	CommonState.setPhase(CommonState.POST_SIMULATION);
	for(int j=0; j<controls.length; ++j)
	{
		if( ctrlSchedules[j].fin() ) controls[j].execute();
	}
}

//---------------------------------------------------------------------

/**
 * Adds a new event to be scheduled, specifying the number of time units
 * of delay, and the node and the protocol identifier to which the event
 * will be delivered.
 * 
 * @param delay 
 *   The number of time units before the event is scheduled.
 *   Has to be non-negative.
 * @param event 
 *   The object associated to this event
 * @param node 
 *   The node associated to the event. A value of null corresponds to a 
 *   control event and will be handled appropriately.  
 * @param pid 
 *   The identifier of the protocol to which the event will be delivered
 */
public static void add(long delay, Object event, Node node, int pid)
{
	if (delay < 0 )
		throw new IllegalArgumentException("Protocol "+
			node.getProtocol(pid)+" is trying to add event "+
			event+" with a negative delay: "+delay);
	if (pid > Byte.MAX_VALUE) 
		throw new IllegalArgumentException(
				"This version does not support more than " 
				+ Byte.MAX_VALUE + " protocols");
	
	long time = CommonState.getTime()+delay;
	if (time >= endtime) return;
	time = (time << rbits) | CommonState.r.nextInt(1 << rbits);
	heap.add(time, event, node, (byte) pid);
}

//---------------------------------------------------------------------

}
