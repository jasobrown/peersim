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
		
package peersim.vector;

import peersim.core.*;
import peersim.reports.Observer;
import peersim.config.Configuration;
import peersim.util.IncrementalStats;

/**
* Observes difference between two vectors.
* It computes the distance vector abs(x-y), and reports statistics on this
* vector such as average, minimum and maximum (according to the string
* format of {@link IncrementalStats}.
*/
public class VectDistance implements Observer {

/** 
* String name of the parameter that defines the first protocol to observe.
*/
public static final String PAR_PROT1 = "protocol1";

/** 
* String name of the parameter that defines the second protocol to observe.
*/
public static final String PAR_PROT2 = "protocol2";

private final String name;

private final int pid1;

private final int pid2;



//--------------------------------------------------------------------------

public VectDistance(String prefix)
{
	name = prefix;
	pid1 = Configuration.getPid(prefix+"."+PAR_PROT1);
	pid2 = Configuration.getPid(prefix+"."+PAR_PROT2);
}

//--------------------------------------------------------------------------

public boolean analyze() {
	
	System.out.print(name+": ");
	
	IncrementalStats is = new IncrementalStats();
	for(int i=0; i<Network.size(); ++i)
	{
		SingleValue sv1 =
			(SingleValue)Network.get(i).getProtocol(pid1),
			    sv2 =
			(SingleValue)Network.get(i).getProtocol(pid2);
		is.add(Math.abs(sv1.getValue()-sv2.getValue()));
	}

	System.out.println(is);

	return false;
}

}



