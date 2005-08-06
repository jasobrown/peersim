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
* and to schedule the next cycle.
*
* <p>
* Note that reimplementing method {@link #nextDelay} of this class allows
* for arbitrary scheduling,
* including adaptively changing or irregular cycle lengths, etc.
*/
public class NextCycleEvent implements Cloneable {


protected final Scheduler sch;


// =============================== initialization ======================
// =====================================================================


/**
* Reads configuration to initialize the object. Extending classes should
* have a constructor with the same signature, often as simple as
* <pre>super(n,obj)</pre>.
* This constructor is called by internal classes only.
*/
protected NextCycleEvent(String n, Object obj) {

	sch = (Scheduler)obj;
}

// --------------------------------------------------------------------

/**
* Returns a clone of the object. Overriding this method is necessary and
* typically is as simple as <pre>return super.clone()</pre>. In general,
* always use <pre>super.clone()</pre> to obtain the object to be returned
* on which you can perform optional deep cloning operations (arrays, etc).
*/
protected Object clone() throws CloneNotSupportedException {
	
	return super.clone();
}


// ========================== methods ==================================
// =====================================================================


/**
* Executes the nextCycle method of the protocol, and schedules the next call
* using the delay returned by {@link #nextDelay}.
* If the next execution time as defined by the delay is outside of the
* valid times as defined by {@link #sch}, then the next event is not scheduled.
* Note that this means that this protocol will no longer be scheduled because
* the next event after the next event is scheduled by the next event.
*/
public final void execute() {

	int pid = CommonState.getPid();
	Node node = CommonState.getNode();
	CDProtocol cdp = (CDProtocol)node.getProtocol(pid);
	cdp.nextCycle(node,pid);
	
	long delay = nextDelay();
	if( CommonState.getTime()+delay < sch.until )
		EDSimulator.add(delay, this, node, pid);

}

// --------------------------------------------------------------------

/**
* Calculates the delay until the next execution of the protocol.
* This default impementation returns a constant delay equal to the step
* parameter of the schedule of this event (as set in the config file).
*/
protected long nextDelay() {
	
	return sch.step;
}

}


