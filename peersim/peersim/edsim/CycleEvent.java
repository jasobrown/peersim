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

import peersim.cdsim.*;
import peersim.core.*;
import peersim.dynamics.*;
import peersim.reports.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
class CycleEvent
{

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** 
 * The reference to the observer to be executed; null if this cycle event
 * refers to a dynamics.
 */
private Observer observer;

/** 
 * The reference to the dynamics to be executed; null if this cycle event
 * refers to an observer.
 */
private Dynamics dynamics;

/** Identifier of the protocol to be executed */
private int pid;

/** Order index used to maintain order between cycle-based events */
private int order;

/** Phase identifier; only for observers */
private int phase;


//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/** 
 * Scheduler object to obtain the next schedule time of this event 
 */
private Scheduler scheduler;

/**
 * Creates a cycle event for an observer
 */
CycleEvent(Observer observer, Scheduler scheduler, int order, int phase)
{
	this.observer = observer;
	this.phase = phase;
	setScheduler(scheduler, order);
}

//---------------------------------------------------------------------

/**
 * Creates a cycle event for a dynamics
 */
CycleEvent(Dynamics dynamics, Scheduler scheduler, int order)
{
	this.dynamics = dynamics;
	setScheduler(scheduler, order);
}

//---------------------------------------------------------------------

/**
 * Creates a cycle event for a cycle-driven protocol.
 */
CycleEvent(int pid, Scheduler scheduler, int order)
{
	this.pid = pid;
	setScheduler(scheduler, order);
}

//---------------------------------------------------------------------

private void setScheduler(Scheduler scheduler, int order)
{
	this.scheduler = scheduler;
	this.order = order;
	EDSimulator.addCycleEvent(scheduler.getNext(), order, this);
}


//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

/**
 * 
 */
public boolean execute(boolean shuffle)
{
	boolean ret = false;
  if (observer != null) {
  	// It's an observer
  	CommonState.setPhase(phase);
  	ret = observer.analyze();
  } else if  (dynamics != null) {
  	// It's a dynamics
  	dynamics.modify();
  } else {
  	// It's a protocol
		if (shuffle) 
			Network.shuffle();
		CommonState.setPid(pid);
		int size = Network.size();
		for (int i =0; i < size; i++) {
			CommonState.setCycleT(i);
			Node node = Network.get(i);
			CDProtocol prot = (CDProtocol) node.getProtocol(pid);
			prot.nextCycle(node, pid);
		}
  }
  long next = scheduler.getNext();
	EDSimulator.addCycleEvent(next, order, this);
	return ret;
}

//---------------------------------------------------------------------

}


