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

import java.io.*;

/**
* Extends the class {@link ConfigProperties} with basic parsing capabilities.
* @see #load
*/
public class ParsedProperties extends ConfigProperties {


// ================= initialization =================================
// ==================================================================

/**
* Calls super constructor.
* @see ConfigProperties#ConfigProperties(String[])
*/
public	ParsedProperties( String[] pars ) {

	super( pars );
}

// ------------------------------------------------------------------

/**
* Calls super constructor.
* @see ConfigProperties#ConfigProperties(String)
*/
public	ParsedProperties( String filename ) throws IOException {

	super( filename );
}


// =========== Public methods ========================================
// ===================================================================


/**
* Loads given file. It works exactly as <code>Properties.load</code>
* with a file input stream to the given file, except that the file is parsed
* the following way allowing to compress some property names
* using <code>{</code> and <code>}</code>: the construct
  <pre>
  property value
  {
  foo value1
  bar value2
  }
  </pre>
  is equivalent to
  <pre>
  property value
  property.foo value1
  property.bar value2
  </pre>
  This mechanism can be nested.
  The <code>{</code> and <code>}</code> must be the only non-whitespace
  character in its line.
*/
public void load( String fileName ) throws IOException {

	BufferedReader f = 
		new BufferedReader(new FileReader( fileName ));
	int lines = 0;
	int pars = parseStream(f, "", 0, lines);
	if (pars == 1)
	{
		System.err.println("One closing bracket ('}') is missing");
		System.exit(1);
	} 
	else if (pars > 1)
	{
		System.err.println(pars+" closing brackets ('}') are missing");
		System.exit(1);
	}

	f.close();
}

// --------------------------------------------------------------------

private int parseStream(BufferedReader f, String prefix, int pars, int lines)
throws IOException {

	boolean eof = false;
	boolean complete = true;
	String part;
	String line = "";
	String last = "";
	while ((part = f.readLine()) != null)
	{
		lines++;
		
		// Reset line
		if (complete) line = "";
		
		// Check if the line is a comment line
		// If so, remove the comment
		int index = part.indexOf('#');
		if (index >= 0)
		{
			part = part.substring(0, index);
		} 

		// Check if the line is empty
		part = part.trim();
		if ("".equals(part)) continue;

		complete = (part.charAt(part.length()-1) != '\\'); 
		if (!complete)
		{  
			line = line + part.substring(0, part.length()-2) + " ";
			continue;
		}
		
		// Complete the line
		line = line + part;
		if (line.equals("{"))
		{
			pars = parseStream(f, last+".", pars+1, lines);
		} 
		else if (line.equals("}"))
		{
			if (pars == 0)
			{
				System.err.println(
					"Additional } at line " + lines + 
					" when parsing the configuration file");
				System.exit(1);
			}
			return pars-1;
		}
		else
		{
			// Search the first token
			String[] tokens = line.split("[\\s:=]+", 2);
			if (tokens.length == 1)
			{
				setProperty(prefix+tokens[0], "");
				//System.out.println(prefix+tokens[0]+" -> ");
			}
			else
			{
				setProperty(prefix+tokens[0], tokens[1]);
				//System.out.println(prefix 
				//+ tokens[0] +" -> " + tokens[1]);
			}
			last = prefix + tokens[0];
		}
	}
	return pars;
}

// --------------------------------------------------------------------

/** for testing */
public static void main(String[] args)
{
	java.util.Properties prop = new ParsedProperties(args);
}
}

