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
* Observes the cosine angle between two vectors. The number which is output is
* the inner product divided by the product of the length of the vectors.
*/
public class VectAngle implements Observer {

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

public VectAngle(String prefix)
{
	name = prefix;
	pid1 = Configuration.getPid(prefix+"."+PAR_PROT1);
	pid2 = Configuration.getPid(prefix+"."+PAR_PROT2);
}

//--------------------------------------------------------------------------

public boolean analyze() {
	
	System.out.print(name+": ");
	
	double sqrsum1=0,sqrsum2=0,prod=0;
	for(int i=0; i<Network.size(); ++i)
	{
		SingleValue sv1 =
			(SingleValue)Network.get(i).getProtocol(pid1),
			    sv2 =
			(SingleValue)Network.get(i).getProtocol(pid2);
		sqrsum1 += sv1.getValue()*sv1.getValue();
		sqrsum2 += sv2.getValue()*sv2.getValue();
		prod += sv2.getValue()*sv1.getValue();
	}

	System.out.println((prod/Math.sqrt(sqrsum1)/Math.sqrt(sqrsum2))+" "+
		Math.sqrt(sqrsum1)+" "+Math.sqrt(sqrsum2));

	return false;
}

}



