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

import java.util.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
* This is the executable class for performing a cycle driven simulation.
* The class is completely static as at the same time we expect to have only
* one simulation running in a virtual machine.
* The simulation is highly configurable.
*/
public class Simulator {


// ============== fields ===============================================
// =====================================================================

/** 
 * Parameter representing the maximum number of cycles to be performed 
 * @config
 */
public static final String PAR_CYCLES = "simulation.cycles";

/**
* This option is only for experts. It switches off the main cycle that
* calles the cycle driven protocols. When you switch this off, you need to
* control the execution of the protocols by configuring controls that
* do the job. It's there for people who want maximal flexibility for their
* hacks.
 * @config
*/
private static final String PAR_NOMAIN = "simulation.nodefaultcycle";

/**
 * This is the prefix for initializers. These have to be of type
 * {@link Dynamics}.
 * @config
 */
private static final String PAR_INIT = "init";

/**
 * This is the prefix for controls.
 * @config
 */
private static final String PAR_CTRL = "control";


// --------------------------------------------------------------------

/** The maximum number of cycles to be performed */
private static int cycles;

/** holds the modifiers of this simulation */
private static Control[] controls=null;

/** Holds the control schedulers of this simulation */
private static Scheduler[] ctrlSchedules = null;


// =============== private methods =====================================
// =====================================================================

/**
 * Load and run initializers.
 */
private static void runInitializers() {
	
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);
	String names[] = Configuration.getNames(PAR_INIT);
	
	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " +names[i]+ ": " + inits[i].getClass());
		((Control)inits[i]).execute();
	}
}

// --------------------------------------------------------------------

private static String[] loadControls() {

	boolean nomaincycle = Configuration.contains(PAR_NOMAIN);
	String[] names = Configuration.getNames(PAR_CTRL);
	if(nomaincycle)
	{
		controls = new Control[names.length];
		ctrlSchedules = new Scheduler[names.length];
	}
	else
	{
		controls = new Control[names.length+1];
		ctrlSchedules = new Scheduler[names.length+1];
		// calling with a prefix that cannot exist
		controls[names.length]=new FullNextCycle(" ");
		ctrlSchedules[names.length] = new Scheduler(" ");
	}
	for(int i=0; i<names.length; ++i)
	{
		controls[i]=(Control)Configuration.getInstance(names[i]);
		ctrlSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded controls "+Arrays.asList(names));
	return names;
}

//---------------------------------------------------------------------

/**
 * Runs an experiment
 */
public static final void nextExperiment()  {

	// Reading parameter
	cycles = Configuration.getInt(PAR_CYCLES);
	CDState.setEndTime(cycles);

	// initialization
	CDState.setCycle(0);
	System.err.println("Simulator: resetting");
	Network.reset();
	System.err.println("Simulator: running initializers");
	runInitializers();
	
	// main cycle
	loadControls();
	System.err.println("Simulator: starting simulation");
	for(int i=0; i<cycles; ++i)
	{
		CDState.setCycle(i);

		boolean stop = false;
		for(int j=0; j<controls.length; ++j)
		{
			if( ctrlSchedules[j].active(i) )
				stop = stop || controls[j].execute();
		}
		if( stop ) break;
		System.err.println("Simulator: cycle "+i+" done");
	}

	CDState.setPhase(CDState.POST_SIMULATION);

	// analysis after the simulation
	for(int j=0; j<controls.length; ++j)
	{
		if( ctrlSchedules[j].fin() ) controls[j].execute();
	}
}

}


