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
* The configuration is generally blind to the semantics of the entries.
* there is an exception, the entries that start with "protocol". These
* entries are pre-processed a bit to enhance performance:
* protocol names are associated to numeric protocol identifiers
* through method {@link #getPid}.
* <p>
  You can use expressions in place of numeric values at all places.
  This is implemented using <a href="http://www.singularsys.com/jep/">JEP</a>.
  You can write expression using the syntax that you can
  find <a href="http://www.singularsys.com/jep/doc/html/op_and_func.html">
  here</a>. For example,
  <pre>
  MAG 2
  SIZE 2^MAG
  </pre>
  SIZE=4.
  You can also have complex expression trees like this:
  <pre>
  A B+C
  B D+E
  C E+F
  D 1
  E F
  F 2
  </pre>
  that results in A=7, B=3, C=4, D=1, E=2, F=2

  <p>Expressions are parsed recursively. Note that no optimization are
  done, so expression F may be evaluated three times here (due to the
  fact that appears twice in C and once in B). But since this should 
  be done only in the initialization phase, this is not a real problem.

  <p>Finally, recursive definitions are not allowed (and without
  function definitions, they do not mean anything). Since it is
  difficult to discover complex recursive chains, I've decided
  to use a simple trick: if the depth of recursion is greater
  than a given threshold (configurable, currently 100), an error
  message is printed. This avoid to fill the stack, that results
  in an anonymous OutOfMemoryError. So, if you write
  <pre>
  overlay.size SIZE
  SIZE SIZE-1
  </pre>
  you get an error message:
  Parameter "overlay.size": Probable recursive definition -
  exceeded maximum depth 100
  <p>
  It is possible to assign arbitrary names to multiple instances of a given
  entity, like eg an observer or protocol. For example you can write

  <pre>
  observer.conn ConnectivityObserver
  observer.0 Class1
  observer.2 Class2
  </pre>
  This trick works with any prefix, not only observer. When method
  {@link #getNames} or {@link #getInstanceArray} are called, eg
  <code>getNames("observer")</code>, the the order in which these
  are returned is alphabetical:
  <code>["observer.0","observer.2","observer.conn"]</code>.
  If you are not satisfied with lexicographic order,
  you can specify the order in this way.
  <pre>
  order.observer 2,conn,0
  </pre>
  where the names are separated by any non-word character (non alphanumeric
  or underscore).
  If not all names are listed then the given order is followed by alphabetical
  order of the rest of the items, eg
  <pre>
  order.observer 2
  </pre>
  results in
  <code>["observer.2","observer.0","observer.conn"]</code>.
*
*/
public class Configuration {


// =================== static fields =================================
// ===================================================================

/**
 * The parameter name to configure a maximum depth different from
 * default. Normally you don't want to set this. The default is 100.
 */
private static final String PAR_MAXDEPTH = "expressions.maxdepth"; 

/**
 * The parameter name to configure ordering of the array as returned by
 * {@link #getInstanceArray} and {@link #getNames}.
 * It is read by these methods. This is realy a prefix which is followed by
 * the type specifier. For example: "order.protocol" will define the
 * order of configuration entries that start with
 * "protocol", but it works for any prefix.
 */
public static final String PAR_ORDER = "order"; 

/**
 * The parameter name prefix to specify the set of protocol entries that are
 * used
 * to calculate the protocol identifiers returned by {@link #getPid}.
 */
public static final String PAR_PROT = "protocol"; 


/**
* The properties object that stores all configuration information.
*/
private static Properties config = null;


/**
 *  Map associating string protocol names to the numeric protocol
 * identifiers.
 */
private static Map protocols;


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
	
	// initialize protocol id-s
	protocols = new HashMap();
	String[] prots = getNames(PAR_PROT);//they're retunred in correct order
	for(int i=0; i<prots.length; ++i)
	{
		protocols.put(prots[i],new Integer(i));
	}
	
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

//-------------------------------------------------------------------

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
 * protocol identifier. The value of the property should be the name of a
 * protocol, which is an arbitrary string, and which gets mapped to a
 * number, a protocol id, according to some sorting defined over the
 * protocol names. By default th sorting is alphabetical.
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
		throw new IllegalParameterException(property,
			"Protocol " + protname + " is not defined");
	}
	return ret.intValue();
}

//-------------------------------------------------------------------

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
		// Maybe there are multiple classes with the same
		// non-qualified name.
		String fullname = ClassFinder.getQualifiedName(classname);
		if (fullname != null && fullname.indexOf(',') >= 0) {
			throw new IllegalParameterException(property,
			"The non-qualified class name " + classname + 
			" corresponds to multiple fully-qualified classes: " +
			fullname);
		}
	}
	if (c == null) {
		// Last attempt: maybe the fully classified name is wrong,
		// but the classname is correct. 
		String shortname = ClassFinder.getShortName(classname);
		String fullname = ClassFinder.getQualifiedName(shortname);
		if (fullname != null) {
			throw new IllegalParameterException(property,
			"Class " + classname + 
			" does not exists. Possible candidate(s): " +
			fullname);
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
 * {@link #getInstanceArray} will use this method to create instances.
 * In other words, calling
 * {@link #getInstance(String)} with these names results in the
 * same instances {@link #getInstanceArray} returns.
 *
 * <p>
 * The array is sorted as follows. If there is no config entry
 * <code>PAR_ORDER+"."+name</code> then the order is aplhabetical. Otherwise
 * this entry defines the order. It must contain a list of entries
 * from the values that belong to the given <code>name</code>, but
 * <em>without</em> the prefix. That is, eg <code>"first"</code> instead of
 * <code>name+".first"</code>.
 * It is assumed that these values contain only word characters (alphanumeric
 * and underscore '_'. The order configuration entry thus contains a list
 * of entries separated by any non-word characters.
 * It is not required that all the names are listed. The returned
 * ordering is such that it is consistent with the list
 * in the order parameter: it starts with the names listed in the order
 * configuration parameter and continues with the rest of the names in
 * alphabetical order.
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
	Configuration.order(ret,name);
	return ret;
}

//-------------------------------------------------------------------

/**
 * The input of this method is a set of item <code>names</code>
 * (eg initializers,
 * observers, dynamics and protocols) and a string specifying the type
 * (prefix) of these.
 * The output is in <code>names</code>, which will contain a permutation
 * of the original array.
 * Parameter PAR_ORDER+"."+type is read from the
 * configuration. If it is not defined then the order is identical to
 * that of <code>names</code>. If it is specified then it defines the
 * order the following way. The configuration entry must contain entries
 * from <code>names</code>. It is assumed that the entries in
 * <code>names</code> contain only word characters (alphanumeric
 * and underscore '_'. The order configuration entry thus cintains a list
 * of entries from <code>names</code> separated by any non-word characters.
 * It is not required that all entries are listed. The returned
 * ordering of <code>names</code> is such that it is consistent with the list
 * in the order parameter.
 * 
 * 
 * @param names
 *   the set of item names to be searched
 * @param type 
 *   the string identifying the particular set of items to be inspected
 */
private static void order(String[] names, String type)
{
	String order = getString(PAR_ORDER+"."+type, null);
	
	if( order != null )
	{
		// split around non-word characters
		String[] sret = order.split("\\W");
		for (int i=0; i < sret.length; i++)
		{
			int j=i;
			for(; j<names.length; ++j)
				if( names.equals(type+"."+sret[i])) break;
			if( j == names.length )
			{
				throw new IllegalParameterException(
				PAR_ORDER+"."+type,
				type + "." + sret[i] + " is not defined.");
			}
			else // swap the element to current position
			{
				String tmps = names[j];
				names[j] = names[i];
				names[i] = tmps;
			}
		}
	}
}



}

