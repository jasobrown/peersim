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

package peersim.extras.am.epidemic.chord;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.util.*;
import peersim.transport.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class LatencyComparator implements NodeComparator
{

private static final String PAR_HOLDER = "holder";

private static final String PAR_TRANSPORT = "transport";

private final int hid;

private final int tid;

/**
 * 
 */
public LatencyComparator(String prefix)
{
	hid = Configuration.getInt(prefix + "." + PAR_HOLDER);
	tid = Configuration.getInt(prefix + "." + PAR_TRANSPORT);
}

public int compare(Node src, Node node1, Node node2)
{
	Transport t = (Transport) src.getProtocol(tid);
	long dist1 = t.getLatency(src, node1);
	long dist2 = t.getLatency(src, node2);
	if (dist1 < dist2)
		return -1;
	else if (dist1 == dist2)
		return 0;
	else
		return 1;
}
}
