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

package aggregation.event;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.util.*;
import peersim.transport.*;
import aggregation.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class EventAggregation implements Aggregation, EDProtocol
{

private static boolean DEBUG = false;
	
/**
 * String name of the parameter used to configure the identifier
 * of the linkable protocol.
 */	
private static final String PAR_LINKABLE = "linkable";

/**
 * String name of the parameter used to configure the identifier
 * of the transport protocol.
 */	
private static final String PAR_TRANSPORT = "transport";
	
/**
 * String name of the parameter used to configure the length of a cycle
 */	
private static final String PAR_CLENGTH = "cyclelength";
	
/** Identifier of the linkable ID */
private static int linkableID;

/** Identifier of the transport ID */
private static int transportID;
	
/** Length (in time units) of a cycle */
private static int cyclelength;

/** Local value */
private double value;

/** Current cycle */
private short cycle = 0;

/** Remote node that we initiate a protocol with */
private Node remote;

private boolean initiated = false;

/**
 * 
 */
public EventAggregation(String prefix)
{
	linkableID = Configuration.getPid(prefix+"."+PAR_LINKABLE);
	transportID = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
  cyclelength = Configuration.getInt(prefix+"."+PAR_CLENGTH);
	initSchedule();
}

public Object clone() throws CloneNotSupportedException
{
	EventAggregation ev = (EventAggregation) super.clone();
	ev.initSchedule();
	return ev;
}

private void initSchedule()
{
	// Set up first event. We assume that clocks are synchronized in a
	// 1-second range
	Node node = CommonState.getNode();
	int delay = CommonRandom.r.nextInt(5000);
	Message msg = new Message(true, node, this, cycle, 0);
	EventHandler.add(delay, msg, node, CommonState.getPid());
}

// Comment inherited from interface
public void processEvent(Node node, int pid, Object event)
{
	if (event == null)  {
		// timeout
		initiated = false;
		return;
	}
	
	Message msg = (Message) event;
	if (msg.src == node) { // It's a timer event
		
		cycle++;
		
		// Select a random neighbor
		Linkable link = (Linkable) node.getProtocol(linkableID);
    if (link.degree() <= 0) { 
			System.out.print(link.degree()+" ");
			return;
	  }
		int r = CommonRandom.r.nextInt(link.degree());
		remote = link.getNeighbor(r);
		Transport trans = (Transport) node.getProtocol(transportID);
		
		// Prepares message and sends it.
		if (DEBUG && value > 0) {
			System.out.println(node.getIndex() + ": Sent init msg " + CommonState.getT() + " " + value);
		}

		EventHandler.add(2000, null, node, pid);
		
		Message request = new Message(true, node, this, cycle, value);
		trans.send(node, remote, request, pid);
		initiated = true;

		// Creates a new timer event (re-using the same message)
		EventHandler.add(cyclelength, msg, node, pid);
		
	} else { // it's a real message
	  
		
		// Check if this message has been initiated
		if (msg.initiated) { 
			if (initiated) {
				return;
			}
			
			// XXX We should not respond if we have initiated an exchange
			// Just respond and update the local value
			double rv = msg.value;
			Node sender = msg.src;
			msg.set(false, node, this, value);
			Transport trans = (Transport) node.getProtocol(transportID);
			trans.send(node, sender, msg, pid);
			double old = value;
			value = (rv + value) / 2;

			if (DEBUG && (value > 0 || msg.value > 0)) {
				System.out.println(node.getIndex() + " at " + CommonState.getT() + 
						": Received request msg from " + sender.getIndex() + " Recv value: " + 
						rv + " Old value " + old + " new value " + value);
			}
			
		} else {
			// Do some checkes 
			if (DEBUG && (value > 0 || msg.value > 0)) {
				System.out.println(node.getIndex() + " at " + CommonState.getT() + 
						": Received response msg from " + msg.src.getIndex() + " Recv value: " + 
						msg.value + " Old value " + value + " new value " + (msg.value + value) / 2);
				System.out.println("msg.sender " + msg.src.getIndex() +
						" remote " + remote.getIndex() +
						" msg.cycle " + msg.cycle +
						" cycle " + cycle);
			}
			if (msg.src == remote && msg.cycle == cycle) {
				value = (msg.value + value) / 2; 
				initiated = false;
			} 
		}	
	}
}

// Comment inherited from interface
public double getValue()
{
	return value;
}

// Comment inherited from interface
public void setValue(double value)
{
	this.value = value;
}

class Message 
{
	boolean initiated;
  Node src;
  EventAggregation sender;
	short cycle;
	double value;

	/**
	 * @param src
	 * @param sender
	 * @param value
	 */
	Message(boolean initiated, Node src, EventAggregation sender, short cycle, double value)
	{
		this.initiated = initiated;
		this.src = src;
		this.sender = sender;
		this.cycle = cycle;
		this.value = value;
	}

	/**
	 * @param src
	 * @param sender
	 * @param value
	 */
	void set(boolean initiated, Node src, EventAggregation sender, double value)
	{
		this.initiated = initiated;
		this.src = src;
		this.sender = sender;
		this.value = value;
	}

}	


}
