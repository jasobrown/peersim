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

import peersim.core.*;
import peersim.config.*;
import peersim.util.FileNameGenerator;
import java.io.*;
import java.lang.reflect.*;

/**
 * Dump the content of one or more protocol vectors in a file. Each line
 * represent a single node, with values from different protocols separated 
 * by spaces. Values are dumped to a file whose name is obtained from a
 * configurable prefix, a number specifying the current simulation time,
 * and the extension ".vec".
 * <p>
 * This observer class can observe any protocol field containing a 
 * primitive value, provided that the field is associated with a getter method 
 * that reads it.
 * The methods to be used are specified through parameter {@value #PAR_METHODS}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#getValue()} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class ValueDumper implements Control
{

// --------------------------------------------------------------------------
// Parameter names
// --------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
private static final String PAR_PROT = "protocol";

/**
 * This is the base name of the file where the values are saved. The full name
 * will be baseName+cycleid+".vec".
 * @config
 */
private static final String PAR_BASENAME = "outf";

/**
 * The getter method(s) used to get values from the protocol instances.
 * Multiple methods can be specified, separated with spaces.
 * Defauls to "getValue" (for backward compatibility with previous 
 * implementation of this class, that were based on the 
 * {@link SingleValue} interface.
 * Refer to the {@linkplain peersim.vector vector package description} for more 
 * information about getters and setters.
 * @config
 */
private static final String PAR_METHODS = "getter";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Prefix name of this observer */
private final String prefix;

/** Protocol identifier */
private final int pid;

/** Base name of the file to be written */
private final String baseName;

private final FileNameGenerator fng;

/** Methods name */
private final String[] methodNames;

/** Methods */
private final Method[] methods;

// --------------------------------------------------------------------------
// Constructor
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public ValueDumper(String prefix)
{
	this.prefix = prefix;
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	baseName = Configuration.getString(prefix + "." + PAR_BASENAME, null);
	if(baseName!=null) fng = new FileNameGenerator(baseName,".vec");
	else fng = null;
	
	String value = Configuration.getString(
		prefix + "." + PAR_METHODS, "getValue");
	methodNames = value.split("\\s");
	// Search the methods
	Class clazz = Network.prototype.getProtocol(pid).getClass();
	methods = new Method[methodNames.length];
	for (int i = 0; i < methodNames.length; i++)
	{
		try 
		{
			methods[i] = GetterSetterFinder.getGetterMethod(
			clazz,methodNames[i]);
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalParameterException(prefix + "." +
				PAR_METHODS, e+"");
		}
	}
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public boolean execute() {
try
{
	System.out.print(prefix + ": ");
	// initialize output streams
	PrintStream pstr = System.out;
	if (baseName != null)
	{
		String filename = fng.nextCounterName();
		System.out.println("writing "+filename);
		pstr = new PrintStream(new FileOutputStream(filename));
	}
	else	System.out.println();
	for (int i = 0; i < Network.size(); ++i)
	{
		for (int j = 0; j < methods.length; j++)
			pstr.print(methods[j].invoke(
			Network.get(i).getProtocol(pid)) + " ");
		pstr.println();
	}
}
catch (InvocationTargetException e)
{
	e.getTargetException().printStackTrace();
	System.exit(1);
}
catch (IOException e)
{
	System.err.println(prefix + ": Unable to write to file: " + e);
	return true;
}
catch (Exception e)
{
	throw new RuntimeException(e);
}

	return false;
}

// ---------------------------------------------------------------------

}
