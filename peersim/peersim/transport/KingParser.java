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

package peersim.transport;

import java.io.*;
import java.util.*;
import peersim.config.*;
import peersim.core.Control;

/**
 * Initializes static singleton {@link E2ENetwork} by reading a king data set.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class KingParser implements Control
{

// ---------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------

/**
 * The file containing the King measurements.
 * @config
 */
private static final String PAR_FILE = "file";

/**
 * The ratio between the time units used in the configuration file and the
 * time units used in the Peersim simulator.
 * @config
 */
private static final String PAR_RATIO = "ratio";

// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------

/** Name of the file containing the King measurements. */
private String filename;

/**
 * Ratio between the time units used in the configuration file and the time
 * units used in the Peersim simulator.
 */
private double ratio;

/** Prefix for reading parameters */
private String prefix;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

/**
 * Read the configuration parameters.
 */
public KingParser(String prefix)
{
	this.prefix = prefix;
	ratio = Configuration.getDouble(prefix + "." + PAR_RATIO, 1);
	filename = Configuration.getString(prefix + "." + PAR_FILE);
}

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

/**
 * Initializes static singleton {@link E2ENetwork} by reading a king data set.
* @return  always false
*/
public boolean execute()
{
	BufferedReader in = null;
	try {
		in = new BufferedReader(new FileReader(filename));
	} catch (FileNotFoundException e) {
		throw new IllegalParameterException(prefix + "." + PAR_FILE, filename
				+ " does not exist");
	}

	// XXX If the file format is not correct, we will get quite obscure
	// exceptions. To be improved.

	String line = null;
	// Skip initial lines
	int size = 0;
	try {
		while ((line = in.readLine()) != null && !line.startsWith("node"));
		while (line != null && line.startsWith("node")) {
			size++;
			line = in.readLine();
		}
	} catch (IOException e) {
	}
	E2ENetwork.reset(size, true);
	System.out.println(size);
	try {
		do {
			// System.out.println(line);
			StringTokenizer tok = new StringTokenizer(line, ", ");
			int n1 = Integer.parseInt(tok.nextToken()) - 1;
			int n2 = Integer.parseInt(tok.nextToken()) - 1;
			int latency = (int) (Double.parseDouble(tok.nextToken()) * ratio);
			E2ENetwork.setLatency(n1, n2, latency);

			line = in.readLine();
		} while (line != null);
	} catch (IOException e) {
	}

	return false;
}

}
