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
import java.util.*;

/**
 * This utility class can be used to obtain get/set methods from classes. In
 * particular, it is used in the vector package to locate get/set methods for
 * observing and modifying protocols fields.
 * <p>
 * A getter method is characterized as follows:
 * <ul>
 * <li> its return type is not void; </li>
 * <li> the argument list is empty.
 * </ul>
 * <p>
 * A setter method is characterized as follows:
 * <ul>
 * <li> its return type is void; </li>
 * <li> the argument list is composed by exactly one parameter.
 * </ul>
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
class Reflection
{

//--------------------------------------------------------------------------

/**
 * Search a getter method in the specified class.
 * 
 * @param clazz
 *          the class where to find get/set method
 * @param methodName
 *          the method to be searched
 * @return the requested method
 */
public static Method getMethodGet(Class clazz, String methodName)
		throws NoSuchMethodException
{
	// Search methods
	Method[] methods = clazz.getMethods();
	ArrayList<Method> list = new ArrayList<Method>();
	for (Method m: methods) {
		if (m.getName().equals(methodName)) {
			list.add(m);
		}
	}
	if (list.size() == 0) {
		throw new NoSuchMethodException("No setter method for method " + methodName
				+ " in class " + clazz.getName());
	} else if (list.size() > 1) {
		throw new NoSuchMethodException("Multiple setter for method " + methodName
				+ " in class " + clazz.getName());
	}
	// Found a single method with the right name; check if
	// it is a gettter.
	Method method = list.get(0);
	Class[] pars = method.getParameterTypes();
	if (pars.length > 0) {
		throw new NoSuchMethodException(method.getName() + "  of class "
				+ clazz.getName()
				+ "is not a valid getter method; its argument list is not empty");
	}
	Class ret = method.getReturnType();
	if (ret.equals(void.class)) {
		throw new NoSuchMethodException(method.getName() + "  of class "
				+ clazz.getName() + "is not a valid getter method: void " +
				"return type");
	}
	return method;
}

//--------------------------------------------------------------------------

/**
 * Search a setter method in the specified class.
 * 
 * @param clazz
 *          the class where to find get/set method
 * @param methodName
 *          the method to be searched
 * @return the requested method
 */
public static Method getMethodSet(Class clazz, String methodName)
		throws NoSuchMethodException
{
	// Search methods
	Method[] methods = clazz.getMethods();
	ArrayList<Method> list = new ArrayList<Method>();
	for (Method m: methods) {
		if (m.getName().equals(methodName)) {
			list.add(m);
		}
	}
	if (list.size() == 0) {
		throw new NoSuchMethodException("No setter method for method " + methodName
				+ " in class " + clazz.getName());
	} else if (list.size() > 1) {
		throw new NoSuchMethodException("Multiple setter for method " + methodName
				+ " in class " + clazz.getName());
	}
	// Found a single method with the right name; check if
	// it is a setter.
	Method method = list.get(0);
	Class[] pars = method.getParameterTypes();
	if (pars.length > 1) {
		throw new NoSuchMethodException(method.getName() + "  of class "
				+ clazz.getName()
				+ "is not a valid setter method; it takes more than one argument");
	}
	if (pars.length == 0) {
		throw new NoSuchMethodException(method.getName() + "  of class "
				+ clazz.getName()
				+ "is not a valid setter method; it takes no arguments");
	}
	Class ret = method.getReturnType();
	if (!ret.equals(void.class)) {
		throw new NoSuchMethodException(method.getName() + "  of class "
				+ clazz.getName() + "is not a valid setter method; it returns a value");
	}
	return method;
}

//--------------------------------------------------------------------------

//--------------------------------------------------------------------------


/**
 * Returns the field type for the specified getter.
 */
public static Class getTypeGet(Method m)
{
	return m.getReturnType();
}

//--------------------------------------------------------------------------

/**
 * Returns the field type for the specified setter.
 */
public static Class getTypeSet(Method m)
{
	Class[] pars = m.getParameterTypes();
	return pars[0];
}

//--------------------------------------------------------------------------

}
