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

import peersim.cdsim.*;
import peersim.core.*;
import peersim.dynamics.*;
import peersim.reports.*;


/**
 * This class contains the central engine of the event-driven simulator
 * of Peersim. The class maintains an ordered list of events; new events
 * are added by invoking method #addd(int, Node, Object, int),
 * while method @link #schedule(int) is used to execute the scheduled
 * events.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class EventHandler
{

//---------------------------------------------------------------------
//Constants
//---------------------------------------------------------------------

/** Numeric id used to identify observers */
protected static final int OBSERVER = -1;

/** Numeric id used to identify dynamics*/
protected static final int DYNAMICS = -2;

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Ordered list of events (heap) */
private static Heap heap = new Heap();

/** If true, when executing a cycle-based protocols nodes are shuffled */
private static boolean shuffle;

//---------------------------------------------------------------------
//Methods
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
public static void add(int delay, Object event, Node node, int pid)
{
	if (pid > Byte.MAX_VALUE) 
		throw new IllegalArgumentException("This version does not support more than " 
				+ Byte.MAX_VALUE + " protocols");
	heap.add(CommonState.getT()+delay, event, node, (byte) pid);
}

//---------------------------------------------------------------------

/**
 * Execute and remove the next event from the ordered event list.
 * @return true if the execution should be stopped.
 */
public static boolean executeNext(int endtime)
{
	Heap.Event ev = heap.removeFirst();
	if (ev.time > endtime)
		return true;
	CommonState.setT(ev.time);
  int pid = ev.pid;
	if (ev.node == null) {
		// Cycle-based event; handled through a special method
		return handleCycleEvent(ev);
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

/**
 * Executes a cycle-based event. If ev.pid < 0, this event corresponds to an 
 * observer or a dynamics. If it larger than 0, it corresponds to the execution
 * of a CDProtocol.
 *
 * @param ev The event to be executed.
 * @return
 */
private static boolean handleCycleEvent(Heap.Event ev) 
{
	int pid = ev.pid;
	if (pid == OBSERVER) {
		Observer observer = (Observer) ev.event;
		return observer.analyze();
	} else if (pid == DYNAMICS) {
		Dynamics dynamics = (Dynamics) ev.event;
		dynamics.modify();
	} else if (pid >= 0) {
		if (shuffle) 
			Network.shuffle();
		CommonState.setPid(pid);
		int size = Network.size();
		for (int i =0; i < size; i++) {
			Node node = Network.get(i);
			CDProtocol prot = (CDProtocol) node.getProtocol(pid);
			prot.nextCycle(node, pid);
		}
	} else {
   	throw new IllegalStateException("Illegal process identifier");
  }
  return false;
}

//---------------------------------------------------------------------

/**
 * @return Returns the shuffle.
 */
public static boolean isShuffle()
{
	return shuffle;
}

//---------------------------------------------------------------------
/**
 * @param shuffle The shuffle to set.
 */
public static void setShuffle(boolean shuffle)
{
	EventHandler.shuffle = shuffle;
}

//---------------------------------------------------------------------

}
