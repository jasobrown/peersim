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
		
package peersim.vector;

import java.lang.reflect.*;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
 * This class computes and reports statistics information about a vector.
 * Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats#toString}. This observer is capable to terminate
 * the simulation based on a configurable condition, expressed by the
 * parameters {@value #PAR_STATISTICS} and {@value #PAR_ENDVALUE}.
 * The method specified by {@value #PAR_STATISTICS} is invoked, and
 * if the return value is equal to {@value #PAR_ENDVALUE}, the
 * simulation is interrupted. 
 * 
 * @see VectControl
 * @see peersim.vector
 */
public class VectorObserver extends VectControl {

/**
 * This parameter is used to select the method to be
 * invoked on IncrementalStats to evaluate if the simulation
 * must be terminated or not. Defaults to {@link IncrementalStats#getAverage()}
 * if parameter {@value #PAR_ENDVALUE} is defined.
 */
private static final String PAR_STATISTICS = "statistics";

/** 
 * When the statistics selected by {@value #PAR_STATISTICS}
 * is equal to this parameter, the control returns
 * true and stops the simulation.
 */
private static final String PAR_ENDVALUE = "endvalue";


/** The name of this observer in the configuration */
private final String prefix;

/** End value */
private double endvalue;

/** The method to be used to compare with endvalue */
private Method method = null;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public VectorObserver(String prefix) {

	super(prefix);
	this.prefix = prefix;
	if (Configuration.contains(prefix+"."+PAR_ENDVALUE)) { 
		endvalue = Configuration.getDouble(prefix+"."+PAR_ENDVALUE);
	  String methodName = Configuration.getString(prefix+"." + 
	  		PAR_STATISTICS, "getAverage");
		// Search the method
		Class clazz = IncrementalStats.class;
		try {
			method = GetterSetterFinder.getGetterMethod(clazz, methodName);
		} catch (NoSuchMethodException e) {
			throw new IllegalParameterException(prefix + "." +
			PAR_STATISTICS, e+"");
		}
	}
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Prints statistics information about a vector.
 * Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats#toString}.
 * @return always false
 */
public boolean execute() {

	IncrementalStats stats = new IncrementalStats();

	for (int j = 0; j < Network.size(); j++)
	{
		Number v = getter.get(j);
		stats.add( v.doubleValue() );
	}
	System.out.println(prefix+": "+stats);
	if (method == null)
		return false;
	else {
		try {
			double v = ((Number) method.invoke(stats)).doubleValue();
			return (v == endvalue);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}
}

}
