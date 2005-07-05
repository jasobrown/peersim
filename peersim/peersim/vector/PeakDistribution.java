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
 * Initializes the values so that {@link #PAR_PEAKS} nodes have value
 * {@link #PAR_VALUE}/{@link #PAR_PEAKS}, the rest zero.
 * <p>
 * This dynamics class can initialize any protocol field containing a 
 * primitive value, provided that the field is associated with a setter method 
 * that modifies it.
 * Setter methods are characterized as follows:
 * <ul>
 * <li> their return type is void; </li>
 * <li> their argument list is composed by exactly one parameter.
 * </ul>
 * <p>
 * The method to be used is specified through parameter {@value #PAR_METHOD}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#setValue(double)} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class PeakDistribution implements Dynamics
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/** 
 * The sum of values in the system, to be equally distributed between peak 
 * nodes.
 * @config
 */
private static final String PAR_VALUE = "value";


/** 
 * The number of peaks in the system. If this value is greater or equal than 
 * 1, it is interpreted as the actual number of peaks. If it is included in 
 * the range [0, 1[ it is interpreted as a percentage with respect to the
 * current network size. Defaults to 1. 
 * @config
 */
private static final String PAR_PEAKS = "peaks";


/** 
 * The protocol to be initialized.
 * @config
 */
private static final String PAR_PROTOCOL = "protocol";

/**
 * The setter method used to set values in the protocol instances.  
 * Defauls to "setValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_METHOD = "method";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Total load */
private final Number value;

/** Number of peaks */
private final double peaks;

/** Protocol identifier */
private final int pid;

/** Setter method name */
private final String methodName;

/** Setter method */
private final Method method;

/** Field type */
private Class type;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * @param prefix the configuration prefix for this class
 */
public PeakDistribution(String prefix)
{
	peaks = Configuration.getDouble(prefix+"."+PAR_PEAKS, 1);
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	// The default value is selected for backward compatibility
	methodName = Configuration.getString(prefix + "." + PAR_METHOD, "getValue");
	
	// Search the method
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	try {
		method = GetterSetterFinder.getSetterMethod(clazz, methodName);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_METHOD, 
				e.getMessage());
	}
	
	// Obtain the type of the field
	type = GetterSetterFinder.getSetterType(method);
	
	// Read parameters based on type
	if (type.equals(int.class)) {
		value = new Integer(Configuration.getInt(prefix + "." + PAR_VALUE));
	} else if (type.equals(long.class)) {
		value = new Long(Configuration.getLong(prefix + "." + PAR_VALUE));
	} else if (type.equals(float.class)) {
		value = new Float(Configuration.getDouble(prefix + "." + PAR_VALUE));
	} else if (type.equals(double.class)) {
		value = new Double(Configuration.getDouble(prefix + "." + PAR_VALUE));
	} else {
		throw new IllegalParameterException(prefix + "." + PAR_METHOD, 
				method.getName() + " of class " + clazz.getName() 
				+ "is not a supported setter");
	}
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void modify()
{
	int pn = (peaks < 1 ? (int) (peaks*Network.size()) : (int) peaks);
	
	try {
		if (type.equals(int.class)) {
			int v = value.intValue()/pn;
			for (int i=0; i < pn; i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, v);
			}
			for (int i=pn; i < Network.size(); i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, 0.0);
			}
		} else if (type.equals(long.class)) {
			long v = value.longValue()/pn;
			for (int i=0; i < pn; i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, v);
			}
			for (int i=pn; i < Network.size(); i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, 0.0);
			}
		} else if (type.equals(float.class)) {
			float v = value.floatValue()/pn;
			for (int i=0; i < pn; i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, v);
			}
			for (int i=pn; i < Network.size(); i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, 0.0);
			}
		} else if (type.equals(double.class)) {
			double v = value.doubleValue()/pn;
			for (int i=0; i < pn; i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, v);
			}
			for (int i=pn; i < Network.size(); i++) {
				Object obj = Network.get(i).getProtocol(pid);
				method.invoke(obj, 0.0);
			}
		}
	} catch (InvocationTargetException e) {
		e.getTargetException().printStackTrace();
		System.exit(1);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

// --------------------------------------------------------------------------

}
