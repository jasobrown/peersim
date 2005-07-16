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


/**
* this class is used to wrap a CDProtocol instance into an event so
* that it can be used in the event based simulation engine.
* This class is responsible for calling the nextCycle method of the CDProtocol
* and to schedule the next cycle. This class can be configured in the
* configuration through parameter {@value #PAR_NEXTCYCLE}. The default
* is this base class, that implements fixed length cycles, that is,
* cycles, that is, the nextCycle method is scheduled to run in reguler
* fixed length intervals.
*
* <p>
* Note that reimplementing this class allows for arbitrary scheduling,
* including adaptively changing or irregular cycle lengths, etc.
*/
public class NextCycleEvent {

// ============================== fields ==============================
// ====================================================================

/**
* Parameter that is used to define the class that is used to schedule
* the next cycle. Its type is (or extends) {@link NextCycleEvent}.
* Defaults to {@link NextCycleEvent}.
*/
public static final String PAR_NEXTCYCLE = "nextcycle";


// =============================== initialization ======================
// =====================================================================


/**
 * Creates a next cycle event for a protocol. It also schedules the protocol
 * for the first execution adding it to the priority queue of the event driven
 * simulation. The time of the first execution is detemined by
 * {@link #firstDelay}.
 */
NextCycleEvent(Node node, int pid, Scheduler sch) {

	EDSimulator.add(firstDelay(sch), this, node, pid);
}


// ========================== methods ==================================
// =====================================================================


/**
* Executes the nextCycle method of the protocol, and schedules the next call
* using the delay returned by {@link #nextDelay}.
*/
public final void execute(Scheduler sch) {

	int pid = CommonState.getPid();
	Node node = CommonState.getNode();
	CDProtocol cdp = (CDProtocol)node.getProtocol(pid);
	cdp.nextCycle(node,pid);
	EDSimulator.add(nextDelay(sch), this, node, pid);
}

// --------------------------------------------------------------------

/**
* Schedules the object for the next execution
* adding it to the priority queue of the event driven simulation.
*/
protected long nextDelay(Scheduler sch) {
	
	return sch.step;
}

// --------------------------------------------------------------------

/**
* Schedules the object for the next execution
* adding it to the priority queue of the event driven simulation.
* This implementation returns a randomly selected point from the cycle
* length, which is given by {@link Scheduler#step}.
*/
protected long firstDelay(Scheduler sch) {
	
	return CommonState.r.nextLong(sch.step);
}

}


