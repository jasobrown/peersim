package peersim.config;

import java.util.*;
import java.lang.reflect.*;

/**
* Fully static class to store configuration information.
* Contains methods to set configuration data and utility methods to read
* items based on their names. Its other purpose is to hide the actual
* implementation of the configuration which can be Properties, XML, whatever.
*/
public class Configuration {


// =================== static fields =================================
// ===================================================================


/**
* The properties object that stores all configuration information.
*/
private static Properties config = null;


// =================== static public methods =========================
// ===================================================================


/** Sets the system-wide configuration in Properties format.
* @param p The Properties object containing coniguration info
* @return The Properties object that was set previously
*/
public static Properties setConfig( Properties p ) {

	Properties prev = config;
	config = p;
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
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
*/
public static int getInt( String name ) {

	try
	{
  		// XXX if not integer than probably config error. I'd rather
		// handle this as an error than silently converting
		return (int) Double.parseDouble( config.getProperty(name) );
	}
	catch( NullPointerException e )
	{
		throw new NoSuchElementException("Property "+name+" not found");
	}
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
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
*/
public static long getLong( String name ) {
 
	try
	{
		return Long.parseLong( config.getProperty(name) );
	}
	catch (NullPointerException e)
	{
		throw new NoSuchElementException("Property "+name+" not found");
	}
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
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
*/
public static double getDouble( String name ) {

	try
	{
		return Double.parseDouble( config.getProperty(name) );
	}
	catch (NullPointerException e)
	{
		throw new NoSuchElementException("Property "+name+" not found");
	}
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
* Reads given configuration item. If not found, returns the default value.
* @param name Name of configuration property
*/
public static String getString( String name ) {

	try
	{
		return config.getProperty(name);
	}
	catch (NullPointerException e)
	{
		// XXX To be fixed
		throw new NoSuchElementException("Property "+name+" not found");
	}
}

// -------------------------------------------------------------------

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
	{
		throw new NoSuchElementException("Property "+name+" not found");
	}
	
	try
	{
		Class c = Class.forName(classname);
		Class pars[] = { String.class };
		Constructor cons = c.getConstructor( pars );
		Object objpars[] = { name };
		return cons.newInstance( objpars );
	}
	catch ( InvocationTargetException e) 
	{
		e.printStackTrace();
		throw new RuntimeException(""+e.getTargetException());
	}
	catch( Exception e )
	{
		throw new RuntimeException(""+e);
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
// XXX if necessari, API will have to be provided to force the (String,Object)
// constructor and not using the default (string) constructor

	String classname = config.getProperty(name);
	if (classname == null)
	{
		throw new NoSuchElementException("Property "+name+" not found");
	}
	
	try
	{
		Class c = Class.forName(config.getProperty(name));
		Class pars[] = { String.class, Object.class };
		Constructor cons = c.getConstructor( pars );
		Object objpars[] = { name, obj };
		return cons.newInstance( objpars );
	}
	catch( NoSuchMethodException e )
	{
		return getInstance( name );
	}
	catch ( InvocationTargetException e ) 
	{
		throw new RuntimeException(""+e.getTargetException());
	}
	catch( Exception e )
	{
		throw new RuntimeException(""+e);
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
* Returns an array of names of maximal length of the form
* name+".0", name+".1", etc, where all names in the list are existing
* property names. If name+"0" is not an existing property name, returns an
* empty array.
* {@link #getInstanceArray} will use this method to create instances.
* In other words, calling
* {@link #getInstance(String)} with these names results in the
* same instances {@link #getInstanceArray} returns.
*/
public static String[] getNames( String name ) {
	
	ArrayList ll = new ArrayList();
	final String pref = name+".";
	String s;
	
	for( int i=0; contains(s=(pref+i)); ++i )
	{
		ll.add( s );
	}

	return (String[])ll.toArray(new String[0]);
}

}
