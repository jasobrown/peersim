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

package peersim.cdsim;

import peersim.core.CommonState;


/**
 * This is the common state of a cycle driven simulation that all objects see.
 * It contains additional information, specific to the cycle driven model,
 * in addition to the info in {@link peersim.core.CommonState}. It's purpose is
 * simplification of parameter structures and increasing efficiency by putting
 * state information here instead of passing parameters. Fully static class, a
 * singleton.
 *<p>
 * The set methods should not be used by applications, they are for system
 * components. Ideally, they should not be visible, but due to the lack of
 * flexibility in java access rights, we are forced to make them visible.
 */
public class CDState extends CommonState {


// ======================= fields ==================================
// =================================================================

/**
 * Current time within the current cycle, for cycle based simulations.
 * Note that {@link #cycle} gives the cycle id to which this value is relative.
 */
private static int ctime = -1;

/**
 * Current cycle in the simulation. It makes sense only in the case of a
 * cycle based simulator, that is, cycle based simulators will maintain this
 * value, others will not. It still makes sense to keep it separate from
 * {@link #time} because it is an int, while time is a long.
 */
private static int cycle = -1;


// ======================== initialization =========================
// =================================================================


static {}


// ======================= methods =================================
// =================================================================


/**
* Returns true if and only if there is a cycle driven simultion going on.
* If it returns false, then the methods of this class throw a runtime
* exception, since no cycle information is available.
*/
public static boolean isCD() { return cycle >= 0; }

//-----------------------------------------------------------------

/**
 * In cycle-driven simulations, returns the current cycle. Otherwise
 * returns -1. In cycle drive simulations {@link #getTime()} returns the
 * same value.
 */
public static int getCycle()
{
	if( cycle >= 0 ) return cycle;
	else throw new RuntimeException("Cycle driven state accessed when "+
		"no cycle state information is available.");
}

//-----------------------------------------------------------------

/**
 * Sets current cycle. Used by the cycle based simulators. Resets also cycle
 * time to 0. It also calls
 * {@link #setTime(long)} with the given parameter, to make sure 
 * {@link #getTime()} is indeed independent of the simulation model.
 */
public static void setCycle(int t)
{
	cycle = t;
	ctime = 0;
	setTime(t);
}

//-----------------------------------------------------------------

/**
 * Returns current cycle as an Integer object.
 */
public static Integer getCycleObj()
{
	if( cycle >= 0 ) return Integer.valueOf(cycle);
	else throw new RuntimeException("Cycle driven state accessed when "+
		"no cycle state information is available.");
}

//-----------------------------------------------------------------

/**
 * Returns the current time within the current cycle, for cycle based
 * simulations. Note that the time returned by {@link #getCycle} is the cycle id
 * in this case. In other words, it returns the number of nodes that have
 * already been visisted in a given cycle. It is negative if the simulation
 * is not cycle driven.
 */
public static int getCycleT()
{
	if( ctime >= 0 ) return ctime;
	else throw new RuntimeException("Cycle driven state accessed when "+
		"no cycle state information is available.");
}

// -----------------------------------------------------------------

public static void setCycleT(int t)
{
	ctime = t;
}
}


