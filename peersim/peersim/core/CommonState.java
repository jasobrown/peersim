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

import  peersim.util.ExtendedRandom;
import  peersim.config.Configuration;

/**
 * This is the common state of the simulation all objects see. It's purpose is
 * simplification of parameter structures and increasing efficiency by putting
 * state information here instead of passing parameters. Fully static class, a
 * singleton.
 *<p>
 * The set methods should not be used by applications, they are for system
 * components. Ideally, they should not be visible, but due to the lack of
 * flexibility in java access rights, we are forced to make them visible.
 */
public class CommonState
{

//======================= constants ===============================
//=================================================================

public static final int POST_SIMULATION = 1;

// ======================= fields ==================================
// =================================================================

/**
 * Current time. Note that this value is simulator independent, all simulation
 * models have a notion related to time. For example, in the cycle based model,
 * the cycle id gives time, while in even driven simulations all events have
 * a timestamp.
 */
private static long time = 0;

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

/**
* This source of randomness should be used by all components.
* This field is public because it doesn't matter if it changes
* during an experiment (although it shouldn't) until no other sources of
* randomness are used within the system. Besides, we can save the cost
* of calling a wrapper method, which is important becuase this is needed
* very often.
*/
public static ExtendedRandom r = null;


// ======================== initialization =========================
// =================================================================


/**
* Configuration parameter used to initialize the random seed.
* If it is not specified the current time is used.
*/
public static String PAR_SEED = "random.seed";


/**
* Initializes the field {@link r} according to the configuration.
* Assumes that the configuration is already
* loaded.
*/
static {
	
	long seed =
		Configuration.getLong(PAR_SEED,System.currentTimeMillis());
	r = new ExtendedRandom(seed);
}


// ======================= methods =================================
// =================================================================


/**
 * Returns current time. In event-driven simulations, returns the current
 * time (a long-value).
 * In cycle-driven simulations, returns the current cycle (a long that
 * can safely be cast into an integer). Initializers, observers, and
 * dynamics should always call this method instead of {@link #getCycle()};
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

/**
* Returns the current protocol identifier. In other words, control is
* held by the indicated protocol on node {@link #getNode}.
*/
public static int getPid()
{
	return pid;
}

//-----------------------------------------------------------------

/** Sets the current protocol identifier.*/
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

}


