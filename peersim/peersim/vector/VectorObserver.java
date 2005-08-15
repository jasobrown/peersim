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
 * This class computes and reports statistics information about one or more 
 * protocol vectors. Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats}.
 * <p>
 * This observer class can observe any protocol field containing a 
 * primitive value, provided that the field is associated with a getter method 
 * that reads it.
 * The methods to be used are specified through parameter {@value #PAR_METHODS}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#getValue()} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class VectorObserver implements Control 
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------


/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * The getter method(s) used to get values from the protocol instances.
 * Multiple methods can be specified, separated with spaces.
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_METHODS = "getter";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** The name of this observer in the configuration */
private final String prefix;

/** Protocol identifier */
private final int pid;

/** Methods name */
private final String[] methodNames;

/** Methods */
private final Method[] methods;


//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
protected VectorObserver(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	String value = Configuration.getString(prefix + "." + PAR_METHODS,
		"getValue");
	methodNames = value.split("\\s");
	
	// Search the methods
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	methods = new Method[methodNames.length];
	for (int i=0; i < methodNames.length; i++)
	{
		try
		{
			methods[i] = GetterSetterFinder.getGetterMethod(
			clazz, methodNames[i]);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalParameterException(prefix + "." +
			PAR_METHODS, e+"");
		}
	}
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public boolean execute()
{
	StringBuilder buffer = new StringBuilder();
	IncrementalStats stats = new IncrementalStats();

	for (int i=0; i < methods.length; i++) {
		stats.reset();
		/* Compute max, min, average */
		for (int j = 0; j < Network.size(); j++) {
			Object obj = Network.get(j).getProtocol(pid);
			try {
				Number v = (Number) methods[i].invoke(obj);
				stats.add( v.doubleValue() );
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
				System.exit(1);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		buffer.append(methodNames[i]);
		buffer.append(" ");
		buffer.append(stats);
		buffer.append(" ");
	}
	Log.println(prefix, buffer.toString());	

	return false;
}

//--------------------------------------------------------------------------

}
