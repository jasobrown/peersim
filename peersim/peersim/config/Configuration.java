/*
 * Copyright (c) 2003 The BISON Project
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
		
package peersim.config;

import java.lang.reflect.*;
import java.util.*;

/**
* Fully static class to store configuration information.
* Contains methods to set configuration data and utility methods to read
* items based on their names. Its other purpose is to hide the actual
* implementation of the configuration which can be Properties, XML, whatever.
* <p>
* Numeric configuration items can complex expressions, that are parsed
* through Java Expression Parser (http://www.singularsys.com/jep/).
* <p>
* Protocol names are associated to numeric protocol identifiers
* through method <t>getPid()</t>.
*/
public class Configuration {


// =================== static fields =================================
// ===================================================================

/**
 * The parameter name to configure a maximum depth different from
 * default.
 */
private static final String PAR_MAXDEPTH = "expressions.maxdepth"; 
	
	
/**
* The properties object that stores all configuration information.
*/
private static Properties config = null;


/**
 *  Map associating string protocol names to the numeric protocol
 * identifiers.
 */
private static Map protocols = new HashMap();


/**
 *  The maximum depth that can be reached when analyzing expressions.
 *  This value can be substituted by setting the configuration parameter
 *  PAR_MAXDEPTH.
 */
private static int maxdepth = 100;

// =================== static public methods =========================
// ===================================================================


/** 
* Sets the system-wide configuration in Properties format.
* @param p The Properties object containing coniguration info
* @return The Properties object that was set previously
*/
public static Properties setConfig( Properties p ) {

	Properties prev = config;
	config = p;
	maxdepth = Configuration.getInt(PAR_MAXDEPTH, 100);
	return prev;
}

// -------------------------------------------------------------------

/**
* @return true if and only if the specified name is assigned a value in the
* configuration
*/
public static boolean contains(String name) {
	
	return config.containsKey(name);
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
* @param def default value
*/
public static int getInt( String name, int def ) {

	try
	{
		return Configuration.getInt(name);
	}
	catch( Exception e )
	{
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws an 
* IllegalArgumentException.
* @param name Name of configuration property
*/
public static int getInt( String name ) 
{
	return (int) Math.round(getVal(name, name, 0));
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
* @param def default value
*/
public static long getLong( String name, long def ) {

	try
	{
		return Configuration.getLong(name);
	}
	catch( Exception e )
	{
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws an 
* IllegalArgumentException.
* @param name Name of configuration property
*/
public static long getLong( String name ) 
{
	return Math.round(getVal(name, name, 0));
}


// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
* @param def default value
*/
public static double getDouble( String name, double def ) {

	try
	{
		return Configuration.getDouble(name);
	}
	catch( Exception e )
	{
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws an 
* IllegalArgumentException.
* @param name Name of configuration property
*/
public static double getDouble( String name ) 
{
	return getVal(name, name, 0);
}

//-------------------------------------------------------------------

/**
 * Read numeric property values, parsing expression if necessary.
 * 
 * @param initial the property name that started this expression evaluation
 * @param property the current property name to be evaluated
 * @param depth the depth reached so far
 * @return the evaluation of the expression associated to property  
 */
public static double getVal(String initial, String property, int depth)
{
	if (depth > maxdepth) {
		throw new IllegalParameterException(initial, 
				"Probable recursive definition - exceeded maximum depth " + 
				maxdepth);
	}
	
	String s = config.getProperty(property);
	if (s == null || s.equals(""))
		throw new MissingParameterException(property, 
				" when evaluating property " + initial);
	
	org.nfunk.jep.JEP jep = new org.nfunk.jep.JEP();
	jep.setAllowUndeclared(true);
	
	jep.parseExpression(s);
	String[] symbols = getSymbols(jep);
	for (int i=0; i < symbols.length; i++) {
		double d = getVal(initial, symbols[i], depth+1);
		jep.addVariable(symbols[i], d);
	}
	
	return jep.getValue();
}

/**
 * Returns an array of string, containing the symbols contained
 * in the expression parsed by the specified JEP parser.
 * @param jep the java expression parser containing the list
 *   of variables
 * @return an array of strings.
 */
private static String[] getSymbols(org.nfunk.jep.JEP jep)
{
	Hashtable h = jep.getSymbolTable();
  String[] ret = new String[h.size()];
	Enumeration e = h.keys();
	int i = 0;
	while (e.hasMoreElements()) {
		ret[i++] = (String) e.nextElement();
	}
  return ret;
  
}


// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
* @param def default value
*/
public static String getString( String name, String def ) {

	try
	{
		return Configuration.getString(name);
	}
	catch( Exception e )
	{
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads the configuration item associated to the specified property.
* @param property Name of configuration property
*/
public static String getString( String property ) {

	String result = config.getProperty(property);
	if( result == null ) throw new MissingParameterException(property);
	
	return result;
}

//-------------------------------------------------------------------

/**
 * Reads the given string property and returns the associated numeric 
 * protocol identifier.
 *  
 * @param property the property name
 * @return the numeric protocol identifier associated to the protocol
 *   name
 */
public static int getPid( String property ) {
	
	String protname = getString(property);
	Integer ret = (Integer) protocols.get(protname); 
//	System.out.println("- " + property);
//	System.out.println("+ " + protname);
//	System.out.println(((Integer) protocols.get(protname)).intValue());
	if (ret == null) {
		throw new IllegalParameterException(property, "Protocol " + protname +
				" does not exist");
	}
	return ret.intValue();
}

//-------------------------------------------------------------------

/**
 * Associates the given string protocol name to the given 
 * numeric protocol identifier. "Normal" users of peersim do
 * not need this method; it is normally invoked by the class
 * that first initializes the protocol stack; in the default
 * peersim settings, this class is {@link peersim.core.GeneralNode}.
 * Refers to the implementation of that class to see an example
 * of use of such methods.
 * 
 * @param protname the string protocol name
 * @param pid the protocol identifier associated to the name
 */
public static void setPid( String protname, int pid)
{
//	System.out.println("- " + protname);
//	System.out.println("+ " + pid);
	protocols.put(protname, new Integer(pid));
}

// -------------------------------------------------------------------

/**
 * Returns the class object for the specified classname. If the specified 
 * class does not
 * exist, few attempts are done to identify the correct class, or
 * at least provide some suggestions.
 * 
 */
private static Class getClass(String property, String classname)
{
	Class c = null;
	try {
		// Maybe classname is just a fully-qualified name
		c = Class.forName(classname);
	} catch ( ClassNotFoundException e) { }
	if (c == null) {
		// Maybe classname is a non-qualified name?
		String fullname = ClassFinder.getQualifiedName(classname);
		if (fullname != null) {
			try {
				c = Class.forName(fullname);
			} catch ( ClassNotFoundException e) { }
		}
	}
	if (c == null) {
		// Maybe there are multiple classes with the same non-qualified name.
		String fullname = ClassFinder.getQualifiedName(classname);
		if (fullname != null && fullname.indexOf(',') >= 0) {
			throw new IllegalParameterException(property,
				"The non-qualified class name " + classname + 
				" corresponds to multiple fully-qualified classes: " +
				fullname);
		}
	}
	if (c == null) {
		// Last attempt: maybe the fully classified name is wrong, but the
		// classname is correct. 
		String shortname = ClassFinder.getShortName(classname);
		String fullname = ClassFinder.getQualifiedName(shortname);
		if (fullname != null) {
			throw new IllegalParameterException(property,
				"Class " + classname + 
				" does not exists. Possible candidate(s): " +	fullname);
		}		
	}
	if (c == null) {
		throw new IllegalParameterException(property,
				"Class " + classname + " not found");
	}
	return c;
}	

//-------------------------------------------------------------------

/**
* Reads given configuration item for a class name. It returns an instance of
* the class. The class must implement a constructor that takes a String as an
* argument. The value of this string will be <tt>name</tt>. Note that this
* constructor can see the configuration information so it can make use of it.
* @param name Name of configuration property
* @throws RuntimeException if there's any problem with creating the object.
*/
public static Object getInstance( String name ) {

	String classname = config.getProperty(name);
	if (classname == null) throw new MissingParameterException(name);

  Class c = getClass(name, classname);		
		
	try {
		Class pars[] = { String.class };
		Constructor cons = c.getConstructor( pars );
		Object objpars[] = { name };
		return cons.newInstance( objpars );
	}
	catch( NoSuchMethodException e )
	{
		throw new IllegalParameterException(name,
			"Class " + classname + " has no " + classname +
			("(String) or " + classname + 
			"(String, Object) constructors"));
	}
	catch ( InvocationTargetException e) 
	{
		if (e.getTargetException() instanceof RuntimeException) {
			throw (RuntimeException) e.getTargetException();
		} else {
			throw new RuntimeException(""+e.getTargetException());
		}
	}
	catch( Exception e )
	{
		throw new IllegalParameterException(name, e.getMessage());
	} 
}

// -------------------------------------------------------------------

/**
* Reads given configuration item for a class name. It returns an instance of
* the class. If the class implements a constructor which takes the same
* parameters as this method, it will be used. Otherwise the constructor
* taking a String will be used, and parameter <tt>name</tt> will be passed to
* it. Note that these
* cosntructors can see the configuration information so it can make use of it.
* @param name Name of configuration property
* @param obj the second parameter to pass to the constructor
* @throws RuntimeException if there's any problem with creating the object.
*/
public static Object getInstance( String name, Object obj ) {
// XXX if necessary, API will have to be provided to force the (String,Object)
// constructor and not using the default (string) constructor

	String classname = config.getProperty(name);
	if (classname == null) throw new MissingParameterException(name);
	
  Class c = getClass(name, classname);		

  try
	{
		Class pars[] = { String.class, Object.class };
		Constructor cons = c.getConstructor( pars );
		Object objpars[] = { name, obj };
		return cons.newInstance( objpars );
	}
	catch( NoSuchMethodException e )
	{
		return getInstance( name );
	}
	catch ( InvocationTargetException e) 
	{
		if (e.getTargetException() instanceof RuntimeException) {
			throw (RuntimeException) e.getTargetException();
		} else {
			e.printStackTrace();
			throw new RuntimeException(""+e.getTargetException());
		}
	}
	catch( Exception e )
	{
		throw new IllegalParameterException(name, e.getMessage());
	} 
}

//-------------------------------------------------------------------

/**
* It returns an array of class instances defined by property names
* returned by {@link #getNames(String)}.
* The classes must implement a constructor which
* takes one String parameter, which will be the full property name of the class
* in the configuration.
* Note that constructors can see the configuration information so they can
* make use of it. The class names are defined by the property names returned
* by {@link #getNames(String)}.
* @param name Prefix of the list of configuration properties which will be
* passed to {@link #getNames(String)}.
* @throws RuntimeException if there's any problem with creating the objects.
*/
public static Object[] getInstanceArray( String name ) {

	String names[] = getNames(name);
	Object[] result = new Object[names.length];
	
	for( int i=0; i<names.length; ++i )
	{
		result[i]=getInstance(names[i]);
	}

	return result;
}

//-------------------------------------------------------------------

/**
 * Returns an array of names starting with the specified name.
 * The returned array is sorted alphabetically. 
 * {@link #getInstanceArray} will use this method to create instances.
 * In other words, calling
 * {@link #getInstance(String)} with these names results in the
 * same instances {@link #getInstanceArray} returns.
 */
public static String[] getNames( String name ) 
{
	ArrayList ll = new ArrayList();
	final String pref = name+".";

	Enumeration e = config.propertyNames();
	while (e.hasMoreElements()) {
		String key = (String) e.nextElement();
		if (key.startsWith(pref) && key.indexOf(".", pref.length())<0)
			ll.add(key);
	}
	String[] ret = (String[])ll.toArray(new String[ll.size()]);
	Arrays.sort(ret);
	return ret;
}

}

