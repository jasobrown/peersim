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

package peersim.cdsim;

import peersim.config.*;
import peersim.core.*;
import peersim.reports.*;
import peersim.util.*;
import peersim.dynamics.*;
import java.util.Arrays;
import java.io.*;

/**
 * This is the executable class for performing a cycle driven simulation. The
 * class is completely static as at the same time we expect to have only one
 * simulation running in a virtual machine. The simulation is highly
 * configurable.
 */
public class RangeSimulator
{

////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////
/**
 *  
 */
public static final String CONFIG_EXTENSIONS = ".cfg";

////////////////////////////////////////////////////////////////////////////
// Configuration parameters
////////////////////////////////////////////////////////////////////////////
/**
 * This config property define the parameters that defines separate experiments
 * and the corresponding values. For each value, a different result file will
 * be produced.
 */
public static final String PAR_CONCURRENT = "range.concurrent";

/**
 * This config property defines the parameters that vary during a single
 * experiment and the corresponding values.
 */
public static final String PAR_RANGE = "range";

/**
 * This config property defines the number of repetitions that will be executed
 * of the same experiment.
 */
public static final String PAR_EXPS = "simulation.experiments";

/**
 * This config property defines the number of cycles to complete
 */
public static final String PAR_CYCLES = "simulation.cycles";

/**
 * If set, it means the order of visiting each node is shuffled in each cycle
 */
public static final String PAR_SHUFFLE = "simulation.shuffle";

/**
 * This is the prefix for network initializers. These have to be of
 * type {@link Dynamics}.
 */
public static final String PAR_INIT = "init";

/**
 * This is the prefix for network dynamism managers. These have to be of type
 * {@link Dynamics}.
 */
public static final String PAR_DYN = "dynamics";

/**
 * This is the prefix for network observers. These have to be of type
 * {@link Observer}.
 */
public static final String PAR_OBS = "observer";

/**
 * This parameter can be added to protocol specifications to avoid
 * the execution of the protocol. 
 */
public static final String PAR_IDLE = "idle";

////////////////////////////////////////////////////////////////////////////
// Static fields
////////////////////////////////////////////////////////////////////////////
/** holds the observer of this simulation */
private static Observer[] observers = null;

/** holds the observer of this simulation */
private static Dynamics[] dynamics = null;

/** Holds the observer schedulers of this simulation */
private static Scheduler[] obsSchedules = null;

/** Holds the dynamics schedulers of this simulation */
private static Scheduler[] dynSchedules = null;

// XXX it would be possible to schedule protocols too the same way

/** Idle array */
private static boolean[] idle;

////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////
/**
 * Main method of the system.
 */
public static void main(String[] args) throws IOException
{
	// Check if there are no arguments or there is an explicit --help
	// flag; if so, print the usage of the class
	if (args.length == 0 || args[0].equals("--help")) {
		usage();
		System.exit(1);
	}
	// Read property file and set up output files, if needed
	System.err.println("Simulator: loading configuration");
	ConfigProperties properties = null;
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
		properties = new ConfigProperties(args);
		Configuration.setConfig(properties);
	}
	// Executes experiments; report short messages about exceptions that are
	// handled by the configuration mechanism.
	try {
		doExperiments(properties);
	} catch (MissingParameterException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	} catch (IllegalParameterException e) {
		System.err.println(e.getMessage());
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
private static ConfigProperties selectConfigFile(File configdir, File resultdir)
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
	ConfigProperties properties = null;
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
private static ConfigProperties checkFile(File config, File resultdir)
{
	ConfigProperties properties;
	// Read config files; skip the file if not readable for any reason.
	try {
		properties = new ConfigProperties(config.getAbsolutePath());
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
		double[][] values = new double[ranges.length][];
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
private static void parseRanges(String[] ranges, String pars[], double[][] values, String name)
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
private static void nextValues(int[] idx, double[][] values)
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
public static void doExperiments(ConfigProperties properties)
{
	// Parsing simulation parameters
	int cycles = Configuration.getInt(PAR_CYCLES);
	boolean shuffle = Configuration.contains(PAR_SHUFFLE);
	// Parse range parameters
	String[] pars;
	double[][] values;
	String[] ranges = Configuration.getNames(PAR_RANGE);
	if (Configuration.contains(PAR_EXPS) || ranges.length == 0) {
		// If there is an explicit experiment or there are no ranges
		pars = new String[ranges.length + 1];
		values = new double[ranges.length + 1][];
		pars[ranges.length] = "EXP";
		values[ranges.length] = StringListParser.parseList("1:" + Configuration.getInt(PAR_EXPS, 1));
	} else {
		pars = new String[ranges.length];
		values = new double[ranges.length][];
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
		// Perform simulation
		System.err.println(log);
		// This is necessary, because if the initialization caused
		// by Network.reset()
		CommonState.setT(0);
		Network.reset();
		System.gc();
		loadObservers();
		loadDynamics();
		// Read idle parameters
		// We assume here that the network has at least one node
		idle = new boolean[Network.get(0).protocolSize()];
		for (int i = 0; i < idle.length; i++) {
			String protpar = GeneralNode.PAR_PROT + "." + i + "." + PAR_IDLE;
			idle[i] = Configuration.contains(protpar) || !(Network.get(0).getProtocol(i) instanceof CDProtocol);
			if (idle[i])
				System.out.println("Protocol " + i + " is idle");
		}
		performSimulation(cycles, shuffle);
		// Increment values
		nextValues(idx, values);
		System.gc();
	}
	if (Configuration.contains("__x"))
		Network.test();
}

//--------------------------------------------------------------------
private static void loadObservers()
{
	// load analizers
	String[] names = Configuration.getNames(PAR_OBS);
	observers = new Observer[names.length];
	obsSchedules = new Scheduler[names.length];
	for (int i = 0; i < names.length; ++i) {
		observers[i] = (Observer) Configuration.getInstance(names[i]);
		obsSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded observers " + Arrays.asList(names));
}

//--------------------------------------------------------------------
private static void loadDynamics()
{
	// load dynamism managers
	String[] names = Configuration.getNames(PAR_DYN);
	dynamics = new Dynamics[names.length];
	dynSchedules = new Scheduler[names.length];
	for (int i = 0; i < names.length; ++i) {
		dynamics[i] = (Dynamics) Configuration.getInstance(names[i]);
		dynSchedules[i] = new Scheduler(names[i]);
	}
	System.err.println("Simulator: loaded modifiers " + Arrays.asList(names));
}

//--------------------------------------------------------------------
protected static void runInitializers()
{
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);

	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " + i + ": " + inits[i].getClass());
		((Dynamics)inits[i]).modify();
	}
}

// --------------------------------------------------------------------
private static void performSimulation(int cycles, boolean shuffle)
{
	// initialization
	System.err.println("Simulator: running initializers");
	CommonState.setT(0); // needed here
	runInitializers();
	// main cycle
	System.err.println("Simulator: starting simulation");
	CommonState.setT(0); // needed here
	for(int i=0; i<cycles; ++i)
	{
		CommonState.setT(i);
		// dynamism
		for (int j = 0; j < dynamics.length; ++j) {
			if (dynSchedules[j].active(i))
				dynamics[j].modify();
		}
		// analizer
		boolean stop = false;
		for (int j = 0; j < observers.length; ++j) {
			if (obsSchedules[j].active(i))
				stop = stop || observers[j].analyze();
		}
		if (stop)
			break;
		// do one round
		nextRound(shuffle);
		System.err.println("Simulator: cycle " + i + " done");
	}
	// analysis after the simulation
	for (int j = 0; j < observers.length; ++j) {
		if (obsSchedules[j].fin())
			observers[j].analyze();
	}
}

// --------------------------------------------------------------------
protected static void nextRound(boolean shuffle)
{
	if (shuffle) {
		Network.shuffle();
	}
	for (int j = 0; j < Network.size(); ++j) {
		Node node = Network.get(j);
		CommonState.setNode(node);
		int len = node.protocolSize();
		// XXX maybe should use different shuffle for each protocol?
		// (instead of running all on one node at the same time?)
		for (int k = 0; k < len; ++k) {
			CommonState.setPid(k);
			Protocol protocol = node.getProtocol(k);
			if (!idle[k])
				((CDProtocol) protocol).nextCycle(node, k);
		}
	}
}

//--------------------------------------------------------------------
private static void usage() {
	System.err.println("Usage:");
	System.err.println("  RangeSimulator -d <configdir> [<resultdir>]");
	System.err.println("  RangeSimulator <configfile> <property>*");
}
}
