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


public class EDFlood implements EDProtocol, Infectable
{

private static final String PAR_LINKABLE = "linkable";

private static final String PAR_TRANSPORT = "transport";

private static final Object INFECTION = new Object();

private class ProtocolData
{

  /** Protocol id */
  final int pid;  
  
  /** Linkable id */
  final int lid;

  /** Transport id */
  final int tid;
  
  ProtocolData(String prefix) 
  {
  	pid = CommonState.getPid();
  	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
  	tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
  }

}

/** */
int status;

/** The node hosting this protocol */
final Node node;

/** */
ProtocolData p;

public EDFlood(String prefix)
{
  p = new ProtocolData(prefix);
  node = CommonState.getNode();
	status = 0;
}

private EDFlood(ProtocolData p)
{
	this.p = p;
  node = CommonState.getNode();
	status = 0;
}

public Object clone()
{
	return new EDFlood(p);
}

public void processEvent(Node node, int pid, Object event)
{
	if (status == 0) {
		Linkable  l = (Linkable ) node.getProtocol(p.lid);
		Transport t = (Transport) node.getProtocol(p.tid);
		for (int i=0; i < l.degree(); i++) {
			t.send(node, l.getNeighbor(i), INFECTION, p.pid);
		}
		status = 1;
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
