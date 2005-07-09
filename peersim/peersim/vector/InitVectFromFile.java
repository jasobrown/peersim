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

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.StringTokenizer;

/**
 * Initializes a protocol vector from data read from a file. The file has to
 * contain one value per line. Lines starting with # or lines that contain only
 * whitespace are ignored. The file can contain more values than necessary but
 * enough values must be present.
 * <p>
 * This dynamics class can initialize any protocol field containing a 
 * primitive value, provided that the field is associated with a setter method 
 * that modifies it.
 * The method to be used is specified through parameter {@value #PAR_METHOD}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#setValue(double)} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class InitVectFromFile extends VectDynamics
{

// --------------------------------------------------------------------------
// Parameter names
// --------------------------------------------------------------------------

/**
 * The filename to load links from.
 * @config
 */
public static final String PAR_FILE = "file";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** The file to be read */
private final String file;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * @param prefix
 *          the configuration prefix for this class
 */
public InitVectFromFile(String prefix)
{
	super(prefix);
	file = Configuration.getString(prefix + "." + PAR_FILE);
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * Initializes values from a file. The file has to contain one value per line.
 * Lines starting with # or lines that contain only whitespace are ignored. The
 * file can contain more values than necessary but enough values must be
 * present.
 */
public void modify() {

	int i = 0;

try {
	FileReader fr = new FileReader(file);
	LineNumberReader lnr = new LineNumberReader(fr);
	String line;
	while ((line = lnr.readLine()) != null && i < Network.size()) {
		if (line.startsWith("#"))
			continue;
		StringTokenizer st = new StringTokenizer(line);
		if (!st.hasMoreTokens())
			continue;
		if( type==int.class || type==long.class )
			set(i,Long.parseLong(st.nextToken()));
		else	set(i,Double.parseDouble(st.nextToken()));
		i++;
	}
}
catch(Exception e) { throw new RuntimeException(e); }
	
	if (i < Network.size())
		throw new RuntimeException(
		"Too few values in file '" + file + "' (only "
		+ i + "); we need " + Network.size() + ".");
}

// --------------------------------------------------------------------------

}
