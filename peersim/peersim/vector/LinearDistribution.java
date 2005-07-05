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
 * Initializes a protocol vector with values in the range [{@value #PAR_MIN}, 
 * {@value #PAR_MAX}], linearly increasing.
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
public class LinearDistribution implements Dynamics, NodeInitializer
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The upper bound of the uniform random variable.
 * @config
 */
private static final String PAR_MAX = "max";

/**
 * The lower bound of the uniform random variable. Defaults to -max.
 * @config
 */
private static final String PAR_MIN = "min";

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

/** Minimum value */
private final Number min;

/** Maximum value */
private final Number max;

/** Protocol identifier */
private final int pid;

/** Last value assigned to a node */
private Number last;

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
public LinearDistribution(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	// The default value is selected for backward compatibility
	methodName = Configuration.getString(prefix + "." + PAR_METHOD, "setValue");
	
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
		max = new Integer(Configuration.getInt(prefix + "." + PAR_MAX));
		min = new Integer(Configuration.getInt(prefix + "." + PAR_MIN, 
				-max.intValue()));
	} else if (type.equals(long.class)) {
		max = new Long(Configuration.getLong(prefix + "." + PAR_MAX));
		min = new Long(Configuration.getLong(prefix + "." + PAR_MIN, 
				-max.longValue()));
	} else if (type.equals(float.class)) {
		max = new Float(Configuration.getDouble(prefix + "." + PAR_MAX));
		min = new Float(Configuration.getDouble(prefix + "." + PAR_MIN, 
				-max.floatValue()));
	} else if (type.equals(double.class)) {
		max = new Double(Configuration.getDouble(prefix + "." + PAR_MAX));
		min = new Double(Configuration.getDouble(prefix + "." + PAR_MIN, 
				-max.doubleValue()));
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
	try {
		if (type.equals(int.class)) {
			int start = min.intValue();
			int step = (max.intValue()-start)/(Network.size()-1);
			int lastval = 0;
			for(int i=0; i<Network.size(); ++i) {
				lastval = i*step+start;
				method.invoke(Network.get(i).getProtocol(pid), lastval);
			}
			last = new Integer(lastval);
		} else if (type.equals(long.class)) {
			long start = min.longValue();
			long step = (max.longValue()-start)/(Network.size()-1);
			long lastval = 0;
			for(int i=0; i<Network.size(); ++i) {
				lastval = i*step+start;
				method.invoke(Network.get(i).getProtocol(pid), lastval);
			}
			last = new Long(lastval);
		} else if (type.equals(float.class)) {
			float start = min.floatValue();
			float step = (max.floatValue()-start)/(Network.size()-1);
			float lastval = 0;
			for(int i=0; i<Network.size(); ++i) {
				lastval = i*step+start;
				method.invoke(Network.get(i).getProtocol(pid), lastval);
			}
			last = new Float(lastval);
		} else if (type.equals(double.class)) {
			double start = min.doubleValue();
			double step = (max.doubleValue()-start)/(Network.size()-1);
			double lastval = 0;
			for(int i=0; i<Network.size(); ++i) {
				lastval = i*step+start;
				method.invoke(Network.get(i).getProtocol(pid), lastval);
			}
			last = new Double(lastval);
		}
	} catch (InvocationTargetException e) {
		e.getTargetException().printStackTrace();
		System.exit(1);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}

// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void initialize(Node n)
{
	try {
		if (type.equals(int.class)) {
			int lastval = (max.intValue()-min.intValue())/(Network.size()-1);
			if (lastval > max.intValue())
				lastval = min.intValue();
			last = new Integer(lastval);
			method.invoke(n.getProtocol(pid), last);
		} else if (type.equals(long.class)) {
			long lastval = (max.longValue()-min.longValue())/(Network.size()-1);
			if (lastval > max.longValue())
				lastval = min.longValue();
			last = new Long(lastval);
			method.invoke(n.getProtocol(pid), last);
		} else if (type.equals(float.class)) {
			float lastval = (max.floatValue()-min.floatValue())/(Network.size()-1);
			if (lastval > max.floatValue())
				lastval = min.floatValue();
			last = new Float(lastval);
			method.invoke(n.getProtocol(pid), last);
		} else if (type.equals(double.class)) {
			double lastval = (max.doubleValue()-min.doubleValue())/(Network.size()-1);
			if (lastval > max.doubleValue())
				lastval = min.doubleValue();
			last = new Double(lastval);
			method.invoke(n.getProtocol(pid), last);
		}
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

// --------------------------------------------------------------------------

}








