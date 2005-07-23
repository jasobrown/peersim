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

import java.util.Properties;
import java.io.*;

/**
* Class for handling configuration files. Extends the functionality
* of Properties by handling files, system resources and command lines.
*/
public class ExtendedConfigProperties extends Properties {


// =========== Public Constructors ===================================
// ===================================================================


/**
* Calls the Properties onstructor.
*/
public ExtendedConfigProperties() { super(); }

// -------------------------------------------------------------------

/**
* Constructs a ConfigProperty object from a parameter list.
* The algorithm is as follows: first <code>resource</code> is used to attempt
* loading default values from the give system resource.
* Then pars[0] is tried if it is an
* existing filename. If it is, reading properties from that file is
* attempted. Then (if pars[0] was a filename then from index 0 otherwise
* from index 1) pars is loaded as if it was a command line argument list
* using <code>loadCommandLineDefs</code>.
*
* A little inconvinience is that if pars[0] is supposed to be the first
* command line argument but it is a valid filename at the same time by
* accident. The caller must take care of that.
*
* No exceptions are thrown, instead error messages are written to the
* standard error. Users who want a finer control should use
* the public methods of this class.
*
* @param pars The (probably command line) parameter list.
* @param resource The name of the system resource that contains the
* defaults. null if there isn't any.
* 
*/
public	ExtendedConfigProperties( String[] pars, String resource ) {
	
	try
	{
		if( resource != null )
		{
			loadSystemResource(resource);
			System.err.println("ConfigProperties: System resource "
			+resource+" loaded.");
		}
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}
	
	if( pars == null || pars.length == 0 ) return;
	
	try
	{
		load( pars[0] );
		System.err.println(
			"ConfigProperties: File "+pars[0]+" loaded.");
		pars[0] = "";
	}
	catch( IOException e )
	{
		System.err.println("ConfigProperties: Failed loading '"+pars[0]
		+"' as a file, interpreting it as a property.");
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}

	if( pars.length==1 && pars[0].length()==0 ) return;
	
	try
	{
		loadCommandLineDefs( pars );
		System.err.println(
			"ConfigProperties: Command line defs loaded.");
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}
}

// -------------------------------------------------------------------

/**
* Constructs a ConfigProperty object by loading a file.
* @param fileName The name of the configuration file.
*/
public ExtendedConfigProperties( String fileName ) throws IOException {

	load( fileName );
}

// -------------------------------------------------------------------

/**
* Calls the Properties constructor.
*/
public	ExtendedConfigProperties( Properties props ) {

	super( props );
}

// -------------------------------------------------------------------

/**
* Calls the contructor with resource set to null.
*/
public	ExtendedConfigProperties( String[] pars ) {

	this( pars, null );
}


// =========== Public methods ========================================
// ===================================================================


/**
* Loads given file. 
*/
public void load( String fileName ) throws IOException {

	BufferedReader f = 
		new BufferedReader(new FileReader( fileName ));
	int lines = 0;
	int pars = parseStream(f, "", 0, lines);
	if (pars == 1) {
		System.err.println("One closing bracket ('}') is missing");
		System.exit(1);
  } else if (pars > 1) {
		System.err.println(pars + " closing brackets ('}') are missing");
		System.exit(1);
  }

	f.close();
}

private int parseStream(BufferedReader f, String prefix, int pars, int lines)
  throws IOException
{
	boolean eof = false;
	boolean complete = true;
	String part;
	String line = "";
	String last = "";
	while ((part = f.readLine()) != null) {
		lines++;
		
		// Reset line
		if (complete)
			line = "";
		
		// Check if the line is a comment line
		// If so, remove the comment
		int index = part.indexOf('#');
		if (index >= 0) {
			part = part.substring(0, index);
		} 

		// Check if the line is empty
		part = part.trim();
		if ("".equals(part))
			continue;

		complete = (part.charAt(part.length()-1) != '\\'); 
		if (!complete) {  
			line = line + part.substring(0, part.length()-2) + " ";
			continue;
		}
		
		// Complete the line
		line = line + part;
		if (line.equals("{")) {
			pars = parseStream(f, last+".", pars+1, lines);
		} else if (line.equals("}")) {
			if (pars == 0) {
				System.err.println("Additional } at line " + lines + 
						" when parsing the configuration file");
				System.exit(1);
			}
			return pars-1;
		} else {
			// Search the first token
			String[] tokens = line.split("[\\s:=]+", 2);
			if (tokens.length == 1) {
				setProperty(prefix+tokens[0], "");
				//System.out.println(prefix + tokens[0] +" -> ");
			} else {
				setProperty(prefix+tokens[0], tokens[1]);
				//System.out.println(prefix + tokens[0] +" -> " + tokens[1]);
			}
			last = prefix + tokens[0];
		}
	}
	return pars;
}

// -------------------------------------------------------------------

/**
* Adds the properties from the given property file. Searches in the class path
* for the file with the given name.
*/
public void loadSystemResource( String n ) throws IOException {
	
	ClassLoader cl = getClass().getClassLoader();
	load( cl.getResourceAsStream( n ) );
}

// -------------------------------------------------------------------


/**
* Appends properties defined in the given command line arg list.
* Every string in the array is considered as a property file line.
* The strings are converted to byte arrays according to the
* default character encoding and then the properties are loaded by the
* <code>Properties.load</code> method. This means that the ISO-8859-1
* (or compatible) encoding is assumed.
*/
public void loadCommandLineDefs( String[] cl ) throws IOException {

	StringBuffer sb = new StringBuffer();
	for(int i=0; i<cl.length; ++i) sb.append( cl[i] ).append( "\n" );
	load( new ByteArrayInputStream(sb.toString().getBytes()) );
}

public static void main(String[] args)
{
	Properties prop = new ExtendedConfigProperties(args);
}
}

