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

import java.util.*;
import peersim.config.*;
import peersim.core.*;


/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class Simulator
{

/**
 * Parameter representing the number of times the experiment is run.
 * Defaults to 1.
 */
public static final String PAR_EXPS = "simulation.experiments";
	
	
public static void main(String[] args)
{
	long time = System.currentTimeMillis();	
	
	System.err.println("Simulator: loading configuration");
	Configuration.setConfig( new ConfigProperties(args) );

	int exps = Configuration.getInt(PAR_EXPS,1);

	try {

		for(int k=0; k<exps; ++k)
		{
		  System.out.println("\n\n");
			System.err.println("Simulator: starting experiment "+k);
			ArrayList list = new ArrayList();
		  String msg;
		  
		  // Try to use OrderSimulator
		  msg = peersim.cdsim.OrderSimulator.nextExperiment();
		  if (msg == null)
		  	continue;
		  list.add(msg);
		  
		  msg = peersim.cdsim.Simulator.nextExperiment();
		  if (msg == null)
		  	continue;
		  list.add(msg);
			
		  msg = peersim.edsim.EDSimulator.nextExperiment();
		  if (msg == null)
		  	continue;
		  list.add(msg);
			
		  for (int i=0; i < list.size(); i++) {
		  	System.out.println(list.get(i));
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
