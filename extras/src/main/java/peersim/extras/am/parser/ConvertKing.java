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

package peersim.extras.am.parser;

import java.io.*;
import java.util.*;


public class ConvertKing
{

private static final int MAX = 3000;

public static void main(String[] args) throws Exception
{
	File fi = new File(args[0]);
	File fo = new File(args[1]);

	BufferedReader in = new BufferedReader(new FileReader(fi));

	int[][] table = new int[MAX][];
	for (int i = 0; i < MAX; i++)
		table[i] = new int[MAX];

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
	System.err.println("KingParser: read " + size + " entries");
	try {
		do {
			StringTokenizer tok = new StringTokenizer(line, ", ");
			int n1 = Integer.parseInt(tok.nextToken()) - 1;
			int n2 = Integer.parseInt(tok.nextToken()) - 1;
			int latency = (int) (Double.parseDouble(tok.nextToken()) / 1000);
			table[n1][n2] = table[n2][n1] = latency;
			System.out.println(n1 + "," + n2 + " = " + latency);

			line = in.readLine();
		} while (line != null);
	} catch (IOException e) {
	}

	ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(
			new FileOutputStream(fo)));

	output.writeInt(size);
	int count = 0;
	for (int i = 1; i <= size; i++) {
		for (int j = i + 1; j <= size; j++) {
			output.writeInt(table[i][j]);
			count++;
		}
	}
	System.out.println("Written " + count + " entries");

	output.close();
}
}
