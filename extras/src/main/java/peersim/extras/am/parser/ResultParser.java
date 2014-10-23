/*
 * Copyright (c) 2003-2007 The BISON Project
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

package peersim.extras.am.parser;

import java.io.*;
import java.util.*;
import peersim.util.*;

/**
 * TODO: Comment 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ResultParser
{

public static void main(String[] args) throws Exception
{
	/** Read parameters */
	if (args.length < 3)
		usage();
	System.err.println("columns " + (args.length-2));
	BufferedReader reader;
	
	// "-" corresponds to the standard input
	if ("-".equals(args[0])) {
		reader = new BufferedReader(new InputStreamReader(System.in));
	} else {
		reader = new BufferedReader(new FileReader(args[0]));
	}
	
	// Identify x column
	int columnx = Integer.parseInt(args[1]) - 1;
	
	// Identify the y columns
	int[] columny = new int[args.length-2];
	int max = columnx;
	for (int i=0; i < columny.length; i++) {
		columny[i] = Integer.parseInt(args[2+i]) - 1;
		if (columny[i] > max)
			max = columny[i];
	}
	
	// Create statistics container 
	TreeMap map = new TreeMap();
	
	// Read lines
	String line = reader.readLine();
	while (line != null) {
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() < max) {
			line = reader.readLine();
			continue;
		}
		String[] tokens = new String[st.countTokens()];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = st.nextToken();
		}
		String cx = tokens[columnx];
		IncrementalStats[] stats = (IncrementalStats[]) map.get(cx);
		if (stats == null) {
			stats = new IncrementalStats[columny.length];
			for (int i=0; i < stats.length; i++)
				stats[i] = new IncrementalStats();
			map.put(cx, stats);
		}
		for (int i=0; i < columny.length; i++) {
			try {
				double cy = Double.parseDouble(tokens[columny[i]]);
				stats[i].add(cy);
			} catch (Exception e) {
			}
		}
		line = reader.readLine();
	}
	
	// Print results
	Iterator it = map.keySet().iterator();
	while (it.hasNext()) {
		Object key = it.next();
		IncrementalStats[] stats = (IncrementalStats[]) map.get(key);
		System.out.print(key);
		for (int i=0; i < columny.length; i++) {
			System.out.print(" " + stats[i].getAverage() + " " + stats[i].getMin()
					+ " " + stats[i].getMax() + " " + stats[i].getN() + " " + stats[i].getVar() + " " + stats[i].getMinCount() + " " + stats[i].getMaxCount());
		}
		System.out.println();
	}
}

/**
 * Prints usage.
 */
private static void usage()
{
	System.err.println("java " + ResultParser.class
			+ " <filename> <x-column> <y-column1> [<y-column2> ...]");
}
}
