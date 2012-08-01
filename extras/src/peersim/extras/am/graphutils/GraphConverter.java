/*
 * Copyright (c) 2011 Alberto Montresor
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

package peersim.extras.am.graphutils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import peersim.config.*;
import peersim.extras.am.util.*;

/**
 * This class takes a graph as input and convert it from the original format to a new one.
 * The user must specify both the input graph and the output graph, through the input and
 * output parameters.
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class GraphConverter
{

private static final String PAR_INPUT = "input";
private static final String PAR_OUTPUT = "output";
private static final String PAR_FILEIN = "file-in";
private static final String PAR_FILEOUT = "file-out";
private static final String PAR_DIRECTED = "directed";
private static final String PAR_SYMMETRIC = "symmetric";

private static final int BASE = 4;

static int counter = 0;
static IntArray[] neighbors = new IntArray[BASE];
static HashMap<Object,Integer> map = new HashMap<Object,Integer>();
static int[] buffer = new int[BASE];


/**
 * @param args
 */
public static void main(String[] args)
throws Exception
{
	System.err.println("Loading configuration");
	Configuration.setConfig( new ParsedProperties(args) );

	String input = Configuration.getString(PAR_INPUT, null);
	String output = Configuration.getString(PAR_OUTPUT, null);
  String filein = Configuration.getString(PAR_FILEIN, null);
  String fileout = Configuration.getString(PAR_FILEOUT, null);
  boolean directed = Configuration.contains(PAR_DIRECTED);
  boolean symmetric = Configuration.contains(PAR_SYMMETRIC);
  
  if (input == null || output == null || filein == null || fileout == null) {
  	  usage();
  		System.exit(1);
  }
  
  if (input.equals("edge-text")) {
		readEdgeText(filein, directed, symmetric);
  }	else if (input.equals("edge-bin")) {
			readEdgeBin(filein, directed, symmetric);
  }	else if (input.equals("edge-mega")) {
		readEdgeMega(filein, directed, symmetric);
	} else if (input.equals("neighbor-text")) {
		readNeighborText(filein, directed, symmetric);
	} else {
		System.err.println("Unknown input format");
	}
  
  if (output.equals("neighbor-text")) {
		writeNeighborText(fileout);
	} else if (output.equals("neighbor-bin")) {
		writeNeighborBin(fileout);
	} else if (output.equals("neighbor-giant")) {
		writeNeighborGiant(fileout);
	} else {
		System.err.println("Unknown output format");
	}


}

private static void usage()
{
	System.err.println("Usage: java GraphConverter <properties>");
	System.err.println("where properties are:");
	System.err.println("  file-in=<filename>");
	System.err.println("  file-out=<filename>");
	System.err.println("  input=<inputformat>");
	System.err.println("  output=<outputformat>");
	System.err.println("  [directed|symmetric] (optional)");
	System.err.println("  giant (optional)");
}


private static void readEdgeText(String filename, boolean directed, boolean symmetric)
 throws IOException
{
	FileReader fr = new FileReader(filename);
	LineNumberReader lnr = new LineNumberReader(fr);
	String line;
	while((line=lnr.readLine()) != null)
	{
	  if (line.charAt(0) == '%') {
	  	   System.err.println("Ignored: " + line);
	  	   continue;
	  }
		String[] pieces = line.split("\\p{Blank}");
		if (pieces.length != 2) {
			System.err.println("Ignored: " + line);
			continue;
		}
		int n0 = getId(pieces[0]);
		int n1 = getId(pieces[1]);
		if (directed) {
			addEdge(n0, n1);
		} else if (symmetric) {
			addEdge(n0, n1);
			addEdge(n1, n0);
		} else {		
			if (n0 < n1) {
				addEdge(n0, n1);					
			} else {
				addEdge(n0, n1);
			}
		}
		if (counter % 1000 == 0)
			System.err.println("Done.... " + counter);
	}
}

private static void readEdgeBin(String filename, boolean directed, boolean symmetric)
throws IOException
{
	DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
	
	boolean end =false;
	int edges = 0;
	while (!end) {
		int e1, e2;
		try {
			e1 = in.readInt();
		} catch (EOFException e) {
			end = true;
			e1 = 0;
		}
		if (end)
			break;
		try {
			e2 = in.readInt();
		} catch (EOFException e) {
			throw e;
		}
		//System.out.println(e1 + " " + e2);
		int n0 = getId(e1);
		int n1 = getId(e2);
		if (directed) {
			addEdge(n0, n1);
		} else if (symmetric) {
			addEdge(n0, n1);
			addEdge(n1, n0);
		} else {		
			if (n0 < n1) {
				addEdge(n0, n1);					
			} else {
				addEdge(n0, n1);
			}
		}
		edges++;
		if (edges % 1000000 == 0)
			System.err.println("Done.... " + edges);
	}
}

private static void readEdgeMega(String filename, boolean directed, boolean symmetric)
throws IOException
{
	InputStream in = new BufferedInputStream(new FileInputStream(filename));
	byte[] buffer = new byte[8];
	
	boolean end =false;
	int edges = 0;
	int olde1 = -1;
	IntArray list = new IntArray(1024);
	while (!end) {
		try {
			int len = in.read(buffer);
			if (len < 0) {
			  neighbors[olde1] = (IntArray) list.clone();
			  if (olde1 > counter) {
			  		counter = olde1;
			  }
				break;
			}
			int e1 = decodeInt(buffer, 0);
			int e2 = decodeInt(buffer, 4);
			if (olde1 != e1) {
				if (olde1 >= 0) {
				  if (olde1 > neighbors.length) {
			  	  		IntArray[] 	temp = new IntArray[olde1*2];
			  	  		System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
			  	  		neighbors = temp;
				  }
				  neighbors[olde1] = (IntArray) list.clone();
				  if (olde1 > counter) {
				  		counter = olde1;
				  }
				}
			  list.reset();
			  olde1 = e1;
			}
		  list.append(e2);
			edges++;
			if (edges % 1000000 == 0)
				System.err.println("Done.... " + edges);
		} catch (EOFException e) {
		  neighbors[olde1] = (IntArray) list.clone();
			end = true;
		}
	}
}

