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

package peersim;

import java.io.*;
import java.util.*;
import peersim.config.*;
import peersim.edsim.*;
import peersim.util.*;
import peersim.core.Log;

/**
 * This is the executable class for performing a cycle driven simulation. The
 * class is completely static as at the same time we expect to have only one
 * simulation running in a virtual machine. The simulation is highly
 * configurable.
 */
public class RangeSimulator extends Simulator
{

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/**
 *  
 */
public static final String CONFIG_EXTENSIONS = ".cfg";

//--------------------------------------------------------------------------
// Configuration parameters
//--------------------------------------------------------------------------

/**
 * This config property define the parameters that defines separate experiments
 * and the corresponding values. For each value, a different result file will
 * be produced.
 * @config
 */
private static final String PAR_CONCURRENT = "range.concurrent";

/**
 * This config property defines the parameters that vary during a single
 * experiment and the corresponding values.
 * @config
 */
private static final String PAR_RANGE = "range";


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Main method of the system.
 */
public static void main(String[] args)
{
	// Check if there are no arguments or there is an explicit --help
	// flag; if so, print the usage of the class
	if (args.length == 0 || args[0].equals("--help")) {
		usage();
		System.exit(1);
	}
	// Read property file and set up output files, if needed
	System.err.println("Simulator: loading configuration");
	Properties properties = null;
	if (args[0].equals("-d")) {
		// The simulator is used to automatically select a configuration
		// file contained in the specified directory and that has not been
		// executed yet.
		if (args.length != 2 && args.length != 3) {
			usage();
			System.exit(1);
		}
		File configdir = new File(args[1]).getAbsoluteFile();
		File resultdir = new File((args.length == 3 ? args[2] : args[1])).getAbsoluteFile();
		properties = selectConfigFile(configdir, resultdir);
		Configuration.setConfig(properties);
	} else {
		// The simulator is use to execute a single file, with the possibility
		// to define properties on the command line.
		properties = new ParsedProperties(args);
		Configuration.setConfig(properties);
	}
	// Executes experiments; report short messages about exceptions that are
	// handled by the configuration mechanism.
	try {
		doExperiments(properties);
	} catch (MissingParameterException e) {
		System.err.println(e+"");
		System.exit(1);
	} catch (IllegalParameterException e) {
		System.err.println(e+"");
		System.exit(1);
	}
	System.exit(0);
}

//--------------------------------------------------------------------
/**
 * Search in the configuration directory a configuration file to be executed.
 * If a configuration file can be executed concurrently, search if any of its
 * concurrent experiment has to be executed yet.
 * 
 * @param configdir
 *          the directory containing configuration files to be executed
 * @param resultdir
 *          the directory where result files are written
 */
private static Properties selectConfigFile(File configdir, File resultdir)
{
	// Verify configuration directory
	if (!configdir.isDirectory()) {
		System.err.println("Error: " + configdir + " is not a directory");
		System.exit(1);
	}
	if (!resultdir.isDirectory()) {
		System.err.println("Error: " + resultdir + " is not a directory");
		System.exit(1);
	}
	// Search file not executed yet in config dir
	File[] files = configdir.listFiles();
	Arrays.sort(files);
	Properties properties = null;
	for (int i = 0; i < files.length && properties == null; i++) {
		String name = files[i].getName();
		if (name.endsWith(CONFIG_EXTENSIONS)) {
			properties = checkFile(files[i], resultdir);
		}
	}
	if (properties == null) {
		System.err.println("Warning: nothing to do, terminating");
		System.exit(2);
	}
	return properties;
}

//--------------------------------------------------------------------

/**
 * Test a config file; read its content in order to verify whether it contains
 * concurrent experiments. If so, it test whether any of them has to be
 * executed. If not, test if the entire file has to be executed.
 * 
 * @param config
 *          the config file to be tested
 * @param resultdir
 *          the result directory
 * @return true if a file has been found, false otherwise
 */
private static Properties checkFile(File config, File resultdir)
{
	Properties properties;
	// Read config files; skip the file if not readable for any reason.
	try {
		properties = new ParsedProperties(config.getAbsolutePath());
		Configuration.setConfig(properties);
	} catch (IOException e) {
		System.err.println("Warning: File " + config + " cannot be open");
		System.err.println(e.getMessage());
		return null;
	}
	File dest = null;
	boolean isnew = false;
	String[] ranges = Configuration.getNames(PAR_CONCURRENT);
	if (ranges.length == 0) {
		// There are no concurrent ranges; we just check if the result
		// file exists; if not, we select this as the configuration file
		// Compute the name of the destination file
		dest = new File(resultdir, config.getName().substring(0, config.getName().length() - 4) + ".txt");
		// Try to create the destination file; creation is atomic
		try {
			isnew = dest.createNewFile();
		} catch (IOException e) {
			System.err.println("Warning: file " + dest + " cannot be created");
			System.err.println(e.getMessage());
			return null;
		}
	} else {
		// There are concurrent ranges; we search for an experiment that has not
		// been executed yet.
		// Obtains values denoting concurrent experiments
		String[] pars = new String[ranges.length];
		String[][] values = new String[ranges.length][];
		parseRanges(ranges, pars, values, PAR_CONCURRENT);
		// Search through experiments
		int[] idx = new int[values.length]; // Initialized to 0
		while (idx[0] < values[0].length && !isnew) {
			// Compute the name of the destination file
			StringBuffer buffer = new StringBuffer(config.getName().substring(0, config.getName().length() - 4));
			for (int j = 0; j < values.length; j++) {
				properties.setProperty(pars[j], "" + values[j][idx[j]]);
				buffer.append("." + values[j][idx[j]]);
			}
			buffer.append(".txt");
			dest = new File(resultdir, buffer.toString());
			// Try to create the destination file; creation is atomic
			try {
				isnew = dest.createNewFile();
			} catch (IOException e) {
				System.err.println("Warning: file " + dest + " cannot be created");
				System.err.println(e.getMessage());
				return null;
			}
			// Select next experiment
			nextValues(idx, values);
		}
	}
	if (isnew) {
		// Redirecting standard output
		try {
			PrintStream stream = new PrintStream(new FileOutputStream(dest));
			Log.setStream(stream);
		} catch (FileNotFoundException e) {
			// Not possible
		}
		return properties;
	} else {
		return null;
	}
}

//--------------------------------------------------------------------

/**
 * Parses a collection of range specifications and returns the set of parameter
 * that will change during the simulation and the values that will be used for
 * those parameters.
 * 
 * @param ranges
 *          the range specifications to be parsed
 * @param pars
 *          the parameters that will change during the simulation
 * @param values
 *          the values that will be used for those parameters
 * @param name
 *          the name of the property describing ranges
 */
private static void parseRanges(String[] ranges, String pars[], String[][] values, String name)
{
	for (int i = 0; i < ranges.length; i++) {
		String[] array = Configuration.getString(ranges[i]).split(";");
		if (array.length != 2) {
			throw new IllegalParameterException(name + "." + i, " should be formatted as <parameter>;<list>");
		}
		pars[i] = array[0];
		values[i] = StringListParser.parseList(array[1]);
	}
}

//--------------------------------------------------------------------

/**
 * Selects the next set of values by incrementing the specified index array.
 * The index array is treated as a vector of digits; the first is managed
 * managed as a vector of digits.
 */
private static void nextValues(int[] idx, String[][] values)
{
	idx[idx.length - 1]++;
	for (int j = idx.length - 1; j > 0; j--) {
		if (idx[j] == values[j].length) {
			idx[j] = 0;
			idx[j - 1]++;
		}
	}
}

//--------------------------------------------------------------------

public static void doExperiments(Properties properties)
{
	// Parsing simulation parameters
	// Parse range parameters
	String[] pars;
	String[][] values;
	String[] ranges = Configuration.getNames(PAR_RANGE);
	if (Configuration.contains(PAR_EXPS) || ranges.length == 0) {
		// If there is an explicit experiment or there are no ranges
		pars = new String[ranges.length + 1];
		values = new String[ranges.length + 1][];
		pars[ranges.length] = "EXP";
		values[ranges.length] = StringListParser.parseList("1:" + Configuration.getInt(PAR_EXPS, 1));
	} else {
		pars = new String[ranges.length];
		values = new String[ranges.length][];
	}
	parseRanges(ranges, pars, values, PAR_RANGE);
	// Execute with different values
	int[] idx = new int[values.length]; // Initialized to 0
	while (idx[0] < values[0].length) {
		// Set values in the property buffer and create log string
		StringBuffer log = new StringBuffer();
		for (int j = 0; j < values.length; j++) {
			properties.setProperty(pars[j], "" + values[j][idx[j]]);
			log.append(pars[j]);
			log.append(" ");
			log.append(values[j][idx[j]]);
			log.append(" ");
		}
		Log.setPrefix(log.toString());
		final int simid = getSimID();
		System.err.print("Starting simulation " + log);
		System.err.println(" invoking "+simName[simid]);
		System.out.println("\n\n");

		// Perform simulation
		if (simid == UNKNOWN) {
			System.err.println(
			    "Simulator: unable to identify configuration, exiting.");
			return;
		}
		
		// XXX could be done through reflection, but
		// this is easier to read.
		switch(simid) {
			case CDSIM:
			     	peersim.cdsim.Simulator.nextExperiment();
				break;
			case EDSIM:
				EDSimulator.nextExperiment();
				break;
	  }

		// Increment values
		nextValues(idx, values);

		// Help garbage collection; not sure it is useful.
		System.gc();
	}
}

//--------------------------------------------------------------------

private static void usage() {
	System.err.println("Usage:");
	System.err.println("  RangeSimulator -d <configdir> [<resultdir>]");
	System.err.println("  RangeSimulator <configfile> <property>*");
}

}
