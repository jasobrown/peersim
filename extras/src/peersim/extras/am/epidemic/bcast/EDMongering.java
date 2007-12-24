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

package peersim.extras.am.epidemic.bcast;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.*;


public class EDMongering implements EDProtocol, Infectable
{

private static final String PAR_LINKABLE = "linkable";

private static final String PAR_TRANSPORT = "transport";

private static final String PAR_PROB = "prob";

private static final String PAR_PERIOD = "period";

private static final Object INFECTION = new Object();

private static final Object TIMEOUT = new Object();

private static final int SUSCEPTIBLE = 0;

private static final int INFECTED = 1;

private static final int REMOVED = 2;


private class ProtocolData
{

  /** Protocol id */
  final int pid;  
  
  /** Linkable id */
  final int lid;

  /** Transport id */
  final int tid;
  
  /** Probability of stopping */
  final double prob;

  /* Cycle length */
  final int period;
  
  ProtocolData(String prefix) 
  {
  	pid = CommonState.getPid();
  	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
  	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
  	period = Configuration.getInt(prefix + "." + PAR_PERIOD);
  	prob = Configuration.getDouble(prefix + "." + PAR_PROB);
  }

}

/** */
int status;

/** The node hosting this protocol */
final Node node;

/** */
ProtocolData p;

public EDMongering(String prefix)
{
  p = new ProtocolData(prefix);
  node = CommonState.getNode();
	status = SUSCEPTIBLE;
}

private EDMongering(ProtocolData p)
{
	this.p = p;
  node = CommonState.getNode();
	status = SUSCEPTIBLE;
}

public Object clone()
{
	return new EDMongering(p);
}

public void processEvent(Node node, int pid, Object event)
{
	if (status == 2)
		return;
	Linkable  l = (Linkable ) node.getProtocol(p.lid);
	Transport t = (Transport) node.getProtocol(p.tid);
	if (event == TIMEOUT) {
		if (status == SUSCEPTIBLE)
			status = INFECTED;
		if (status == INFECTED) {
			EDSimulator.add(p.period, TIMEOUT, node, pid);
			int r = CommonState.r.nextInt(l.degree());
			t.send(node, l.getNeighbor(r), INFECTION, pid);
		}
	} else if (event == INFECTION) {
		if (status == SUSCEPTIBLE) {
			status = INFECTED;
			EDSimulator.add(p.period, TIMEOUT, node, pid);
			int r = CommonState.r.nextInt(l.degree());
			t.send(node, l.getNeighbor(r), INFECTION, pid);
		} else if (status == INFECTED) {
			float chance = CommonState.r.nextFloat();
			if (chance < p.prob)
				status = REMOVED;
		}
		
	}
	                     
}

public void setInfected(boolean infected)
{
	if (infected) {
		if (status == 0) {
			Linkable  l = (Linkable ) node.getProtocol(p.lid);
			Transport t = (Transport) node.getProtocol(p.tid);
			for (int i=0; i < l.degree(); i++) {
				t.send(node, l.getNeighbor(i), INFECTION, p.pid);
			}
			status = 1;
		}
	} else {
		status = 0;
	}
}

public boolean isInfected()
{
	return status != 0;
}


}
