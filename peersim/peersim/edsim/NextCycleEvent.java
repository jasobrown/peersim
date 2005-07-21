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
public class NextCycleEvent implements Cloneable {

// ============================== fields ==============================
// ====================================================================

/**
* Parameter that is used to define the class that is used to schedule
* the next cycle. Its type is (or extends) {@link NextCycleEvent}.
* Defaults to {@link NextCycleEvent}.
*/
public static final String PAR_NEXTC = "nextcycle";


// =============================== initialization ======================
// =====================================================================


/**
* Reads configuration to initialize the object. Extending classes should
* have a constructor with the same signature, often as simple as
* <pre>super(n)</pre>.
* This specific implementation does nothing.
*/
public NextCycleEvent(String n) {}

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
 * Schedules the protocol
 * for the first execution adding it to the priority queue of the event driven
 * simulation. The time of the first execution is detemined by
 * {@link #firstTime}. This mehtod creates a clone of the object and puts
 * that clone into the queue. Also, it calls the {@link #firstTime} of the
 * clone and not this object.
 */
public final void scheduleFirstEvent(Node node, int pid, Scheduler sch) {

	NextCycleEvent nce=null;
	try { nce = (NextCycleEvent)this.clone(); }
	catch(CloneNotSupportedException e) {} //cannot possibly happen
	long time = CommonState.getTime();
	long firsttime = nce.firstTime(sch);
	if( firsttime > time ) EDSimulator.add(firsttime-time, nce, node, pid);
}

// --------------------------------------------------------------------

/**
* Executes the nextCycle method of the protocol, and schedules the next call
* using the delay returned by {@link #nextDelay}.
*/
public final void execute(Scheduler sch) {

	int pid = CommonState.getPid();
	Node node = CommonState.getNode();
	CDProtocol cdp = (CDProtocol)node.getProtocol(pid);
	cdp.nextCycle(node,pid);
	
	long delay = nextDelay(sch);
	if( CommonState.getTime()+delay < sch.until )
		EDSimulator.add(delay, this, node, pid);

}

// --------------------------------------------------------------------

/**
* Schedules the object for the next execution
* adding it to the priority queue of the event driven simulation.
* This default impementation uses a constant delay equal to the step
* parameter of the schedule of this event (as set in the config file).
*/
protected long nextDelay(Scheduler sch) {
	
	return sch.step;
}

// --------------------------------------------------------------------

/**
* Returns the first time this even is executed.
* This implementation returns a randomly selected point within the cycle
* length, which is given by {@link Scheduler#step}.
* It also makes sure that the value is within the sceduled execution
* interval. In other words, it returns a random point from the interval
* that beginds with <pre>max(currentTime,from)</pre>, inclusive, and
* ends with <pre>min(max(currentTime,from)+step,until)</pre>, exclusive.
* @param sch is the schedule that contains the values from, until and step.
* @return the selected time point or -1, if there is no feasible one
*/
protected long firstTime(Scheduler sch) {
	
	long from = Math.max(CommonState.getTime(),sch.from);
	if( sch.until > from )
	{
		long delaybound = Math.min(sch.step,sch.until-from);
		return from+CommonState.r.nextLong(delaybound);
	}
	else
		return -1;
}

}


