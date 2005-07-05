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
import peersim.reports.*;
import peersim.util.*;

/**
 * Observes difference between two vectors. It computes the distance vector
 * abs(x-y), and reports statistics on this vector such as average, minimum and
 * maximum (according to the string format of {@link IncrementalStats}).
 * <p>
 * This observer class can observe any protocol field containing a 
 * primitive value, provided that the field is associated with a getter method 
 * that reads it.
 * Getter methods are characterized as follows:
 * <ul>
 * <li> their return type is not void; </li>
 * <li> their argument list is empty.
 * </ul>
 * <p>
 * The methods to be used are specified through parameter {@value #PAR_METHOD1}
 * and {@value #PAR_METHOD2}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#getValue()} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class VectDistance implements Observer
{

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The first protocol to be observed.
 * @config
 */
private static final String PAR_PROT1 = "protocol1";

/**
 * The second protocol to be observed.
 * @config
 */
private static final String PAR_PROT2 = "protocol2";

/**
 * The getter method used to obtain the values of the first protocol. 
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_METHOD1 = "method1";

/**
 * The getter method used to obtain the values of the second protocol. 
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_METHOD2 = "method2";


//--------------------------------------------------------------------------
//Fields 
//--------------------------------------------------------------------------

/** The prefix for this observer*/
private final String name;

/** Identifier of the first protocol */
private final int pid1;

/** Identifier of the second protocol */
private final int pid2;

/** Method name, first protocol */
private final String methodName1;

/** Method name, second protocol */
private final String methodName2;

/** Method, first protocol */
private final Method method1;

/** Method, second protocol */
private final Method method2;

// --------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Reads parameters and search for getter/setter methods
 */
public VectDistance(String prefix)
{
	name = prefix;
	pid1 = Configuration.getPid(prefix + "." + PAR_PROT1);
	pid2 = Configuration.getPid(prefix + "." + PAR_PROT2);

	methodName1 = Configuration.getString(prefix + "." + PAR_METHOD1, 
	"getValue");
	methodName2 = Configuration.getString(prefix + "." + PAR_METHOD2, 
	"getValue");
	
	// Search the methods
	Class class1 = Network.prototype.getProtocol(pid1).getClass();
	Class class2 = Network.prototype.getProtocol(pid2).getClass();
	try {
		method1 = GetterSetterFinder.getGetterMethod(class1, methodName1);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_METHOD1, 
				e.getMessage());
	}
	try {
		method2 = GetterSetterFinder.getGetterMethod(class1, methodName2);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." + PAR_METHOD1, 
				e.getMessage());
	}
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public boolean analyze()
{
	IncrementalStats is = new IncrementalStats();
	try {
		for (int i = 0; i < Network.size(); ++i) {
			Object obj1 = Network.get(i).getProtocol(pid1);
			Object obj2 = Network.get(i).getProtocol(pid2);
			double v1 = ((Number) method1.invoke(obj1)).doubleValue();
			double v2 = ((Number) method2.invoke(obj2)).doubleValue();
			is.add(Math.abs(v1-v2));
		}
	} catch (InvocationTargetException e) {
		e.getTargetException().printStackTrace();
		System.exit(1);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
	Log.println(name, is.toString());
	return false;
}

//--------------------------------------------------------------------------

}
