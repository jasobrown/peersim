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
* The protocols that this scheduler schedules for the first execution.
* It might contain several protocol names, separated by whitespace. All
* protocols will be scheduled based on the common parameters set for this
* scheduler and the parameters of the protocol (cycle length).
* Protocols are scheduled independently of each other.
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

private final NextCycleEvent[] nce;

private final Scheduler[] sch;

private final int[] pid;

private final boolean randstart;

// =============================== initialization ======================
// =====================================================================


/**
*/
public CDScheduler(String n) {

	String[] prots=Configuration.getString(n+"."+PAR_PROTOCOL).split("\\s");
	pid = new int[prots.length];
	sch = new Scheduler[prots.length];
	nce = new NextCycleEvent[prots.length];
	for(int i=0; i<prots.length; ++i)
	{
		pid[i] = Configuration.lookupPid(prots[i]);
		if( !(Network.prototype.getProtocol(pid[i]) instanceof
			CDProtocol))
		{
			throw new IllegalParameterException(n+"."+PAR_PROTOCOL,
				"Only CDProtocols are accepted here");
		}
	
		// with no default values to avoid "overscheduling"
		// due to lack of step option.
		String protname = Node.PAR_PROT+"."+prots[i];
		sch[i] = new Scheduler(protname, false);
	
		if( Configuration.contains(n+"."+PAR_NEXTC) )
		{
			nce[i] = (NextCycleEvent)
			  Configuration.getInstance(n+"."+PAR_NEXTC,sch[i]);
		}
		else
		{
			nce[i] = new NextCycleEvent((String)null,sch[i]);
		}
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
	
	final long time = CommonState.getTime();
	for(int i=0; i<pid.length; ++i)
	{
		Object nceclone=null;
		try { nceclone = nce[i].clone(); }
		catch(CloneNotSupportedException e) {} //cannot possibly happen
		
		final long delay = firstDelay(sch[i].step);
		final long nexttime = time+delay;
		if( nexttime < sch[i].until && nexttime >= sch[i].from )
			EDSimulator.add(delay, nceclone, n, pid[i]);
	}
}

// --------------------------------------------------------------------

/**
* Returns the time (through giving the delay from the current time)
* when this even is first executed.
* If {@value #PAR_RNDSTART} is not set, it returns zero, otherwise
* a random value between 0, inclusive, and the configured cycle length
* (the {@value Scheduler#PAR_STEP} parameter of the protocol), exclusive.
* @param cyclelength The length of a cycle of the cycle based protocol
* for which this method is called
*/
protected long firstDelay(long cyclelength) {
	
	if(randstart)
		return CommonState.r.nextLong(cyclelength);
	else
		return 0;
}
}


