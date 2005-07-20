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

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Serves as an abstract superclass for dynamics objects that deal
 * with vectors.
 * <p>
 * Such a dynamics class can initialize any protocol field containing a 
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
abstract class VectControl implements Control
{

// --------------------------------------------------------------------------
// Parameter names
// --------------------------------------------------------------------------

/**
 * The protocol to be initialized.
 * @config
 */
public static final String PAR_PROT = "protocol";

/**
 * The setter method used to set values in the protocol instances. Defauls to
 * "setValue" (for backward compatibility with previous implementation of this
 * class, that were based on the {@link SingleValue} interface. Refer to the
 * {@linkplain peersim.vector vector package description} for more
 * information about getters and setters.
 * @config
 */
public static final String PAR_METHOD = "setter";


// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Identifier of the protocol to be initialized */
protected final int pid;

/** Setter method name */
protected final String methodName;

/** Setter method */
protected final Method method;

/** Field type */
protected final Class type;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * @param prefix
 *          the configuration prefix for this class
 */
public VectControl(String prefix)
{
	// Read configuration parameter
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	methodName = Configuration.getString(prefix+"."+PAR_METHOD,"setValue");
	// Search the method
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	try {
		method = GetterSetterFinder.getSetterMethod(clazz, methodName);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." +
		PAR_METHOD, e.getMessage());
	}
	// Obtain the type of the field
	type = GetterSetterFinder.getSetterType(method);
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
* Sets the given integer value.
*/
protected void set(Node n, long val) {
	
	try 
	{
		if(type==long.class) method.invoke(n.getProtocol(pid),val);
		else if(type==int.class)
			method.invoke(n.getProtocol(pid),(int)val);
		else throw new Exception("type has to be int or long");
	
	}
	catch (Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}

// --------------------------------------------------------------------------

/**
* Sets the given real value.
*/
protected void set(Node n, double val) {
	
	try
	{
		if(type==double.class) method.invoke(n.getProtocol(pid),val);
		else if(type==float.class)
			method.invoke(n.getProtocol(pid),(float)val);
		else throw new Exception("type has to be double or float");
	
	}
	catch (Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}

// --------------------------------------------------------------------------

/**
* Sets the given integer value.
*/
protected void set(int i, long val) { set(Network.get(i),val); }

// --------------------------------------------------------------------------

/**
* Sets the given real value.
*/
protected void set(int i, double val) { set(Network.get(i),val); }

}

