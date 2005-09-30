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
		
package peersim.config;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import org.lsmp.djep.groupJep.*;

/**
* Fully static class to store configuration information.
* It defines a method, {@link #setConfig},
* to set configuration data. This method is called by the simulator engines
* as the very first thing they do. It can be called only once, after that the
* class becomes read only.
* All components can then access this configuration and utility methods to read
* property values based on their names.
* <p>
* The design of this class also hides the actual
* implementation of the configuration which can be Properties, XML, whatever.
* Currently only Properties is supported.
* <p>
* Apart from storing (name,value) pairs, this class also does some processing,
* and offers some utility functions.
* This extended functionality consists of the following: reading values with
* type checking, ordering of entries, pre-processing
* protocol names, parsing expressions, resolving underspecified classnames,
* and finaly some basic debugging possibilities. We discuss these in the
* following.
* <p>
* Note that the configuration is initialized using a Properties object.
* The class of this object might implement some additional pre-processing
* on the file or provide an extended syntax for defining property files.
* See {@link ParsedProperties} for more details. This is the class that is
* currently used by simulation engines.
 <h3>Typed reading of values</h3>
 Properties can have arbitrary values of type String. This class offers
 a set of read methods that perform the appropriate conversion of the
 string value to the given type, eg long.
 They also allow for specifiying default values in case the given property
 is not specified.
  <h3>Resolving class names</h3>

  The possibilities for the typed reading of a value includes interpreting
  the value as a class name.
  In this case an object will be constructed.
  It is described at method {@link #getInstance(String)} how this is achieved
  exactly.
  What needs to be noted here is that the property value need not be a
  fully specified classname.
  It might contain only the short class name without the package specification.
  In this case, it is attempted to locate a class with that name in the
  classpath, and if a unique class is found, it will be used.
  This simplifies the configuration files and also allows to remove their
  dependence on the exact location of the class.
  
  <h3>Components and their ordering</h3>
  The idea of the configuration is that it mostly contains components and their
  descriptions (parameters).
  Although this class is blind to the semantics of these components, it
  offers some low level functionality that helps dealing with them.
  This functionality is based on the assumption that
  components have a type and a name. Both types and names are strings
  of alphanumeric and underscore characters.
  For example, {@value #PAR_PROT} is a type, "foo" can be a name.
  Method {@link #getNames} allow the caller to get the list of names for a
  given type.
  Some other methods, like {@link #getInstanceArray} use this list to
  return a list of components.
  
  <p>
  Assuming the configuration is in Properties format (which is currently
  the only format available) component types and names are defined as follows.
  Property names containing two non-empty words separated by
  one dot (".") character are
  treated specially (the words contain word characters: alphanumeric and
  underscore ("_")).
  The first word will be the type, and the second is the name
  of a component.
  For example,
  <pre>
  control.conn ConnectivityObserver
  control.1 WireKOut
  control.2 PrintGraph
  </pre>
  defines control components of names "conn","1" an "2" (arguments of the
  components not shown).
  When
  {@link #getNames} or {@link #getInstanceArray} are called, eg
  <code>getNames("control")</code>, then the order in which these
  are returned is alphabetical:
  <code>["control.1","control.2","control.conn"]</code>.
  If you are not satisfied with lexicographic order,
  you can specify the order in this way.
  <pre>
  order.control 1,conn,2
  </pre>
  where the names are separated by any non-word character (non alphanumeric
  or underscore).
  If not all names are listed then the given order is followed by alphabetical
  order of the rest of the items, eg
  <pre>
  order.control 2
  </pre>
  results in
  <code>["control.2","control.1","control.conn"]</code>.
  <p>
  It is also possible to exclude elements from the list, while
  ordering them. The syntax is identical to that of the above, only the
  parameter name begins with <code>include</code>. For example
  <pre>
  include.control conn 2
  </pre>
  will result in returning <em>only</em> <code>control.conn</code> and
  <code>control.2</code>, in this order.
  Note that for example the empty list results in a zero length array in this
  case.
  <em>Important!</em> If include is
  defined then ordering is ignored. That is, include is stronger than order.
 <h3>Protocol names</h3>
 As mentioned, the configuration is generally blind to the actual names of the
 components.
 There is an exception: the components of type {@value #PAR_PROT}. These
 are pre-processed a bit to enhance performance:
 protocol names are mapped to numeric protocol identifiers.
 The numeric identifier of a protocol is its index in the array
 returned by {@link #getNames}. See above how to control this order.
 The numeric identifiers then can be looked up based on the name and
 vice versa.
 Besides, the identifier can be directly requested based on a property name
 when the protocol name
 is the value of a property which is frequently the case.
 <p>
 <h3>Expressions</h3>
 Numeric property values can be complex expressions, that are parsed
 using <a href="http://www.singularsys.com/jep/">JEP</a>.
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

  <p>Expressions are parsed recursively. Note that no optimization is
  done, so expression F is evaluated three times here (due to the
  fact that appears twice in C and once in B). But since properties
  are read just once at initialization, this is not a performance
  problem.

  <p>Finally, recursive definitions are not allowed (and without
  function definitions, they make no sense). Since it is
  difficult to discover complex recursive chains, a simple trick
  is used: if the depth of recursion is greater
  than a given threshold (configurable, currently {@value #DEFAULT_MAXDEPTH}, 
  an error message is printed. This avoids to fill the stack, that results
  in an anonymous OutOfMemoryError. So, if you write
  <pre>
  overlay.size SIZE
  SIZE SIZE-1
  </pre>
  you get an error message:
  Parameter "overlay.size": Probable recursive definition -
  exceeded maximum depth {@value #DEFAULT_MAXDEPTH}
  
  <h3>Debug</h3>
  
  It is possible to obtain debug information about the configuration
  properties by activating special configuration properties. 
  <p>
  If property {@value #PAR_DEBUG} is defined,
  each config property and the associated value are printed. Properties
  that are not present in the config file but have default values are
  postfixed with the string "(DEFAULT)".
  <p>
  If property {@value #PAR_DEBUG}  is defined and it is equal to
  {@value #DEBUG_EXTENDED},
  information about the configuration method invoked, and where
  this method is invoked, is also printed.
  If it is equal to {@value #DEBUG_FULL},
  all the properties are printed, even if they are not read.
  <p>
  Each line printed by this debug feature is prefixed by the
  string "DEBUG".

  <h3>Use of brackets</h3>
  
  For the sake of completeness, we mention it here that if this class is
  initialized using {@link ParsedProperties}, then it is possible to
  use some more compressed format to specify the components.
  See {@link ParsedProperties#load}.
*
*/
public class Configuration {

//=================== constants =================================
//===================================================================

/* Constants used when reading integer (int, long) values */
private static BigInteger maxlong = new BigInteger(""+ Long.MAX_VALUE);
private static BigInteger minlong = new BigInteger(""+ Long.MIN_VALUE);
private static BigInteger maxint = new BigInteger(""+ Integer.MAX_VALUE);
private static BigInteger minint = new BigInteger(""+ Integer.MIN_VALUE);

// =================== static fields =================================
// ===================================================================

	
/** Symbolic constant for no debug */
private static final int DEBUG_NO   = 0;

/** Symbolic constant for regular debug */
private static final int DEBUG_REG  = 1;

/** Symbolic constant for extended debug */
private static final int DEBUG_CONTEXT = 2;

/** Default max depth limit to avoid recursive definitions */
private static final int DEFAULT_MAXDEPTH = 100;

/**
 * The debug level for the configuration.
 * If defined, a line is printed for each configuration
 * parameter read. If defined and equal to {@value #DEBUG_EXTENDED}, additional
 * context information for debug is printed. If defined and equal
 * to {@value #DEBUG_FULL}, all the configuration properties are printed at 
 * the beginning, not just when they are called.
 * @config
 */
private static final String PAR_DEBUG = "debug.config"; 

/**
 * If parameter {@value #PAR_DEBUG} is equal to this string, 
 * additional context information for debug is printed.
 */
private static final String DEBUG_EXTENDED = "context";

/**
 * If parameter {value #PAR_DEBUG} is equal to this string, 
 * all the configuration properties are printed at the beginning,
 * not just when they are called.
 */
private static final String DEBUG_FULL = "full";
	
/**
 * The maximum depth for expressions. This is a simple mechanism to
 * avoid unbounded recursion. The default is {@value #DEFAULT_MAXDEPTH}, 
 * and you probably don't want to change it.
 * @config
 */
private static final String PAR_MAXDEPTH = "expressions.maxdepth"; 

/**
 * Used to configure ordering of the components. Determines the ordering in
 * the array as returned by {@link #getNames}.
 * See the general description of {@link Configuration} for details.
 * @config
 */
private static final String PAR_ORDER = "order"; 

/**
 * Used to configure ordering of the components. Determines the ordering in
 * the array as returned by {@link #getNames}, and can bu used to also
 * exclude elements.
 * See the general description of {@link Configuration} for details.
 * @config
 */
private static final String PAR_INCLUDE = "include"; 

// XXX it's ugly because it replicates the definition of Node.PAR_PROT, but
// this would be the only dependence on the rest of the core...
/**
 * The type name of components describing protocols. This is the only point
 * at which the class is not blind to the actual semantics of the
 * configuration.
 */
static final String PAR_PROT = "protocol"; 


/**
* The properties object that stores all configuration information.
*/
private static Properties config = null;


/**
 * Map associating string protocol names to the numeric protocol
 * identifiers. The protocol names are understood without prefix.
 */
private static Map<String,Integer> protocols;


/**
 *  The maximum depth that can be reached when analyzing expressions.
 *  This value can be substituted by setting the configuration parameter
 *  PAR_MAXDEPTH.
 */
private static int maxdepth = DEFAULT_MAXDEPTH;

/** Debug level */
private static int debugLevel = DEBUG_NO;

// =================== initialization ================================
// ===================================================================

/** to prevent construction */
private Configuration() {}

// =================== static public methods =========================
// ===================================================================


/** 
* Sets the system-wide configuration in Properties format. It can be called
* only once. After that the configuration becomes unmodifiable (read only).
* If modification is attempted, a RuntimeException is thrown and no change is
* made.
* @param p The Properties object containing configuration info
*/
public static void setConfig( Properties p ) {

	if( config != null )
	{
		throw new RuntimeException(
			"Setting configuration was attempted twice.");
	}
	
	config = p;
	maxdepth = Configuration.getInt(PAR_MAXDEPTH, DEFAULT_MAXDEPTH);
	
	// initialize protocol id-s
	protocols = new HashMap<String,Integer>();
	String[] prots = getNames(PAR_PROT);//they're returned in correct order
	for(int i=0; i<prots.length; ++i)
	{
		protocols.put(  prots[i].substring(PAR_PROT.length()+1),
				new Integer(i));
	}

	String debug = config.getProperty(PAR_DEBUG);
	if (DEBUG_EXTENDED.equals(debug))
	  debugLevel = DEBUG_CONTEXT;
	else if (DEBUG_FULL.equals(debug)) {
		Map map = new TreeMap();
		Enumeration e = p.propertyNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = p.getProperty(name);
			map.put(name, value);
		}
		Iterator i = map.keySet().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			System.err.println("DEBUG " + name +
				("".equals(map.get(name)) ? 
					"" : " = " + map.get(name))); 
		}
	}
	else if (debug != null)
		debugLevel = DEBUG_REG;
}

