/*
 * Copyright (c) 2004 The BISON Project
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

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Provides static methods to obtain the package-qualified class name
 * of a class, given just the non-qualified name, and to obtain
 * the non-qualified name, given the package-qualified class name.
 * 
 * Inspired from some code written by David Postill (david@postill.org.uk)
 * (found in http://groups.google.com).
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ClassFinder 
{

//--------------------------------------------------------------------------
//Fields and initialization
//--------------------------------------------------------------------------
	
	
/** Local map containing the associations */
private static Map map = new TreeMap();	
	
static {
	try {
		findClasses(map);
	} catch (IOException e) {
		e.printStackTrace();
	}
}
	
	
//--------------------------------------------------------------------------
//Public static methods
//--------------------------------------------------------------------------
	
/**
 * Returns the non-qualified name of a class, removing all the package
 * information.
 */
public static String getShortName(String className) {
  int index = className.lastIndexOf('.');
  if (index < 0) {
  	return className;
  } else {
  	return className.substring(index+1);
  }
}

/**
 * Returns the package-qualified name associated to the specified
 * non-qualified name, if exists. Otherwise it returns null.
 * 
 * Only classes reachable from the classpath defined by the 
 * "java.class.path" property are considered. 
 * Jar files and directories are both parsed.
 * If multiple classes with the same name but different 
 * fully-qualified names are present, a comma-separated list
 * of fully-qualified class names is returned.
 * 
 * @param name the non-qualified name of the class to be searched
 * @return the qualified name, if exists.
 */
public static String getQualifiedName(String name)
{
	return (String) map.get(name);
}

//--------------------------------------------------------------------------
//Private static methods
//--------------------------------------------------------------------------
	
/**
 * Finds all the classes reachable from the current classpath;
 * for each of them, inserts an association (name, fully-qualified 
 * name) in the specified map. Both names are String objects.
 * 
 * Only classes reachable from the classpath defined by the 
 * "java.class.path" property are considered. 
 * Jar files and directories are both parsed.
 * If multiple classes with the same name but different 
 * fully-qualified names are present, they are inserted
 * in the map as associations (name, comma-separated list of
 * fully-qualified names).
 * 
 * @param map
 * @throws IOException
 */
private static void findClasses(Map map)
throws IOException
{
  String classPath = System.getProperty( "java.class.path" );
  String separator = System.getProperty( "path.separator"  );
  StringTokenizer path = new StringTokenizer( classPath, separator );

  while( path.hasMoreTokens() ) {
  	
    String pathElement = path.nextToken();
    File pathFile = new File( pathElement );
    
    if( pathFile.isDirectory() ) {
    	if (!pathElement.endsWith("/")) {
    		pathElement = pathElement + "/";
    		pathFile = new File( pathElement);
    	}
      findClassInPathDir( map, pathElement, pathFile );
    	// Search directories
    } else if ( pathFile.exists() ) {
    	findClassInJar( map, pathFile);
    }
  }
}

/**
 * Parses jar file.
 * 
 * @param map the map where to insert associations
 * @param pathFile the file name of the associated jar file
 * @throws IOException
 */
private static void findClassInJar(Map map, File pathFile)
throws IOException
{
  ZipFile zipFile = new ZipFile( pathFile );
  Enumeration entries = zipFile.entries();
  while( entries.hasMoreElements() ) {
  	
    String entry = entries.nextElement().toString();
    if( entry.endsWith( ".class" ) ) {
      String className = classname( entry );
      String shortName = getShortName( className );
      if (map.containsKey(shortName)) {
      	map.put(shortName, map.get(shortName)+","+className);
      } else {
      	map.put(shortName, className);
      }
    }
   
  }
}

/**
 * Recursively parses directories.
 * 
 * @param map the map where to insert associations
 * @param pathElement the path string used for recursion
 * @param pathFile the file (directory) to be analyzed
 * @throws IOException
 */
private static void findClassInPathDir( Map map, String pathElement, File pathFile ) 
	throws IOException
{
  String[] list = pathFile.list();

  for( int i = 0; i < list.length; i++ ) {
    File file = new File( pathFile, list[i] );
    if( file.isDirectory() ) {
      findClassInPathDir( map, pathElement, file );
    }
	  else if ( file.exists() && (file.length() != 0) && list[i].endsWith( ".class" ) ) {
	    String classFile = file.toString().substring( pathElement.length());
	    String className = classname( classFile );
      String shortName = getShortName( className );
      if (map.containsKey(shortName)) {
      	map.put(shortName, map.get(shortName)+","+className);
      } else {
      	map.put(shortName, className);
      }
	  }
  }
}

/**
 * Translates a class file name in a class name.
 */
private static String classname(String classFile)
{ 
  return classFile.replace( '/', '.' ).substring( 0, classFile.length() - ".class".length() ); 
}

/** 
 * Testing.
 * 
 * @param argv
 * @throws IOException
 */
public static void main( String[] argv )
throws IOException
{
	Iterator i = map.keySet().iterator();
	while (i.hasNext()) {
		String key = (String) i.next();
		String name = (String) map.get(key);
		System.out.println(key + " --> " + name);
	}
}
  
}
