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

import peersim.core.*;
import peersim.cdsim.CDProtocol;
import peersim.config.*;
import peersim.dynamics.NodeInitializer;

/**
* Schedules the first execution of the cycle based protocol instances in
* the event driven angine.
* Unlike {@link Scheduler}, this component is a standalone {@link Control},
* so for all {@link CDProtocol}s one instance of this scheduler needs to be
* specified in an event driven simulation. It will most often be used as an
* initializer, since the scheduled events schedule themselves for the
* consequtive executions (see {@link NextCycleEvent}).
*
* <p>The {@link CDProtocol} specification in the configuration needs to
* contain a {@link Scheduler} specification for the step size (config
* parameter {@value Scheduler#PAR_STEP}). This value is used as a cycle length.
*@see NextCycleEvent
*/
public class CDScheduler implements Control, NodeInitializer {

// ============================== fields ==============================
// ====================================================================

/**
* Parameter that is used to define the class that is used to schedule
* the next cycle. Its type is (or extends) {@link NextCycleEvent}.
* Defaults to {@link NextCycleEvent}.
* @config
*/
private static final String PAR_NEXTC = "nextcycle";

/**
* The protocol that this scheduler schedules for the first execution.
* @config
*/
private static final String PAR_PROTOCOL = "protocol";

/**
* If set, it means that the initial execution of the give protocol is
* scheduled for a different random time for all nodes. The random time
* is a sample between the current time (inclusive) and the cycle length
* (exclusive), the latter being specified by parameter
* {@value Scheduler#PAR_STEP} of the assigned protocol.
* @see #execute
* @config
*/
private static final String PAR_RNDSTART = "randstart";

private final NextCycleEvent nce;

private final Scheduler sch;

private final int pid;

private final boolean randstart;

private final String name;


// =============================== initialization ======================
// =====================================================================


/**
*/
public CDScheduler(String n) {

	name = n;
	String protname = Node.PAR_PROT+"."+
		Configuration.getString(n+"."+PAR_PROTOCOL);
	pid = Configuration.getPid(n+"."+PAR_PROTOCOL);
	if( !(Network.prototype.getProtocol(pid) instanceof CDProtocol))
	{
		throw new IllegalParameterException(n+"."+PAR_PROTOCOL,
			"A CDProtocol is needed here");
	}
	
	// with no default values to avoid "overscheduling"
	// due to lack of step option.
	sch = new Scheduler(protname, false);
	if( Configuration.contains(n+"."+PAR_NEXTC) )
	{
		nce = (NextCycleEvent)
			Configuration.getInstance(n+"."+PAR_NEXTC,sch);
	}
	else
	{
		nce = new NextCycleEvent((String)null,sch);
	}

	randstart = Configuration.contains(n+"."+PAR_RNDSTART);
}



// ========================== methods ==================================
// =====================================================================

/**
 * Schedules the protocol at all nodes
 * for the first execution adding it to the priority queue of the event driven
 * simulation. The time of the first execution is detemined by
 * {@link #firstDelay}. The implementation calls {@link #initialize}
 * for all nodes.
 * @see #initialize
*/
public boolean execute() {
	
	for(int i=0; i<Network.size(); ++i)
	{
		initialize(Network.get(i));
	}
	
	return false;
}

// --------------------------------------------------------------------

/**
 * Schedules the protocol at given node
 * for the first execution adding it to the priority queue of the event driven
 * simulation. The time of the first execution is detemined by
 * {@link #firstDelay}. If {@link #firstDelay} returns a value that defines
 * a next execution time which is not valid according to the schedule of the
 * protocol then the protocol is not scheduled for execution from now on.
*/
public void initialize(Node n) {
	
	Object nceclone=null;
	try { nceclone = nce.clone(); }
	catch(CloneNotSupportedException e) {} //cannot possibly happen
	
	final long time = CommonState.getTime();
	final long delay = firstDelay();
	final long nexttime = time+delay;
	if( nexttime < sch.until && nexttime >= sch.from )
		EDSimulator.add(delay, nceclone, n, pid);
}

// --------------------------------------------------------------------

/**
* Returns the time (through giving the delay from the current time)
* when this even is first executed.
* If {@value #PAR_RNDSTART} is not set, it returns zero, otherwise
* a random value between 0, inclusive, and the configured cycle length
* (the {@value Scheduler#PAR_STEP} parameter of the protocol), exclusive.
*/
protected long firstDelay() {
	
	if(randstart)
		return CommonState.r.nextLong(sch.step);
	else
		return 0;
}
}


