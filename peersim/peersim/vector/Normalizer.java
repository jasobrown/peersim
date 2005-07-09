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
import peersim.core.*;
import peersim.dynamics.Dynamics;
import peersim.config.*;
import peersim.config.Configuration;

/**
 * Normalizes the values of a protocol vector. 
 * <p>
 * This dynamics class can normalie any protocol field containing a 
 * primitive value, provided that the field is associated with a getter method 
 * that reads it and setter method that modifies it.
 * The methods to be used are specified through parameters 
 * {@value #PAR_GETTER} and {@value #PAR_SETTER}.
 * Only getter/setter for float/double fields are valid.
 * <p>
 * For backward compatibility, if no methods are specified, the method
 * {@link SingleValue#getValue()} and {@link SingleValue#setValue(double)} 
 * are used, respectively. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class Normalizer extends VectDynamics
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * The L1 norm (sum of absolute values) to normalize to. After the operation the
 * L1 norm will be the value given here. Defaults to 1.
 * @config
 */
public static final String PAR_L1 = "l1";

/**
 * The getter method used to obtain the protocol values. 
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
public static final String PAR_GETTER = "getter";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** L1 norm */
private final double l1;

/** Getter method to be invoked on the source protocol */
private final Method getter;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * @param prefix
 *          the configuration prefix for this class
 */
public Normalizer(String prefix)
{
	super(prefix);
	l1 = Configuration.getDouble(prefix + "." + PAR_L1, 1);
	// Read parameters
	String getterMethod = Configuration.getString(prefix + "." + PAR_GETTER,
			"getValue");
	// Search methods
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	try {
		getter = GetterSetterFinder.getGetterMethod(clazz,getterMethod);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_GETTER, e
				.getMessage());
	}
	
	if( !(type==double.class || type==float.class) )
		throw new IllegalParameterException(prefix + "." + PAR_METHOD,
			"type of value must be floating point, instead of "+
			type);
			
	if( type !=  GetterSetterFinder.getGetterType(getter) )
		throw new IllegalParameterException(prefix + "." + PAR_GETTER,
			"getter and setter must have the same numeric type, "+
			"but we have "+type+" and "+
			GetterSetterFinder.getGetterType(getter));
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/**
 * Makes the sum of the absolute values (L1 norm) equal to the value given in
 * the configuration parameter {@value #PAR_L1}.
 */
public void modify() {
try {
	double sum = 0.0;
	for (int i = 0; i < Network.size(); ++i)
	{
		Number n=(Number)getter.invoke(Network.get(i).getProtocol(pid));
		sum += Math.abs(n.doubleValue());
	}
	if (sum == 0.0)
	{
		throw new
		RuntimeException("Attempted to normalize all zero vector.");
	}
	double factor = l1 / sum;
	for (int i = 0; i < Network.size(); ++i)
	{
		Number n=(Number)getter.invoke(Network.get(i).getProtocol(pid));
		double val = n.doubleValue()*factor;
		set(i,val);
	}
} catch (InvocationTargetException e) {
	e.getTargetException().printStackTrace();
	System.exit(1);
} catch (Exception e) {
	throw new RuntimeException(e);
}
}

//--------------------------------------------------------------------------

}
