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

/**
* This is the common state of the simulation all objects see.
* It's purpose is simplification of parameter structures and increasing
* efficiency by putting
* state information here instead of passing parameters.
* Fully static class, a singleton.
*/
public class CommonState {


// ======================= fields ==================================
// =================================================================

/**
* Current time in the simulation. It's public because eevrything would have
* read and write rights anyway.
*/
private static int time = 0;

/**
 * The current pid.
 */
private static int pid;

/**
* Working variable to provide for an object version of time.
* This is useful to save memory because all objects can use the same object.
*/
private static Integer _time = new Integer(0);

/**
* Information about where exactly the simulation is. 
*/
private static int phase = 0;

public static final int PRE_DYNAMICS = 0;

public static final int PRE_CYCLE = 1;

public static final int POST_LAST_CYCLE = 2;


// ======================== initialization =========================
// =================================================================

/**
*/
static { }


// ======================= methods =================================
// =================================================================


/** Returns the current time. */
public static int getT() { return time; }

// -----------------------------------------------------------------

public static void setT( int t ) {
	
	_time = new Integer(t);
	time = t;
}

// -----------------------------------------------------------------

/** Returns current time as an Integer object */
public static Integer getTimeObj() {  return _time; }

// -----------------------------------------------------------------

/**
* Returns the phase within a time step. Currently the following phases are
* understood.
* <ul>
* <li> {@link #PRE_DYNAMICS} Nothing has been done in the current cycle </li>
* <li> {@link #PRE_CYCLE} The dynamism managers have been run but the
* cycle has not started yet </li>
* <li> {@link #POST_LAST_CYCLE} the simulation is completed </li>
* </ul>
*/
public static int getPhase() { return phase; }

// -----------------------------------------------------------------

public static void setPhase( int p ) { phase = p; }

// -----------------------------------------------------------------

/** Returns the current protocol identifier */
public static int getPid()
{
	return pid;
}

//-----------------------------------------------------------------

public static void setPid(int p)
{
	pid = p;
}

}