// -------------------------------------------------------------------

/**
* @return true if and only if name is a specified (exisitng) property.
*/
public static boolean contains(String name) {
	
	boolean ret = config.containsKey(name); 
	debug(name, "" +ret);
	return ret;
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* {@link MissingParameterException}.
* @param name Name of configuration property
* @param def default value
*/
public static boolean getBoolean(String name, boolean def) {

	try
	{
		return Configuration.getBoolean(name);
	}
	catch( Exception e )
	{
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given property. If not found, or the value is empty string
* then throws a 
* {@link MissingParameterException}.
* Empty string is not accepted as false due to
* the similar function of {@link #contains} which returns true in that case.
* True is returned if the lowercase value of
* the property is "true", otherwise false is returned.
* @param name Name of configuration property
*/
public static boolean getBoolean(String name) {

	if( config.getProperty(name) == null ) {
		throw new MissingParameterException(name,
				"\nPossibly incorrect property: " +
				getSimilarProperty(name));
	}
	if( config.getProperty(name).matches("\\p{Blank}*") )
		throw new MissingParameterException(name,
		"Blank value is not accepted when parsing Boolean.");

	boolean ret = (new Boolean(config.getProperty(name))).booleanValue(); 
	debug(name, "" +ret);
	
	return ret;
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, returns the default value.
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* {@link MissingParameterException}.
* @param name Name of configuration property
*/
public static int getInt( String name ) 
{
	Number ret = (Number) getValInteger(name, name, 0);
	debug(name, "" +ret);
	return ret.intValue();
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, returns the default value.
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* {@link MissingParameterException}.
* @param name Name of configuration property
*/
public static long getLong( String name )
{
	Number ret = (Number) getValInteger(name, name, 0);
	debug(name, "" +ret);
	return ret.longValue();
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, returns the default value.
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* MissingParameterException.
* @param name Name of configuration property
*/
public static double getDouble( String name ) 
{
	double ret = getVal(name, name, 0); 
	debug(name, "" +ret);
	return ret;
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
private static double getVal(String initial, String property, int depth)
{
	if (depth > maxdepth) {
		throw new IllegalParameterException(initial, 
		"Probable recursive definition - exceeded maximum depth " + 
		maxdepth);
	}

	String s = config.getProperty(property);
	if (s == null || s.equals("")) {
		throw new MissingParameterException(property, 
				" when evaluating property " + initial +
				"\nPossibly incorrect property: " +
				getSimilarProperty(property));
	}

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
 * Read numeric property values, parsing expression if necessary.
 * 
 * @param initial the property name that started this expression evaluation
 * @param property the current property name to be evaluated
 * @param depth the depth reached so far
 * @return the evaluation of the expression associated to property  
 */
private static Object getValInteger(String initial, String property, int depth)
{
	if (depth > maxdepth) {
		throw new IllegalParameterException(initial, 
		"Probable recursive definition - exceeded maximum depth " + 
		maxdepth);
	}

	String s = config.getProperty(property);
	if (s == null || s.equals("")) {
		throw new MissingParameterException(property, 
				" when evaluating property " + initial +
				"\nPossibly incorrect property: " +
				getSimilarProperty(property));
	}

	GroupJep jep = new GroupJep(new Integers());
	jep.setAllowUndeclared(true);
	
	jep.parseExpression(s);
	String[] symbols = getSymbols(jep);
	for (int i=0; i < symbols.length; i++) {
		Object d = getValInteger(initial, symbols[i], depth+1);
		jep.addVariable(symbols[i], d);
	}
	Object ret = jep.getValueAsObject();
	if (jep.hasError()) 
		System.err.println(jep.getErrorInfo());
	return ret;
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
* Reads given configuration property. If not found, returns the default value.
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
		debug(name, "" +def + " (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* MissingParameterException.
* Removes trailing whitespace characters.
* @param property Name of configuration property
*/
public static String getString( String property ) {

	String result = config.getProperty(property);
	if( result == null ) 
		throw new MissingParameterException(property, 
				"\nPossibly incorrect property: " +
				getSimilarProperty(property));
	debug(property, "" +result);
	
	return result.trim();
}

//-------------------------------------------------------------------

/**
 * Reads the given property from the configuration interpreting it as
 * a protocol name. Returns the numeric protocol identifier
 * of this protocol name. See the discussion of protocol name at
 * {@link Configuration} for details
 * on how this numeric id is calculated
 *  
 * @param name Name of configuration property
 * @return the numeric protocol identifier associated to the value of the
 * property
 */
public static int getPid( String name ) {
	
	String protname = getString(name);
	return lookupPid(protname);
}

//-------------------------------------------------------------------

/**
 * Calls {@link #getPid(String)}, and returns the default if no property
 * is defined with the given name.
 *
 * @param name Name of configuration property
 * @param pid the default protocol identifier
 * @return the numeric protocol identifier associated to the value of the
 * property, or the default if not defined
 */
public static int getPid( String name, int pid ) {
	
	try {
		String protname = getString(name);
		return lookupPid(protname);
	} catch (MissingParameterException e) {
		return pid;
	}
}

//-------------------------------------------------------------------

/**
 * Returns the numeric 
 * protocol identifier of the given protocol name.
 * 
 * @param protname the protocol name.
 * @return the numeric protocol identifier associated to the protocol
 *   name
 */
public static int lookupPid( String protname ) {
	
	Integer ret = (Integer) protocols.get(protname); 
	if (ret == null) {
		throw new MissingParameterException(PAR_PROT+"."+protname, 
				"\nPossibly incorrect property: " +
				getSimilarProperty(PAR_PROT+"."+protname));

	}
	return ret.intValue();
}

//-------------------------------------------------------------------

/**
 * Returns the name of a 
 * protocol that has the given identifier.
 * <p>Note that this is not a constant time operation in the number of
 * protocols, although typically there are very few protocols defined.
 * 
 * @param pid numeric protocol identifier.
 * @return name of the protocol that has the given id. null if no protocols
 * have the given id.
 */
public static String lookupPid( int pid ) {
	
	if(!protocols.containsValue(pid))  return null;
	for(Map.Entry<String,Integer> i : protocols.entrySet())
	{
		if(i.getValue().intValue()==pid)
			return i.getKey();
	}
	
	// never reached but java needs it...
	return null;
}

//-------------------------------------------------------------------

/**
* Reads given configuration property. If not found, throws a 
* {@link MissingParameterException}.
* When creating the Class object, a few attempts are done to resolve
* the classname. See {@link Configuration} for details.
* @param name Name of configuration property
*/
public static Class getClass(String name)
{
	String classname = config.getProperty(name);
	if (classname == null) 
		throw new MissingParameterException(name, 
				"\nPossibly incorrect property: " +
				getSimilarProperty(name));
	debug(name, classname);
	
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
			throw new IllegalParameterException(name,
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
			throw new IllegalParameterException(name,
			"Class " + classname + 
			" does not exists. Possible candidate(s): " +
			fullname);
		}		
	}
	if (c == null) {
		throw new IllegalParameterException(name,
				"Class " + classname + " not found");
	}
	return c;
}	

//-------------------------------------------------------------------

/**
* Reads given configuration property. If not found, returns the default value.
* @param name Name of configuration property
* @param def default value
* @see #getClass(String)
*/
public static Class getClass( String name, Class def ) {

	try
	{
		return Configuration.getClass(name);
	}
	catch( Exception e )
	{
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration property for a class name. It returns an instance of
* the class. The class must implement a constructor that takes a String as an
* argument. The value of this string will be <tt>name</tt>. The constructor
* of the class can see the configuration so it can make use of this name
* to read its own parameters from it.
* @param name Name of configuration property
*/
public static Object getInstance( String name ) {

	Class c = getClass(name);
	final String classname = c.getSimpleName();
		
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
			"(String) constructor");
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
		throw new IllegalParameterException(name, e+"");
	} 
}

// -------------------------------------------------------------------

/**
* Reads given configuration property for a class name. It returns an instance of
* the class. The class must implement a constructor that takes a String
* and an Object as
* arguments. The value of the string will be <tt>name</tt>. The constructor
* of the class can see the configuration so it can make use of this name
* to read its own parameters from it.
* @param name Name of configuration property
* @param obj the object to pass to the constructor
*/
public static Object getInstance( String name, Object obj ) {

	Class c = getClass(name);		
	final String classname = c.getSimpleName();

	try
	{
		Class pars[] = { String.class, Object.class };
		Constructor cons = c.getConstructor( pars );
		Object objpars[] = { name, obj };
		return cons.newInstance( objpars );
	}
	catch( NoSuchMethodException e )
	{
		throw new IllegalParameterException(name,
			"Class " + classname + " has no " + classname +
			"(String, Object) constructor");
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
		throw new IllegalParameterException(name, e+"");
	} 
}

//-------------------------------------------------------------------

/**
* It returns an array of class instances.
* The instances are constructed by calling {@link #getInstance(String)}
* on the names returned by {@link #getNames(String)}.
* @param name The component type (ie prefix of the list of configuration
* properties) which will be
* passed to {@link #getNames(String)}.
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
 * Returns an array of names prefixed by the specified name.
 * The array is sorted as follows. If there is no config entry
 * <code>{@value #PAR_INCLUDE}+"."+name</code> or
 * <code>{@value #PAR_ORDER}+"."+name</code> then the order is
 * aplhabetical. Otherwise
 * this entry defines the order. For more information see
 * {@link Configuration}.
 * @param name the component type (ie, the prefix)
 * @return the full property names in the order specified by the configuration
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
	return Configuration.order(ret,name);
}

//-------------------------------------------------------------------

/**
 * The input of this method is a set of property <code>names</code>
 * (eg initializers,
 * controls and protocols) and a string specifying the type
 * (prefix) of these.
 * The output is in <code>names</code>, which will contain a permutation
 * of the original array.
 * Parameter PAR_INCLUDE+"."+type, or if not present, PAR_ORDER+"."+type
 * is read from the
 * configuration. If non of them are defined then the order is identical to
 * that of <code>names</code>. Otherwise the configuration entry must contain
 * entries
 * from <code>names</code>. It is assumed that the entries in
 * <code>names</code> contain only word characters (alphanumeric
 * and underscore '_'. The order configuration entry thus cintains a list
 * of entries from <code>names</code> separated by any non-word characters.
 * <p>
 * It is not required that all entries are listed.
 * If PAR_INCLUDE is used, then only those entries are returned that are
 * listed.
 * If PAR_ORDER is used, then all names are returned, but the array will start
 * with those that are listed. The rest of the names follow in alphabetical
 * order.
 * 
 * 
 * @param names
 *   the set of property names to be searched
 * @param type 
 *   the string identifying the particular set of properties to be inspected
 */
private static String[] order(String[] names, String type)
{
	String order = getString(PAR_INCLUDE+"."+type, null);
	boolean include = order!=null;
	if( !include ) order = getString(PAR_ORDER+"."+type, null);
	
	int i=0;
	if( order != null && !order.equals("") )
	{
		// split around non-word characters
		String[] sret = order.split("\\W+");
		for (; i < sret.length; i++)
		{
			int j=i;
			for(; j<names.length; ++j)
				if( names[j].equals(type+"."+sret[i])) break;
			if( j == names.length )
			{
				throw new IllegalParameterException(
				(include?PAR_INCLUDE:PAR_ORDER)+"."+type,
				type + "." + sret[i]+ " is not defined.");
			}
			else // swap the element to current position
			{
				String tmps = names[j];
				names[j] = names[i];
				names[i] = tmps;
			}
		}
	}
	
	Arrays.sort(names,i,names.length);
	int retsize = ( include ? i : names.length );
	String [] ret = new String[retsize];
	for( int j=0; j<retsize; ++j ) ret[j] = names[j];
	return ret;
}

//-------------------------------------------------------------------

/**
 * Print debug information for configuration. The amount of 
 * information depends on the debug level DEBUG.
 * 0 = nothing
 * 1 = just the config name
 * 2 = config name plus methodd calling
 * 
 * @param name
 */
private static void debug(String name, String result)
{
	if (debugLevel == DEBUG_NO)
		return;
	StringBuffer buffer = new StringBuffer();
	buffer.append("DEBUG ");
	buffer.append(name);
	buffer.append(" = ");
	buffer.append(result);

	// Additional info
	if (debugLevel == DEBUG_CONTEXT) {
		
	  buffer.append("\n  at ");
		// Obtain the stack trace
		StackTraceElement[] stack = null;
		try {
			throw new Exception();
		} catch (Exception e) {
			stack = e.getStackTrace();
		}
		
		// Search the element that invoked Configuration
		// It's the first whose class is different from Configuration
		int pos;
		for (pos=0; pos < stack.length; pos++) {
			if (!stack[pos].getClassName().equals(Configuration.class.getName()))
				break;
		}

		buffer.append(stack[pos].getClassName());
		buffer.append(":");
		buffer.append(stack[pos].getLineNumber());
		buffer.append(", method ");
		buffer.append(stack[pos-1].getMethodName());
		buffer.append("()");
	}
	
	
	System.err.println(buffer);
}

//-------------------------------------------------------------------

/** 
 * @return an array of adjacent letter pairs contained in the input string
 * http://www.catalysoft.com/articles/StrikeAMatch.html 
 */
private static String[] letterPairs(String str)
{
	int numPairs = str.length() - 1;
	String[] pairs = new String[numPairs];
	for (int i = 0; i < numPairs; i++) {
		pairs[i] = str.substring(i, i + 2);
	}
	return pairs;
}

//-------------------------------------------------------------------

/** 
 * @return an ArrayList of 2-character Strings. 
 * http://www.catalysoft.com/articles/StrikeAMatch.html
 */
private static ArrayList wordLetterPairs(String str)
{
	ArrayList allPairs = new ArrayList();
	// Tokenize the string and put the tokens/words into an array
	String[] words = str.split("\\s");
	// For each word
	for (int w = 0; w < words.length; w++) {
		// Find the pairs of characters
		String[] pairsInWord = letterPairs(words[w]);
		for (int p = 0; p < pairsInWord.length; p++) {
			allPairs.add(pairsInWord[p]);
		}
	}
	return allPairs;
}

//-------------------------------------------------------------------

/** 
 * @return lexical similarity value in the range [0,1] 
 * http://www.catalysoft.com/articles/StrikeAMatch.html
 */
private static double compareStrings(String str1, String str2)
{
	ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
	ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
	int intersection = 0;
	int union_ = pairs1.size() + pairs2.size();
	for (int i = 0; i < pairs1.size(); i++) {
		Object pair1 = pairs1.get(i);
		for (int j = 0; j < pairs2.size(); j++) {
			Object pair2 = pairs2.get(j);
			if (pair1.equals(pair2)) {
				intersection++;
				pairs2.remove(j);
				break;
			}
		}
	}
	return (2.0 * intersection) / union_;
}

//-------------------------------------------------------------------

/** 
 * Among the defined properties, returns the one more
 * similar to String property
 */
private static String getSimilarProperty(String property)
{
	String bestProperty = null;
	double bestValue = 0.0;
	Enumeration e = config.keys();
	while (e.hasMoreElements()) {
		String key = (String) e.nextElement();
		double compare = compareStrings(key, property);
		if (compare > bestValue) {
			bestValue = compare;
		  bestProperty = key;
		}
	}
	return bestProperty;
}

}

