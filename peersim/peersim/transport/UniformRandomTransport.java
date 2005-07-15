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

package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.util.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class UniformRandomTransport implements Transport
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** String name of the parameter used to configure the minimum latency */	
private static final String PAR_MINDELAY = "mindelay";	
	
/** String name of the parameter used to configure the maximum latency */	
private static final String PAR_MAXDELAY = "maxdelay";	
	
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Minimum delay for message sending */
private int min;
	
/** Delay range */
private int range;

	
//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * 
 */
public UniformRandomTransport(String prefix)
{
	min = Configuration.getInt(prefix + "." + PAR_MINDELAY);
	int max = Configuration.getInt(prefix + "." + PAR_MAXDELAY);
	if (max < min) 
	   throw new IllegalParameterException(prefix+"."+PAR_MAXDELAY, 
	   "The maximum latency cannot be smaller than the minimum latency");
	range = max-min+1;
}

//---------------------------------------------------------------------

public Object clone()
{
	return this;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

// Comment inherited from interface
public void send(Node src, Node dest, Object msg, int pid)
{
	int delay = min + CommonState.r.nextInt(range);
	EDSimulator.add(delay, msg, dest, pid);
}

//Comment inherited from interface
public int getLatency(Node src, Node dest)
{
	return min + CommonState.r.nextInt(range);
}


}
