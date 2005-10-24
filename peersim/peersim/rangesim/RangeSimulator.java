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

package peersim.rangesim;

import java.io.*;
import java.util.*;

import peersim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
 * This class is the main class for the Range Simulator. A range is
 * a collection of values <em>S</em> to be assigned to a variable 
 * <em>v</em>. The Range Simulator invokes the standard Peersim 
 * simulator once for each distinct value. If multiple ranges 
 * <em>S1, S2, ..., Sn</em> are specified, the standard Peersim
 * simulator is invoked for each element in 
 * <em>S1 * S2 * ... * Sn</em>.  
 * <p>  
 * Ranges are specified with the following syntax:
<pre>
range.[id] [var];[range]
</pre>
 * where:
 * <UL>
 * <LI> {@value #PAR_RANGE} is the prefix for all range
 * specifications;</LI>
 * <LI> <code>id</code> is an identifier; since they are not referred anywhere else,
 * consecutive numbers are a good choice for range identifiers;</LI>
 * <LI> <code>var</code> is a variable parameter </LI>
 * <LI> <code>range</code> describes the collection of values to be associated
 * to <code>var</code>, whose syntax and semantics is defined in 
 * {@link peersim.util.StringListParser}. </LI>
 * </UL>
 * An example of range specification is the following:
<pre>
range.0 SIZE;2^10:2^18*|2
range.1 K;20:30
range.2 CHURN;0.05,0.10,0.20 
</pre>
 * With this specification, the collection of values associated to 
 * <code>SIZE</code> is {2^10,2^11,...,2^18}; <code>K</code> contains 
 * {20,21,22,...,30}, while <code>CHURN</code> contains just the
 * specified values.
 * <p>
 * A separate Java virtual machine is invoked to run each of the
 * experiments. An attempt is done to run the same JVM version as
 * the one running the Range Simulator; if this is not possible
 * (for example due to path problems), the command shell mechanism
 * is used to run the first JVM version found in the path.
 * </p>
 * It is possible to specify options for the forked JVM using the
 * {@value #PAR_JVM} parameter on the command line. For example,
 * a command line like this:
<pre>
java peersim.rangesim.RangeSimulator config.file jvm.options=-Xmx256m
</pre>
 * can be used to run the forked JVM with a maximum heap of 256MB.
 * <p>
 * The new JVM inherits the same classpath as the JVM running the
 * RangeSimulator. The {@value #PAR_JVM} parameter can be used to
 * specify additional classpath specification.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class RangeSimulator
{

// --------------------------------------------------------------------------
// Configuration parameters
// --------------------------------------------------------------------------

/**
 * This is the prefix of the config properties whose value vary during
 * a set of experiments.
 * @config
 */
private static final String PAR_RANGE = "range";

/**
 * This config property can be used to set options in the JVMs that
 * are forked to execute experiments with different configuration
 * parameters.
 * @config
 */
private static final String PAR_JVM = "jvm.options";


// --------------------------------------------------------------------------
// Static variables
// --------------------------------------------------------------------------

/** Names of range parameters */
private static String[] pars;

/** Values to be simulated, for each parameter */
private static String[][] values;

/** The jvm options to be used when creating jvms */
private static String[] jvmoptions;


// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * Main method of the system.
 */
public static void main(String[] args)
throws IOException
{
	// Check if there are no arguments or there is an explicit --help
	// flag; if so, print the usage of the class
	if (args.length == 0 || args[0].equals("--help")) {
		usage();
		System.exit(1);
	}

	String[] argv = args.clone();
	
	// Read property file
	System.err.println("Simulator: loading configuration");
	Properties properties = new ParsedProperties(args);
	Configuration.setConfig(properties);
	
	// Read jvm options and separate them in different strings
	jvmoptions = Configuration.getString(PAR_JVM, "").split(" ");

	// Parse range parameters
	parseRanges();

	// Executes experiments; report short messages about exceptions that are
	// handled by the configuration mechanism.
	try {
		doExperiments(properties, argv);
	} catch (MissingParameterException e) {
		System.err.println(e + "");
		System.exit(1);
	} catch (IllegalParameterException e) {
		System.err.println(e + "");
		System.exit(1);
	}
	System.exit(0);
}

// --------------------------------------------------------------------

/**
 * Parses a collection of range specifications and returns the set of
 * parameter that will change during the simulation and the values that
 * will be used for those parameters.
 */
private static void parseRanges()
{
	// Get ranges
	String[] ranges = Configuration.getNames(PAR_RANGE);

	if (Configuration.contains(Simulator.PAR_EXPS) || ranges.length == 0) {
		// If there is an explicit simulation.experiment or there are no 
		// ranges
		pars = new String[ranges.length + 1];
		values = new String[ranges.length + 1][];
		pars[ranges.length] = "EXP";
		values[ranges.length] = StringListParser.parseList("1:"
				+ Configuration.getInt(Simulator.PAR_EXPS, 1));
	} else {
		pars = new String[ranges.length];
		values = new String[ranges.length][];
	}

	for (int i = 0; i < ranges.length; i++) {
		String[] array = Configuration.getString(ranges[i]).split(";");
		if (array.length != 2) {
			throw new IllegalParameterException(ranges[i],
					" should be formatted as <parameter>;<value list>");
		}
		pars[i] = array[0];
		values[i] = StringListParser.parseList(array[1]);
	}
}

// --------------------------------------------------------------------

/**
 * Selects the next set of values by incrementing the specified index
 * array. The index array is treated as a vector of digits; the first is
 * managed managed as a vector of digits.
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

// --------------------------------------------------------------------

public static void doExperiments(Properties properties, String[] args)
		throws IOException
{
	// Configure the java parameter for exception
	String filesep = System.getProperty("file.separator");
	String classpath = System.getProperty("java.class.path");
	String javapath = System.getProperty("java.home") + filesep + "bin" + filesep
			+ "java";
	ArrayList<String> list = new ArrayList<String>(20);
	list.add(javapath);
	list.add("-cp");
	list.add(classpath);
	for (int i=0; i < jvmoptions.length; i++)
		list.add(jvmoptions[i]);
	list.add("peersim.Simulator");
	for (int i=0; i < args.length; i++)
		list.add(args[i]);
	list.add(Simulator.PAR_REDIRECT);	
	// Multiple experiments with the same value are managed here.
	list.add(Simulator.PAR_EXPS+"=1");
	list.add(Log.PAR_LOG + "=RangeLogger");
	int startlog = list.size();
	list.add("");
	
	int startpar = list.size();
	for (int i=0; i < values.length; i++)
		list.add("");
		
	// Prepare the argument array for process forking
	String[] newargs = new String[list.size()];

	// Execute with different values
	int[] idx = new int[values.length]; // Initialized to 0
	while (idx[0] < values[0].length) {

		// Configure the argument string array
		for (int j = 0; j < pars.length; j++) {
			list.set(startpar + j, pars[j] + "=" + values[j][idx[j]]);
		}

		// Configure the log
		StringBuffer log = new StringBuffer();
		for (int j = 0; j < pars.length; j++) {
			log.append(pars[j]);
			log.append(" ");
			log.append(values[j][idx[j]]);
			log.append(" ");
		}

		list.set(startlog, Log.PAR_LOG + "." + RangeLogger.PAR_PREFIX+"="+log);

		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(list.toArray(newargs));
			pb.redirectErrorStream(true);
			p = pb.start();
		} catch (IOException e1) {
			try {
				list.set(0, "java");
				ProcessBuilder pb = new ProcessBuilder(list.toArray(newargs));
				pb.redirectErrorStream(true);
				p = pb.start();
			} catch (IOException e2) {
				System.err.println("Unable to launch a Java virtual machine");
				System.exit(1);
			}
		}
		BufferedReader toprint = new BufferedReader(new InputStreamReader(p
				.getInputStream()));
		String line;
		while ((line = toprint.readLine()) != null) {
			int last = line.charAt(line.length()-1);
			if (last != TaggedOutputStream.TAG) {
				System.err.println(line);
			} else {
				line = line.substring(0, line.length()-1);
				System.out.println(line);
			}
		}

		// Increment values
		nextValues(idx, values);
	
	}
}

// --------------------------------------------------------------------

private static void usage()
{
	System.err.println("Usage:");
	System.err.println("  peersim.RangeSimulator <configfile> <property>*");
}

}
