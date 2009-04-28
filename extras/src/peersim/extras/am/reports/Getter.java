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

import java.lang.reflect.*;

import peersim.config.*;
import peersim.core.*;
import peersim.vector.*;

/**
 * Vectors can be written through this class. Typically {@link Control} classes
 * use this to manipulate vectors.
 * <p>
 * The method to be used is specified at construction time.
 * For backward compatibility, if no method is specified, the method
 * <code>getValue</code> is used. In this way, protocols
 * implementing the {@link SingleValue} interface can be manipulated using the
 * old configuration syntax (i.e., without specifying the method).
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class Getter {

// ============================ fields ===================================
// =======================================================================

/** Identifier of the protocol that defines the vector */
private int pid;

/** Getter method name */
private String methodName;

/** Getter method */
private Method method = null;

/** Parameter type of getter method */
private Class type;


// ========================== initialization =============================
// =======================================================================


/**
 * Constructs a Getter class based on the configuration. 
 * @param prefix the configuration prefix to use when reading the configuration
 * @param protocol the protocol we want to manipulate using a getter method.
 * @param methodn the getter method name.
 */
public Getter(String prefix, String protocol, String methodn) {

	// Read configuration parameter
	pid = Configuration.lookupPid(protocol);

	methodName = methodn;
	// Search the method
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	try {
		method = GetterSetterFinder.getGetterMethod(clazz, methodName);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." +
		methodn, e+"");
	}
	// Obtain the type of the field
	type = GetterSetterFinder.getGetterType(method);
}


// =============================== methods =============================
// =====================================================================

/**
* @return type of return value of getter method
*/
public Class getType() {

	return type;
}

// --------------------------------------------------------------------------

/**
* Gets the given value as a Number.
* @param n The node to get the value on. The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public Number get(Node n) {
	
	try 
	{
		Object ret =method.invoke(n.getProtocol(pid));
		if (ret instanceof Boolean)
			return ((Boolean) ret) ? 1 : 0;
		else
			return (Number) ret;
	}
	catch (Exception e)
	{
		throw new RuntimeException("While using getter "+methodName,e);
	}
}

// --------------------------------------------------------------------------

/**
* Gets the given integer value.
* @param n The node to get the value on. The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public long getLong(Node n) {
	
	if(type==long.class || type==int.class)
	{
		try 
		{
			return ((Number)
			method.invoke(n.getProtocol(pid))).longValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(
			"While using getter "+methodName,e);
		}
	}	
	else throw new RuntimeException("type has to be int or long");
}

// --------------------------------------------------------------------------

/**
* Gets the given real value.
* @param n The node to get the value on. The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public double getDouble(Node n) {
	
	if(type==double.class || type==float.class)
	{
		try
		{
			return ((Number)
			method.invoke(n.getProtocol(pid))).doubleValue();
		}
		catch (Exception e)
		{
			throw new RuntimeException(
			"While using getter "+methodName,e);
		}
	}
	else throw new RuntimeException(
			"type has to be double or float");
}

// --------------------------------------------------------------------------

/**
* Gets the given value as a Number.
* @param i The index of the node to get the value on in the network.
* The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public Number get(int i) { return get(Network.get(i)); }

// --------------------------------------------------------------------------

/**
* Gets the given integer value.
* @param i The index of the node to get the value on in the network.
* The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public long getLong(int i) { return getLong(Network.get(i)); }

// --------------------------------------------------------------------------

/**
* Gets the given real value.
* @param i The index of the node to get the value on in the network.
* The protocol is defined
* by {@link #pid}.
* @return the read value.
*/
public double getDouble(int i) { return getDouble(Network.get(i)); }

}

