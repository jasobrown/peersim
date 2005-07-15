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
		
package peersim.core;

import peersim.config.Configuration;

// XXX a quite primitive scheduler, should be able to be configured
// much more flexibly using a simlpe syntax for time ranges.
/**
* A binary function over the time points. That is,
* for each time point returns a boolean value.
* 
* The concept of time depends on the simulation model. Current time
* has to be set by the simulation engine, irrespective of the model,
* and can be read using {@link CommonState.getTime()}. This schedular
* is interpreted over those time points.
*
* <p>In this simple implementation the valid times will be
* <tt>from, from+step, from+2*step, etc,</tt>
* where the last element is strictly less than <tt>until</tt>.
* Alternatively, if <tt>at</tt> is defined, then the schedule will be a single
* time point. If FINAL is
* defined, it is also added to the set of active time points.
*/
public class Scheduler {


// ========================= fields =================================
// ==================================================================


/** 
* Defaults to 1.
*/
public static final String PAR_STEP = "step";

/** 
* Defaults to -1.
*/
public static final String PAR_AT = "at";


/** 
* Defaults to 0.
*/
public static final String PAR_FROM = "from";

/** 
* Defaults to <tt>Integer.MAX_VALUE</tt>.
*/
public static final String PAR_UNTIL = "until";

/**
* Defines if component is active after the last cycle has finished.
* Note that the index of last cycle is not know in advance because other
* components can stop the simulation at any time.
* By default not set.
*/
public static final String PAR_FINAL = "FINAL";

protected final long step;

protected final long from;

protected final long until;

protected final boolean fin;

/** The next scheduled time point.*/
protected long next;

// ==================== initialization ==============================
// ==================================================================


public Scheduler(String prefix) {
	
	this(prefix, true);
}

// ------------------------------------------------------------------

public Scheduler(String prefix, boolean useDefault)
{
	long at = Configuration.getInt(prefix+"."+PAR_AT,-1);
	if( at < 0 )
	{
		if (useDefault) 
			step = Configuration.getInt(prefix+"."+PAR_STEP,1);
		else
			step = Configuration.getInt(prefix+"."+PAR_STEP);
		from = Configuration.getInt(prefix+"."+PAR_FROM,0);
		until = Configuration.getInt(
				prefix+"."+PAR_UNTIL,Integer.MAX_VALUE);
	}
	else
	{
		from = at;
		until = at+1;
		step = 1;
	}
	next = from;
	fin = Configuration.contains(prefix+"."+PAR_FINAL);
}


// ===================== public methods ==============================
// ===================================================================


public boolean active(long time) {
	
	if( time < from || time >= until ) return false;
	return (time - from)%step == 0; 
}

// -------------------------------------------------------------------

public boolean active() {
	
	return active( CommonState.getTime() );
}

// -------------------------------------------------------------------

public boolean fin() { return fin; }

//-------------------------------------------------------------------

/**
* Returns the next time point. If the returned value is negative, there are
* no more time points. As a side effect, it also updates the next time point,
* so repreated calls to this method return the scheduled times.
*/
public long getNext()
{
	long ret = next;
	next += step;
	if( next >= until ) next = -1;
	return ret;
}

//-------------------------------------------------------------------

}


