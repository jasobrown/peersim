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
import peersim.config.*;
import peersim.core.*;


/**
* Event-driven simulator engine.
* It is a fully static singleton class.
* For an event driven simulation 
* the configuration has to describe a set of {@link Protocol}s,
* a set of {@link Control}s and their ordering and a set of
* initializers and their ordering. See parameters {@value #PAR_INIT},
* {@value #PAR_CTRL}.
* <p>
* One experiment run by {@link #nextExperiment} works as follows.
* First the initializers are run in the specified order. Then the first
* execution of all specified controls is scheduled in the event queue.
* This scheduling is defined by the {@link Scheduler} parameters of each
* control component.
* After this, the first event is taken from the event queue. If the event
* wraps a control, the control is executed, otherwise the event is
* delivered to the destination protocol, that must implement
* {@link EDProtocol}. This
* is iterated while the current time is less than {@value #PAR_ENDTIME} or
* the queue becomes empty.
* If more control events fall at the same time point, then the order given
* in the configuration is respected. If more non-control events fall at the same
* time point, they are processed in a random order.
* <p>
* The engine also provides the interface to add events to the queue.
* Note that this engine does not explicitly run the protocols.
* In all cases at least one control or initializer has to be defined that
* sends event(s) to protocols.
* <p>
* Controls can be scheduled (using the {@link Scheduler}
* parameters in the configuration) to run after the experiment
* has finished.
* That is, each experiment is finished by running the controls that are
* scheduled to be run after the experiment.
* <p>
* Any control can interrupt an experiment at any time it is
* executed by returning true in method {@link Control#execute}.
* However, the controls scheduled to run after the experiment are still
* executed completely, irrespective of their return value and even if
* the experiment was interrupted.
* <p>
* {@link CDScheduler} has to be mentioned that is a control that
* can bridge the gap between {@link peersim.cdsim} and the event driven
* engine. It can wrap {@link peersim.cdsim.CDProtocol} appropriately so that the
* execution of the cycles are scheduled in configurable ways for each node
* individually. In some cases this can add a more fine-grained control
* and more realism to {@link peersim.cdsim.CDProtocol} simulations,
* at the cost of some
* loss in performance.
* <p>
* When protocols at different nodes send messages to each other, they might
* want to use a model of the transport layer so that in the simulation
* message delay and message omissions can be modeled in a modular way.
* This functionality is implemented in package {@link peersim.transport}.
* @see Configuration
 */
public class EDSimulator
{

//---------------------------------------------------------------------
// Parameters
//---------------------------------------------------------------------
	
/**
 * The ending time for simulation. Only events that have a strictly smaller
 * value are executed.
 * @config
 */
private static final String PAR_ENDTIME = "simulation.endtime";	

/**
 * This parameter specifies
 * how often the simulator should log the current time on the
 * standard error.
 * @config
 */
private static final String PAR_LOGTIME = "simulation.logtime";	

/** 
 * This parameter specifies how many
 * bits are used to order events that occur at the same time. Defaults
 * to 8. A value smaller than 8 causes an IllegalParameterException.
 * Higher values allow for a better discrimination, but reduce
 * the maximal time steps that can be simulated.
 * @config 
 */	
private static final String PAR_RBITS = "simulation.timebits";

/**
 * This is the prefix for initializers.
 * These have to be of type
 * {@link Control}. They are run at the beginning of each experiment, in the
 * order specified by the configuration.
 * @see Configuration
 * @config
 * @config
 */
private static final String PAR_INIT = "init";

/**
 * This is the prefix for {@link Control} components.
 * They are run at the time points defined by the
 * {@link Scheduler} associated to them. If some controls have to be
 * executed at the same time point, they are executed in the order
 * specified in the configuration.
 * @see Configuration
 * @config
 */
private static final String PAR_CTRL = "control";


//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Maximum time for simulation */
private static long endtime;

/** Log time */
private static long logtime;

/** Number of bits used for random */
private static int rbits;

/** holds the modifiers of this simulation */
private static Control[] controls=null;

/** Holds the control schedulers of this simulation */
private static Scheduler[] ctrlSchedules = null;

/** Ordered list of events (heap) */
private static Heap heap = new Heap();

private static long nextlog = 0;

// =============== initialization ======================================
// =====================================================================

/** to prevent construction */
private EDSimulator() {}

//---------------------------------------------------------------------
//Private methods
//---------------------------------------------------------------------

/**
 * Load and run initializers.
 */
private static void runInitializers() {
	
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

private static void scheduleControls()
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
 *   The control event
 */
static void addControlEvent(long time, int order, ControlEvent event)
{
	if (time >= endtime) return;
	time = (time << rbits) | order;
	heap.add(time, event, null, (byte)0);
}

//---------------------------------------------------------------------

/**
 * This method is used to check whether the current configuration can
 * be used for event driven simulations. It checks for the existence of
 * config parameter {@value #PAR_ENDTIME}.
 */
public static final boolean isConfigurationEventDriven()
{
	return Configuration.contains(PAR_ENDTIME);
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
 * Runs an experiment, resetting everything except the random seed.
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
	if( CommonState.getEndTime() < 0 ) // not initialized yet
		CommonState.setEndTime(endtime);
	logtime = Configuration.getLong(PAR_LOGTIME, Long.MAX_VALUE);

	// initialization
	System.err.println("EDSimulator: resetting");
	controls = null;
	ctrlSchedules = null;
	heap = new Heap();
	nextlog = 0;
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
		if( ctrlSchedules[j].fin ) controls[j].execute();
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
 *   The node associated to the event.
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

}
