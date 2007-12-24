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

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;


public class CDFlood implements CDProtocol, Infectable
{

private static final String PAR_LINKABLE = "linkable";

private class ProtocolData
{

  /** Protocol id */
  final int pid;  
  
  /** Linkable id */
  final int lid;

  ProtocolData(String prefix) 
  {
  	pid = CommonState.getPid();
  	lid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
  }

}

/** */
int status;

/** */
ProtocolData p;

public CDFlood(String prefix)
{
  p = new ProtocolData(prefix);
	status = 0;
}

public CDFlood(ProtocolData p)
{
	this.p = p;
	status = 0;
}

public Object clone()
{
	return new CDFlood(p);
}

public void nextCycle(Node node, int protocolID)
{
	if (status == 1) {
		Linkable l = (Linkable) node.getProtocol(p.lid);
		for (int i=0; i < l.degree(); i++) {
			CDFlood peer = (CDFlood) l.getNeighbor(i).getProtocol(p.pid);
			if (peer.status == 0)
				peer.status = 1;
		}
		status = 2;
	}
}

public void setInfected(boolean infected)
{
	if (infected)
		status = 1;
	else
		status = 0;
}

public boolean isInfected()
{
	return status>0;
}


}
