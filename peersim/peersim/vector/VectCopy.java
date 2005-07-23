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
 * The methods to be used are specified through parameters 
 * {@value #PAR_GETTER} and {@value peersim.vector.VectControl#PAR_METHOD}.
 * For backward compatibility, if no methods are specified, the method
 * {@link SingleValue#getValue()} and {@link SingleValue#setValue(double)} 
 * are used, respectively. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class VectCopy extends VectControl implements  NodeInitializer
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The identifier of the protocol to be copied. The protocol given in 
 * parameter {@value peersim.vector.VectControl#PAR_PROT} will initialized copying values from this
 * protocol.
 * @config
 */
private static final String PAR_SOURCE = "source";

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

// --------------------------------------------------------------------------
// Variables
// --------------------------------------------------------------------------

/** Destination protocol id */
private final int gpid;

/** Getter method to be invoked on the source protocol */
private final Method getter;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public VectCopy(String prefix)
{
	super(prefix);
	
	gpid = Configuration.getPid(prefix + "." + PAR_SOURCE);
	String gmethod = Configuration.getString(prefix + "." + PAR_GETTER,
		"getValue");
	Class gclass = Network.prototype.getProtocol(gpid).getClass();
	try {
		getter = GetterSetterFinder.getGetterMethod(gclass, gmethod);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_GETTER,
			e.getMessage());
	}
}

//--------------------------------------------------------------------------
//Method
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public boolean execute() {
try {

	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Object obj = Network.get(i).getProtocol(gpid);
		Number ret = (Number)getter.invoke(obj);
		if(type==int.class || type==long.class) set(i,ret.longValue());
		else set(i,ret.doubleValue());
	}
}
catch(Exception e)
{
	e.printStackTrace();
	System.exit(1);
}

	return false;
}

//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void initialize(Node n) {
try {

	Object obj = n.getProtocol(gpid);
	Number ret = (Number)getter.invoke(obj);
	if(type==int.class || type==long.class) set(n,ret.longValue());
	else set(n,ret.doubleValue());
}
catch(Exception e)
{
	e.printStackTrace();
	System.exit(1);
}
}

//--------------------------------------------------------------------------

}
