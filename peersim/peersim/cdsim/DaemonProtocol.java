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
 
package peersim.cdsim;

import java.util.Arrays;
import peersim.config.Configuration;
import peersim.reports.Observer;
import peersim.dynamics.Dynamics;
import peersim.core.Node;
import peersim.core.CommonState;

/**
* A protocol that is not realy a protocol, but a trick to carry out all
* kinds of tasks during the simulation. Many users will probably not need it,
* but it is a nice way to eg run observers in any time, not only between cycles.
*/
public class DaemonProtocol implements CDProtocol {


// ========================= fields =================================
// ==================================================================


/**
* This is the prefix for network dynamism managers. These have to be of
* type {@link Dynamics}.
*/
public static final String PAR_DYN = "dynamics";

public static final String PAR_OBS = "observer";

/**
* The dynamics and observers will be run according to this frequency.
* It is interpreted within a cycle, in terms of cycle time
* ({@link CommonState#getCycleT}). The first cycletime is 0.
* Defaults to 1.
*/
public static final String PAR_STEP = "step";

// --------------------------------------------------------------------

/** holds the observers of this simulation */
private static Observer[] observers=null;

/** holds the modifiers of this simulation */
private static Dynamics[] dynamics=null;

private static int step;

// ========================= initialization =========================
// ==================================================================


public DaemonProtocol(String s)
{  
	step = Configuration.getInt(s+"."+PAR_STEP,1);
	
	// load analizers
	String[] names = Configuration.getNames(s+"."+PAR_OBS);
	observers = new Observer[names.length];
	for(int i=0; i<names.length; ++i)
	{
		observers[i]=(Observer)Configuration.getInstance(names[i]);
	}
	System.err.println(s+": loaded observers "+Arrays.asList(names));

	// load dynamism managers
	names = Configuration.getNames(s+"."+PAR_DYN);
	dynamics = new Dynamics[names.length];
	for(int i=0; i<names.length; ++i)
	{
		dynamics[i]=(Dynamics)Configuration.getInstance(names[i]);
	}
	System.err.println(s+": loaded modifiers "+Arrays.asList(names));
}

// ------------------------------------------------------------------

public Object clone() throws CloneNotSupportedException {

	DaemonProtocol ip = (DaemonProtocol)super.clone();
	return ip;
}


// ========================= methods =================================
// ===================================================================

	
public void nextCycle( Node node, int protocolID ) {

	if( CommonState.getCycleT() % step != 0 ) return;
	for(int j=0; j<dynamics.length; ++j) dynamics[j].modify();
	for(int j=0; j<observers.length; ++j) observers[j].analyze();
}

}

