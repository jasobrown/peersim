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
import java.math.*;
import java.util.*;
import org.lsmp.djep.groupJep.*;

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
* through method {@link #getPid(String)}.
* <h3>Expressions</h3>
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

  <p>Expressions are parsed recursively. Note that no optimization is
  done, so expression F is evaluated three times here (due to the
  fact that appears twice in C and once in B). But since properties
  are read just once at initialization, this is not a performance
  problem.

  <p>Finally, recursive definitions are not allowed (and without
  function definitions, they make no sense). Since it is
  difficult to discover complex recursive chains, a simple trick
  is used: if the depth of recursion is greater
  than a given threshold (configurable, currently 100), an error
  message is printed. This avoids to fill the stack, that results
  in an anonymous OutOfMemoryError. So, if you write
  <pre>
  overlay.size SIZE
  SIZE SIZE-1
  </pre>
  you get an error message:
  Parameter "overlay.size": Probable recursive definition -
  exceeded maximum depth 100
  
  <h3>Ordering</h3>
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
  <p>
  It is also possible to exclude elements from the list, while
  ordering them. The syntax is identical to that of the above, only the
  parameter name begins with <code>include</code>. For example
  <pre>
  include.observer conn 2
  </pre>
  will result in returning <em>only</em> <code>observer.conn</code> and
  <code>observer.2</code>, in this order.
  Note that for example the empy list results in a zero length array in this
  case.
  <em>Important!</em> If include is
  defined then ordering is ignored. That is, include is stronger than order.

  <h3>Debug</h3>
  
  It is possible to obtain debug information about the configuration
  properties by activating special configuration properties. 
  <p>
  If property debug.config is defined,
  each config property and the associated value are printed. Properties
  that are not present in the config file but have default values are
  postfixed with the string "(DEFAULT").
  <p>
  If property debug.config is defined and it is equal to "context",
  information about the configuration method invoked, and where
  this method is invoked, is also printed.
  <p>
  If property debug.config is defined and it is equal to "full",
  all the properties are printed, even if they are not read.
  <p>
  Each line printed by this debug feature is prefixed by the
  string "DEBUG".

  <h3>Use of brackets</h3>
  
  It is possible to use brackets to simplify the writing of the 
  configuration properties. When a bracket is present, it must
  be the only non-space element of a line. The last property defined 
  before the opening bracket define the prefix that is added to all the 
  properties defined included between brackets.
  In other words, a construct like this:
  <pre>
  observer.degree GraphObserver 
  {
    protocol newscast
    undir
  }
  </pre>
  is equivalent to the definition of these four properties:
  <pre>
  observer.degree GraphObserver 
  observer.degree.protocol newscast
  observer.degree.undir
  </pre>
  
  Nested brackets are possible. The rule of the last property before 
  the opening bracket applies also to the inside brackets, with
  the prefix being the complete property definition (so, including
  the prefix observed before). Example:
  <pre>
	dynamics.1 peersim.dynamics.DynamicNetwork
	{
	  add CRASH
	  substitute
	  init.0 peersim.dynamics.WireRegularRandom 
	  {
	    degree DEGREE
	    protocol 0
	  }
	}
  </pre>
  defines the following properties:
  <pre>
	dynamics.1 peersim.dynamics.DynamicNetwork
	dynamics.1.add CRASH
	dynamics.1.substitute
	dynamics.1.init.0 peersim.dynamics.WireRegularRandom 
	dynamics.1.init.0.degree DEGREE
	dynamics.1.init.0.protocol 0
  </pre>
  
  <p>
  Know limitations: 
  The parsing mechanism is very simple; no complex error messages
  are provided. In case of missing closing brackets, the simulator
  will stop reporting the number of missing brackets. Additional
  closing brackets (i.e., missing opening brackets) produce an
  error messages reporting the line where the closing bracket
  is present. Misplaced brackets (included in lines that
  contains other characters) are ignored, thus may produce
  the previous error messages.
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
	
