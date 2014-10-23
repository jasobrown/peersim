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

private static final String PAR_INFECTABLE = "infectable";

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

  /** Infectable id */
  final int iid;

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
  	iid = Configuration.getPid(prefix + "." + PAR_INFECTABLE);
  	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
  	period = Configuration.getInt(prefix + "." + PAR_PERIOD);
  	prob = Configuration.getDouble(prefix + "." + PAR_PROB);
  }

}

/** */
int status;

/** */
ProtocolData p;

public EDMongering(String prefix)
{
	assert false;
  //p = new ProtocolData(prefix);
	status = SUSCEPTIBLE;
	System.err.print("fffslkfklsdlkdslksdlklksd");
}

private EDMongering(ProtocolData p)
{
	this.p = p;
	status = SUSCEPTIBLE;
	int r = CommonState.r.nextInt(p.period);
	EDSimulator.add(r, TIMEOUT, CommonState.getNode(), CommonState.getPid());
}

public Object clone()
{
	return new EDMongering(p);
}

public void processEvent(Node node, int pid, Object event)
{
	EDSimulator.add(p.period, TIMEOUT, node, pid);
	Linkable  l = (Linkable ) node.getProtocol(p.lid);
	Transport t = (Transport) node.getProtocol(p.tid);
	Infectable i = (Infectable) node.getProtocol(p.iid);
	if (event == TIMEOUT) {
		if (status == INFECTED  || i.isInfected()) {
			status = INFECTED;
			// Evitare di spedire alla cloud
			int r = CommonState.r.nextInt(l.degree());
			t.send(node, l.getNeighbor(r), INFECTION, pid);
			System.err.print("*");
			assert false;
		}
	} else if (event == INFECTION) {
		if (status == SUSCEPTIBLE) {
			status = INFECTED;
			i.setInfected(true);
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
		status = INFECTED;
	} else {
		status = SUSCEPTIBLE;
	}
}

public boolean isInfected()
{
	return status != SUSCEPTIBLE;
}


}