public static int decodeInt(byte [] buf, int offset) {
  int num = (int) (buf[offset] & 0xff);
  num |= ((int) (buf[offset + 1] & 0xff) << 8);
  num |= ((int) (buf[offset + 2] & 0xff) << 16);
  num |= ((int) (buf[offset + 3] & 0xff) << 24);
  return num;
}



private static void readNeighborText(String filename, boolean directed, boolean symmetric)
throws IOException
{
	FileReader fr = new FileReader(filename);
	LineNumberReader lnr = new LineNumberReader(fr);
	String line;
	while((line=lnr.readLine()) != null)
	{
		StringTokenizer st = new StringTokenizer(line);
		int n0 = getId(st.nextToken());
		while (st.hasMoreTokens()) {
		  int n1 = getId(st.nextToken());
			if (directed) {
				addEdge(n0, n1);
			} else if (symmetric) {
				addEdge(n0, n1);
				addEdge(n1, n0);
			} else {		
				if (n0 < n1) {
					addEdge(n0, n1);					
				} else {
					addEdge(n0, n1);
				}
			}
		}
//		if (counter % 1000 == 0)
//			System.err.println("Done.... " + counter);	
	}
}



private static void addEdge(int n0, int n1) 
{
	IntArray A = neighbors[n0];
	if (!A.contains(n1)) {
		A.append(n1);
	}
}	

private static int getId(String val) 
{
	Integer i = map.get(val);
	if (i == null) {
		i = counter;
		map.put(val, i);
	  if (counter == neighbors.length) {
	  	  IntArray[] temp = new IntArray[counter*2];
	  	  System.arraycopy(neighbors, 0, temp, 0, counter);
	  	  neighbors = temp;
	  }
	  neighbors[counter] = new IntArray(BASE);
		counter++;
  }
	return i;
}	

private static int getId(Integer val) 
{
	Integer i = map.get(val);
	if (i == null) {
		i = counter;
		map.put(val, i);
	  if (counter == neighbors.length) {
	  	  IntArray[] temp = new IntArray[counter*2];
	  	  System.arraycopy(neighbors, 0, temp, 0, counter);
	  	  neighbors = temp;
	  }
	  neighbors[counter] = new IntArray(BASE);
		counter++;
  }
	return i;
}	


private static int getComponents(int[] newid) 
{
	int[] tags = new int[counter];
	int[] sizes = new int[counter+1];
	int component = 0;
	Queue<Integer> q = new ArrayBlockingQueue<Integer>(counter);
	for (int i=0; i < counter; i++) {
		if (tags[i] == 0) {
			component++;
			bfsVisit(i, component, tags, sizes, q);
		} 
	}
	int max = 1;
	for (int i=2; i<=component; i++) {
		if (sizes[i] > sizes[max])
			max = i;
	}
	int id = 0;
  for (int i=0; i < counter; i++) {
  	  if (tags[i] == max) {
  	  	  	newid[i] = id++;
  	  } else {
  	  		newid[i] = -1;
  	  }
  }
  return id;
}

private static void bfsVisit(int i, int component, int[] tags, int[] sizes, Queue<Integer> q)
{
	q.clear();
  tags[i] = component;
	sizes[component]++;
	q.offer(i);
	int c = 0;
	while (!q.isEmpty()) {
		c++;
		//if (c % 1000 == 0)
			System.out.println(c + " " + component);
	  int v = q.poll();
		int degree = neighbors[v].size();
		for (int j=0; j<degree; j++) {
			int u = neighbors[v].get(j);
			if (tags[u] == 0) {
			  tags[u] = component;
				sizes[component]++;
				q.offer(u);
			}
		}
	}	  
}

private static void writeNeighborText(String filename)
 throws IOException
{
	PrintStream out = new PrintStream(filename);
	
	for (int i=0; i < counter; i++) {
		out.print(i);
		int size = neighbors[i].size();
		for (int j=0; j < size; j++) {
			out.print(" ");
			out.print(neighbors[i].get(j));
		}
		out.println();
	}
}

private static void writeNeighborBin(String filename)
throws IOException
{
	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

	out.writeInt(counter);
	for (int i=0; i < counter; i++) {
		out.writeInt(i);
		int degree = neighbors[i].size();
		out.writeInt(degree);
		for (int j=0; j < degree; j++) {
			out.writeInt(neighbors[i].get(j));
		}
	}
	out.close();
	
}

private static void writeNeighborGiant(String filename)
throws IOException
{
	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

	int[] ids = new int[counter];
	int newSize = getComponents(ids);
	System.out.println("newSize " + newSize);
	out.writeInt(newSize);
	for (int i=0; i < counter; i++) {
		int ir = ids[i]; // Real i
		if (ir >= 0) {
			out.writeInt(ir);
			int degree = neighbors[i].size();
			int dr = degree; // Real degree
			for (int j=0; j < degree; j++) {
				int jr = ids[neighbors[i].get(j)];
				if (jr < 0)  {
					dr--;
				}
			}
			System.out.println(i + " " + degree + " " + dr);
			out.writeInt(dr);
			for (int j=0; j < degree; j++) {
				int jr = ids[neighbors[i].get(j)];
				if (jr >= 0)  {
					out.writeInt(jr);
				}
			}
		}
	}
	out.close();
	
}

}