/**
 * The parameter name to configure the debug level for the configuration
 * mechanism. If defined, a line is printed for each configuration
 * parameter read. If defined and equal to {@link #PAR_EXTENDED}, additional 
 * debug information is printed.
 */
private static final String PAR_DEBUG = "debug.config"; 

/**
 * If parameter {@link #PAR_DEBUG} is equal to this string, 
 * additional context information for debug is printed.
 */
private static final String PAR_EXTENDED = "context";

/**
 * If parameter {@link #PAR_DEBUG} is equal to this string, 
 * all the configuration items are printed at the beginning,
 * not just when they are called.
 */
private static final String PAR_FULL = "full";
	
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
 * The parameter name to configure ordering and exclusion of the array as
 * returned by {@link #getInstanceArray} and {@link #getNames}.
 * It is read by these methods. This is realy a prefix which is followed by
 * the type specifier. For example: "include.protocol" will define the
 * set and order of configuration entries that start with
 * "protocol", but it works for any prefix.
 */
public static final String PAR_INCLUDE = "include"; 

// XXX it's ugly because it replicates the definition of PAR_PROT, but
// this would be the only dependence on the rest of the core...
/**
 * The parameter name prefix to specify the set of protocol entries that are
 * used
 * to calculate the protocol identifiers returned by {@link #getPid(String)}.
 */
public static final String PAR_PROT = "protocol"; 


/**
* The properties object that stores all configuration information.
*/
private static Properties config = null;


/**
 * Map associating string protocol names to the numeric protocol
 * identifiers. The protocol names are understood without prefix.
 */
private static Map protocols;


/**
 *  The maximum depth that can be reached when analyzing expressions.
 *  This value can be substituted by setting the configuration parameter
 *  PAR_MAXDEPTH.
 */
private static int maxdepth = 100;

/** Debug level */
private static int debugLevel = DEBUG_NO;

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
		protocols.put(  prots[i].substring(PAR_PROT.length()+1),
				new Integer(i));
	}

	String debug = config.getProperty(PAR_DEBUG);
	if (PAR_EXTENDED.equals(debug))
	  debugLevel = DEBUG_CONTEXT;
	else if (PAR_FULL.equals(debug)) {
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
					("".equals(map.get(name)) ? "" : " = " + map.get(name))  
					); 
		}
	}
	else if (debug != null)
		debugLevel = DEBUG_REG;
	
	return prev;
}

// -------------------------------------------------------------------

