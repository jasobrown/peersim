/*
 * Copyright (c) 2010 Alberto Montresor
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

package peersim.extras.am.reports;

import java.io.*;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.vector.*;

/**
 * 
 */
public class MultipleVectorDumper implements Control
{

// --------------------------------------------------------------------------
// Parameter names
// --------------------------------------------------------------------------

/**
 * The protocol to operate on.
 * @config
 */
protected static final String PAR_PROT = "protocol";

/**
 * The getter method used to obtain the protocol values. Defaults to
 * <code>getValue</code> (for backward compatibility with previous
 * implementation of this class, that were based on the {@link SingleValue}
 * interface). Refer to the {@linkplain peersim.vector vector package
 * description} for more information about getters and setters.
 * @config
 */
protected static final String PAR_GETTER = "getter";

/**
 * This is the base name of the file where the values are saved. The full
 * name will be baseName+cycleid+".vec".
 * @config
 */
private static final String PAR_BASENAME = "outf";

// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** The getter to be used to read a vector. */
private final Getter[] getters;

/** Prefix name of this observer */
private final String prefix;

/** Base name of the file to be written */
private final String baseName;

private final FileNameGenerator fng;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters. Invoked by
 * the simulation engine.
 * @param prefix
 *          the configuration prefix for this class
 */
public MultipleVectorDumper(String prefix)
{
	this.prefix = prefix;
	String par_getter = Configuration.getString(prefix + "." + PAR_GETTER);
	String par_prot = Configuration.getString(prefix + "." + PAR_PROT);
	String[] names = par_getter.split(",");
	String[] protocols = par_prot.split(",");

	if (names.length != protocols.length) {
		throw new IllegalParameterException(prefix + "." + PAR_GETTER,
				"The number of protocols and getters is different");
	}

	getters = new Getter[names.length];
	for (int i = 0; i < getters.length; i++) {
		getters[i] = new Getter(prefix, protocols[i], names[i]);
	}
	baseName = Configuration.getString(prefix + "." + PAR_BASENAME, null);
	if (baseName != null)
		fng = new FileNameGenerator(baseName, ".vec");
	else
		fng = null;
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * Prints statistics information about a vector. Provided statistics
 * include average, max, min, variance, etc. Values are printed according
 * to the string format of {@link IncrementalStats#toString}.
 * @return always false
 */
public boolean execute()
{
	try {

		System.out.println(prefix + ": ");

		// initialize output streams
		PrintStream pstr = System.out; // Default
		if (baseName != null) {
			String filename = fng.nextCounterName();
			System.out.println("writing " + filename);
			pstr = new PrintStream(new FileOutputStream(filename));
		}
		for (int i = 0; i < Network.size(); ++i) {
			for (int j = 0; j < getters.length - 1; j++) {
				pstr.print(getters[j].get(i) + " ");
			}
			pstr.println(getters[getters.length - 1].get(i));
		}
		if (pstr != System.out) 
			pstr.close();
	} catch (IOException e) {
		throw new RuntimeException(prefix + ": Unable to write to file: " + e);
	}

	return false;

}
}
