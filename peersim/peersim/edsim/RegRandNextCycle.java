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
import peersim.config.Configuration;


/**
* Implements a random delay, but making sure there is one call in each
* consequtive <code>step</code> time units.
*/
public class RegRandNextCycle extends NextCycleEvent {

// ============================== fields ==============================
// ====================================================================


/**
* If set, the beginning of the first cycle will be initialized to the
* time of construction. It means that if all protocols are added at once,
* all will have to same idea of the beginning and end of the cycle.
* Not set by default. In that case the beginning of the cycle is initialized
* to be the time of first invocation, which is a random time.
* @config
*/
private static final String PAR_STARTNOW = "startnow";

/**
* Indicates the start of the next cycle for a particular protocol
* instance. If negative it means it has not been initialized yet.
*/
private long nextCycleStart = -1;

// =============================== initialization ======================
// =====================================================================


/**
* Calls super constructor.
*/
public RegRandNextCycle(String n) {

	super(n);
	if( Configuration.contains(n+"."+PAR_STARTNOW) )
		nextCycleStart = CommonState.getTime();
}

// --------------------------------------------------------------------

/**
* Calls super.clone().
*/
protected Object clone() throws CloneNotSupportedException {
	
	return super.clone();
}



// ========================== methods ==================================
// =====================================================================


/**
* Returns a random delay but making sure there is one invocation in each
* consequtive interval of length <code>step</code>.
*/
protected long nextDelay(Scheduler sch) {
	
	final long now = CommonState.getTime();
	if(nextCycleStart<0)
	{
		// not initialized
		nextCycleStart=now;
	}

	while(nextCycleStart<now) nextCycleStart+=sch.step;
	nextCycleStart+=sch.step;
	return nextCycleStart-now-CommonState.r.nextLong(sch.step);
}

}