/**
* @return true if and only if the specified name is assigned a value in the
* configuration
*/
public static boolean contains(String name) {
	
	boolean ret = config.containsKey(name); 
	debug(name, "" +ret);
	return ret;
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, returns the default value.
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
		System.err.println(def + " (Default value)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, or the value is empty string
* then throws a 
* MissingParameterException. Empty string is not accepted as false due to
* the similar function of "contains" which returns true in that case.
* True is returned if the lowercase value of
* the item is "true", otherwise false is returned.
* @param name Name of configuration property.
*/
public static boolean getBoolean(String name) {

	if( config.getProperty(name) == null ) {
		throw new MissingParameterException(name, "\nPossible uncorrect property: " +
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws a 
* MissingParameterException.
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws a 
* MissingParameterException.
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
		debug(name, ""+def+" (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads given configuration item. If not found, throws a 
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
public static double getVal(String initial, String property, int depth)
{
	if (depth > maxdepth) {
		throw new IllegalParameterException(initial, 
		"Probable recursive definition - exceeded maximum depth " + 
		maxdepth);
	}

	String s = config.getProperty(property);
	if (s == null || s.equals("")) {
		throw new MissingParameterException(property, 
				" when evaluating property " + initial + "\nPossible uncorrect property: " +
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
public static Object getValInteger(String initial, String property, int depth)
{
	if (depth > maxdepth) {
		throw new IllegalParameterException(initial, 
		"Probable recursive definition - exceeded maximum depth " + 
		maxdepth);
	}

	String s = config.getProperty(property);
	if (s == null || s.equals("")) {
		throw new MissingParameterException(property, 
				" when evaluating property " + initial + "\nPossible uncorrect property: " +
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
		debug(name, "" +def + " (DEFAULT)");
		return def;
	}
}

// -------------------------------------------------------------------

/**
* Reads the configuration item associated to the specified property.
* Removes trailing whitespace characters.
* @param property Name of configuration property
*/
public static String getString( String property ) {

	String result = config.getProperty(property);
	if( result == null ) 
		throw new MissingParameterException(property, 
				"\nPossible uncorrect property: " +
				getSimilarProperty(property));
	debug(property, "" +result);
	
	return result.trim();
}

//-------------------------------------------------------------------

/**
 * Reads the given string property from the configuration and returns the
 * associated numeric 
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
	return lookupPid(protname);
}

//-------------------------------------------------------------------

/**
 * Reads the given string property from the configuration and returns the
 * associated numeric 
 * protocol identifier. The value of the property should be the name of a
 * protocol, which is an arbitrary string, and which gets mapped to a
 * number, a protocol id, according to some sorting defined over the
 * protocol names. By default the sorting is alphabetical.
 * If the property is not defined, defaults to the specified protocol
 * identifier.
 *  
 * @param property the property name
 * @param pid the default protocol identifier
 * @return the numeric protocol identifier associated to the protocol
 *   name
 */
public static int getPid( String property, int pid ) {
	
	try {
		String protname = getString(property);
		return lookupPid(protname);
	} catch (MissingParameterException e) {
		return pid;
	}
}

//-------------------------------------------------------------------

/**
 * Reads the given string property and returns the associated numeric 
 * protocol identifier. The parameter should be the name of a
 * protocol, which is an arbitrary string, and which gets mapped to a
 * number, a protocol id, according to some sorting defined over the
 * protocol names. By default the sorting is alphabetical.
 * 
 * @param protname the protocol name.
 * @return the numeric protocol identifier associated to the protocol
 *   name
 */
public static int lookupPid( String protname ) {
	
	Integer ret = (Integer) protocols.get(protname); 
	if (ret == null) {
		throw new MissingParameterException(PAR_PROT+"."+protname, 
				"\nPossible uncorrect property: " +
				getSimilarProperty(PAR_PROT+"."+protname));

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
	if (classname == null) 
		throw new MissingParameterException(name, 
				"\nPossible uncorrect property: " +
				getSimilarProperty(name));
	debug(name, classname);

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
			e.printStackTrace();
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
	if (classname == null) 
		throw new MissingParameterException(name, 
				"\nPossible uncorrect property: " +
				getSimilarProperty(name));
	debug(name, classname);
	
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
 * <code>PAR_INCLUDE+"."+name</code> or
 * <code>PAR_ORDER+"."+name</code> then the order is aplhabetical. Otherwise
 * this entry defines the order. It must contain a list of entries
 * from the values that belong to the given <code>name</code>, but
 * <em>without</em> the prefix. That is, eg <code>"first"</code> instead of
 * <code>name+".first"</code>.
 * It is assumed that these values contain only word characters (alphanumeric
 * and underscore '_'. The order configuration entry thus contains a list
 * of entries separated by any non-word characters.
 * <p>
 * It is not required that all entries are listed.
 * If {@link #PAR_INCLUDE} is used, then only those entries are returned
 * that are listed.
 * If {@link #PAR_ORDER} is used, then all names are returned,
 * but the array will start
 * with those that are listed. The rest of the names follow in alphabetical
 * order.
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
 * The input of this method is a set of item <code>names</code>
 * (eg initializers,
 * observers, dynamics and protocols) and a string specifying the type
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
 *   the set of item names to be searched
 * @param type 
 *   the string identifying the particular set of items to be inspected
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
	int union = pairs1.size() + pairs2.size();
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
	return (2.0 * intersection) / union;
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

