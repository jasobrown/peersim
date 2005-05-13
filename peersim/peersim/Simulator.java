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

package peersim;

import peersim.config.*;
import peersim.core.*;
import peersim.cdsim.OrderSimulator;
import peersim.edsim.EDSimulator;


/**
* This is the main entry point to peersim. This class load configuration and
* detects the simulation type. According to this, it invokes the appropriate
* simulator. The known simulators at this moment, along with the way to
* detect them are the following:
* <ul>
* <li>{@link peersim.cdsim.Simulator}:
* if the configuration contains the property
* <code>simulation.cycles</code> and does not contain the property
* <code>simulation.order</code> then this standard cycle based simulator is
* invoked.
* <li>{@link OrderSimulator}: if the configuration contains the property
* <code>simulation.order</code> then this cycle based simulator is
* invoked, which uses a different style of configuration.
* <li>{@link EDSimulator}: if the configuration contains the property
* <code>simulation.endtime</code> then this standard event based simulator is
* invoked.
* </ul>
* Note that this class checks only for these clues and does not check if the
* configuration is consistent or valid.
*/
public class Simulator {

// ========================== static constants ==========================
// ======================================================================

/** {@link peersim.cdsim.Simulator} */
protected static final int CDSIM = 0;

/** {@link OrderSimulator} */
protected static final int CDORDSIM = 1;

/** {@link EDSimulator} */
protected static final int EDSIM = 2;

protected static final int UNKNOWN = -1;

/** the class names of simulators used */
protected static final String[] simName = {
	"peersim.cdsim.Simulator",
	"peersim.cdsim.OrderSimulator",
	"peersim.edsim.EDSimulator",
};

/**
 * Parameter representing the number of times the experiment is run.
 * Defaults to 1.
 */
public static final String PAR_EXPS = "simulation.experiments";
	
// ========================== methods ===================================
// ======================================================================

/**
* Returns the numeric id of the simulator to invoke.
*/
protected static int getSimID() {
	
	if( Configuration.getString(OrderSimulator.PAR_ORDER,null) != null )
	{
		return CDORDSIM;
	}
	else if( Configuration.getInt(peersim.cdsim.Simulator.PAR_CYCLES,-132)
								!= -132 )
	{
		return CDSIM;
	}
	else if( Configuration.getInt(EDSimulator.PAR_ENDTIME,-132) != -132 )
	{	
		return EDSIM;
	}
	else	return UNKNOWN;
}

// ----------------------------------------------------------------------

public static void main(String[] args)
{
	long time = System.currentTimeMillis();	
	
	System.err.println("Simulator: loading configuration");
	Configuration.setConfig( new ExtendedConfigProperties(args) );

	int exps = Configuration.getInt(PAR_EXPS,1);

	final int SIMID = getSimID();
	if( SIMID == UNKNOWN )
	{
		System.err.println(
		    "Simulator: unable to identify configuration, exiting.");
		return;
	}
	
	try {

		for(int k=0; k<exps; ++k)
		{
			System.err.print("Simulator: starting experiment "+k);
			System.err.println(" invoking "+simName[SIMID]);
			System.out.println("\n\n");
			
			// XXX could be done through reflection, but
			// this is easier to read.
			switch(SIMID)
			{
			case CDORDSIM:
				OrderSimulator.nextExperiment();
				break;
			case CDSIM:
				peersim.cdsim.Simulator.nextExperiment();
				break;
			case EDSIM:
				EDSimulator.nextExperiment();
				break;
			}
		}
	
	} catch (MissingParameterException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	} catch (IllegalParameterException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	}

	// undocumented testing capabilities
	if(Configuration.contains("__t")) 
		System.out.println(System.currentTimeMillis()-time);
	if(Configuration.contains("__x")) Network.test();
	
	
	
}

}
