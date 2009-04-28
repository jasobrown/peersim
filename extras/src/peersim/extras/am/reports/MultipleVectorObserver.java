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

package peersim.extras.am.reports;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.vector.*;

/**
 * Serves as an abstract superclass for {@link Control}s that deal
 * with vectors.
 */
public class MultipleVectorObserver implements Control {


// --------------------------------------------------------------------------
// Parameter names
// --------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
protected static final String PAR_PROT = "protocol";

/**
 * The getter method used to obtain the protocol values. 
 * Defaults to <code>getValue</code>
 * (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface).
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
protected static final String PAR_GETTER = "getter";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** The getter to be used to read a vector. */
protected final Getter[] getters;

protected final String prefix;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public MultipleVectorObserver(String prefix)
{
	this.prefix = prefix;
	String par_getter = Configuration.getString(prefix + "." + PAR_GETTER);
	String par_prot   = Configuration.getString(prefix + "." + PAR_PROT);
	String[] names = par_getter.split(",");
	String[] protocols = par_prot.split(",");
	
	if (names.length != protocols.length) {
		throw new IllegalParameterException(prefix+"."+PAR_GETTER, 
				"The number of protocols and getters is different");
	}
	
	getters = new Getter[names.length];
	for (int i=0; i < getters.length; i++) {
		getters[i] = new Getter(prefix,protocols[i],names[i]);
	}
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/**
* Prints statistics information about a vector.
* Provided statistics include average, max, min, variance,
* etc. Values are printed according to the string format of {@link 
* IncrementalStats#toString}.
* @return always false
*/
public boolean execute() {

IncrementalStats[] stats = new IncrementalStats[getters.length];

for (int i=0; i < stats.length; i++) {
	stats[i] = new IncrementalStats();
}

for (int j = 0; j < Network.size(); j++)
{
	for (int i=0; i < getters.length; i++) { 
		Number v = getters[i].get(j);
		stats[i].add( v.doubleValue() );
	}
}

System.out.print(prefix+":");
for (int i=0; i < stats.length; i++) {
	System.out.print(" " + stats[i]);
}
System.out.println();

return false;
}

}

