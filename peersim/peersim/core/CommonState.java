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
 * This is the common state of the simulation all objects see. It's purpose is
 * simplification of parameter structures and increasing efficiency by putting
 * state information here instead of passing parameters. Fully static class, a
 * singleton.
 */
public class CommonState
{

//======================= constants ===============================
//=================================================================

public static final int PRE_DYNAMICS = 0;

public static final int PRE_CYCLE = 1;

public static final int POST_LAST_CYCLE = 2;

// ======================= fields ==================================
// =================================================================

/**
 * Current time within the current cycle, for cycle based simulations.
 * Note that {@link #cycle} gives the cycle id to which this value is relative.
 */
private static int ctime = 0;

/**
 * Current cycle in the simulation. It makes sense only in the case of a
 * cycle based simulator, that is, cycle based simulators will maintain this
 * value, others will not. It still makes sense to keep it separate from
 * {@link #time} because it is an int, while time is a long.
 */
private static int cycle = 0;

/**
 * Current time. Note that this value is simulator independent, all simulation
 * models have a notion related to time. For example, in the cycle based model,
 * the cycle id gives time, while in even driven simulations all events have
 * a timestamp.
 */
private static long time = 0;

/**
 * Working variable to provide for an object version of cycle. This is useful to
 * save memory because all objects can use the same object.
 */
private static Integer _cycle = new Integer(0);

/**
 * Information about where exactly the simulation is.
 */
private static int phase = 0;

/**
 * The current pid.
 */
private static int pid;

/**
 * The current node.
 */
private static Node node;

// ======================== initialization =========================
// =================================================================

/**
 */
static {
}

// ======================= methods =================================
// =================================================================

/**
 * In cycle-driven simulations, returns the current cycle. In
 * event-driven simulations, returns -1.
 */
public static int getCycle()
{
	return cycle;
}

// -----------------------------------------------------------------

/**
 * Returns the current cycle. This method is deprecated; method
 * @link #getCycle() should be used instead.
 * 
 * @deprecated
 */
public static int getT()
{
	return cycle;
}

//-----------------------------------------------------------------

/**
 * Sets current cycle. Used by the cycle based simulators. Resets also cycle
 * time to 0. It also calls
 * {@link #setTime} with the given parameter, to make sure {@link #getTime}
 * is indeed independent of the simulation model.
 */
public static void setCycle(int t)
{
	_cycle = new Integer(t);
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
	return _cycle;
}

//-----------------------------------------------------------------

/**
 * Returns current time. In event-driven simulations, returns the current
 * time (a long-value).
 * In cycle-driven simulations, returns the current cycle (a long that
 * can safely be cast into an integer). Initializers, observers, and
 * dynamics should always call this method instead of {@link getCycle()};
 * in this way, they are compatible with both cycle-driven and event-driven
 * simulations.
 */
public static long getTime()
{
	return time;
}

//-----------------------------------------------------------------

/**
 * Sets the current time. 
 */
public static void setTime(long t)
{
	time = t;
}

//-----------------------------------------------------------------

/**
 * Returns the phase within a time step. Currently the following phases are
 * understood.
 * <ul>
 * <li>{@link #PRE_DYNAMICS}Nothing has been done in the current cycle</li>
 * <li>{@link #PRE_CYCLE}The dynamism managers have been run but the cycle has
 * not started yet</li>
 * <li>{@link #POST_LAST_CYCLE}the simulation is completed</li>
 * </ul>
 */
public static int getPhase()
{
	return phase;
}

// -----------------------------------------------------------------

public static void setPhase(int p)
{
	phase = p;
}

// -----------------------------------------------------------------

/** Returns the current protocol identifier */
public static int getPid()
{
	return pid;
}

//-----------------------------------------------------------------

/** Sets the current protocol identifier */
public static void setPid(int p)
{
	pid = p;
}

//-----------------------------------------------------------------

/**
 * Returns the current node. When a protocol is executing, it is the node
 * hosting the protocol.
 */
public static Node getNode()
{
	return node;
}

//-----------------------------------------------------------------

/** Sets the current node */
public static void setNode(Node n)
{
	node = n;
}

//-----------------------------------------------------------------

/**
 * Returns the current time within the current cycle, for cycle based
 * simulations. Note that the time returned by {@link #getCycle}is the cycle id
 * in this case.
 */
public static int getCycleT()
{
	return ctime;
}

// -----------------------------------------------------------------

public static void setCycleT(int t)
{
	ctime = t;
}
}
