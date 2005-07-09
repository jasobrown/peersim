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
import peersim.dynamics.*;
import peersim.util.*;

/**
 * Initializes the values drawing uniform random samples from the range
 * [{@value #PAR_MIN}, {@value #PAR_MAX}].
 * <p>
 * This dynamics class can initialize any protocol field containing a 
 * primitive value, provided that the field is associated with a setter method 
 * that modifies it.
 * The method to be used is specified through parameter {@value #PAR_METHOD}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#setValue(double)} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class UniformDistribution extends VectDynamics implements NodeInitializer
{

//--------------------------------------------------------------------------
//Parameter names
//--------------------------------------------------------------------------

/**
 * The upper bound of the uniform random variable.
 * @config
 */
public static final String PAR_MAX = "max";

/**
 * The lower bound of the uniform
 * random variable. Defaults to -{@value #PAR_MAX}.
 * @config
 */
public static final String PAR_MIN = "min";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Minimum value */
private final Number min;

/** Maximum value */
private final Number max;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public UniformDistribution(String prefix)
{
	super(prefix);
	
	// Read parameters based on type
	if (type==long.class || type==int.class) {
		max = new Long(Configuration.getLong(prefix + "." + PAR_MAX));
		min = new Long(Configuration.getLong(prefix + "." + PAR_MIN, 
				-max.longValue()));
	} else { // we know it's double or float
		max = new Double(Configuration.getDouble(prefix+"."+PAR_MAX));
		min = new Double(Configuration.getDouble(prefix+"."+PAR_MIN, 
				-max.doubleValue()));
	}
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void modify() {

	if( type==long.class || type==int.class)
	{
		long d = max.longValue() - min.longValue();
		for (int i = 0; i < Network.size(); ++i)
		{
			set(i,CommonRandom.nextLong(d) + min.longValue());
		}
	}
	else
	{
		double d = max.doubleValue() - min.doubleValue();
		for (int i = 0; i < Network.size(); ++i)
		{
			set(i,CommonRandom.r.nextDouble()*d);
		}
	}
}

// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void initialize(Node n) {

	if( type==long.class || type==int.class)
	{
		long d = max.longValue() - min.longValue();
		set(n,CommonRandom.nextLong(d) + min.longValue());
	}
	else
	{
		double d = max.doubleValue() - min.doubleValue();
		set(n,CommonRandom.r.nextDouble()*d);
	}
}

// --------------------------------------------------------------------------

}
