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

/**
 * Initializes a protocol vector by copying the values of another 
 * protocol vector.
 * <p>
 * This dynamics class can copy any primitive field in the source
 * protocol to any primitive field in the destination protocol,
 * provided that the former field is associated with a getter method,
 * while the latter is associated with a setter method.
 * <p>
 * Getter methods are characterized as follows:
 * <ul>
 * <li> their return type is not void; </li>
 * <li> their argument list is empty.
 * </ul>
 * <p>
 * Setter methods are characterized as follows:
 * <ul>
 * <li> their return type is void; </li>
 * <li> their argument list is composed by exactly one parameter.
 * </ul>
 * <p>
 * The methods to be used are specified through parameters 
 * {@value #PAR_GETTER} and {@value #PAR_SETTER}.
 * For backward compatibility, if no methods are specified, the method
 * {@link SingleValue#getValue()} and {@link SingleValue#setValue(double)} 
 * are used, respectively. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class VectCopy implements Dynamics, NodeInitializer
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The identifier of the protocol to be modified. 
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The identifier of the protocol to be copied. The protocol given in 
 * parameter {@value #PAR_PROT} will initialized based on this value.
 * @config
 */
private static final String PAR_CLONE = "copy";

/**
 * The getter method used to obtain the protocol values. 
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_GETTER = "getter";

/**
 * The setter method used to set values in the protocol instances.  
 * Defauls to "setValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_SETTER = "setter";

// --------------------------------------------------------------------------
// Variables
// --------------------------------------------------------------------------

/** Source protocol id */
private int spid;

/** Destination protocol id */
private int dpid;

/** Getter method to be invoked on the source protocol */
private Method sm;

/** Setter method to be invoked on the destination protocol */
private Method dm;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * @param prefix
 *          the configuration prefix for this class
 */
public VectCopy(String prefix)
{
	// Read parameters
	spid = Configuration.getPid(prefix + "." + PAR_CLONE);
	dpid = Configuration.getPid(prefix + "." + PAR_PROT);
	String smethod = Configuration.getString(prefix + "." + PAR_GETTER, "value");
	String dmethod = Configuration.getString(prefix + "." + PAR_SETTER, "value");
	// Search methods
	Class sclass = Network.prototype.getProtocol(spid).getClass();
	Class dclass = Network.prototype.getProtocol(dpid).getClass();
	try {
		sm = GetterSetterFinder.getGetterMethod(sclass, smethod);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_GETTER, e
				.getMessage());
	}
	try {
		dm = GetterSetterFinder.getSetterMethod(dclass, dmethod);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_SETTER, e
				.getMessage());
	}
}

//--------------------------------------------------------------------------
//Method
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void modify()
{
	int size = Network.size();
	try {
		for (int i = 0; i < size; i++) {
			Object sobj = Network.get(i).getProtocol(spid);
			Object dobj = Network.get(i).getProtocol(dpid);
			Object ret = sm.invoke(sobj);
			dm.invoke(dobj, ret);
		}
	} catch (InvocationTargetException e) {
		e.getTargetException().printStackTrace();
		System.exit(1);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void initialize(Node n)
{
	try {
		Object sobj = n.getProtocol(spid);
		Object dobj = n.getProtocol(dpid);
		Object ret = sm.invoke(sobj);
		dm.invoke(dobj, ret);
	} catch (InvocationTargetException e) {
		// Should never happen
		e.printStackTrace();
		System.exit(1);
	} catch (IllegalAccessException e) {
		// Should never happen
		e.printStackTrace();
		System.exit(1);
	}
}

//--------------------------------------------------------------------------

}
