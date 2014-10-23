package peersim.extras.am.parser;
import java.io.*;
import java.util.*;

import peersim.util.*;


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

public class ConvertRawKing
{
  private static int count = 0;

  private static HashMap<Integer, Integer> nodes = new HashMap<Integer, Integer>();
  
  
  public static int add(Integer node)
  {
  	if (nodes.get(node) == null) {
  		nodes.put(node, count);
  		return count++;
  	} else {
  		return nodes.get(node);
  	}
  }

  public static int getAddress(String s) throws Exception
  {
  	String[] b = s.split("\\.");
  	int ret = 0;
  	for (int i=0; i < 4 ; i++) {
  		//System.out.println("-> " + b[i]);
  		//System.out.println(b[i] + " " + (byte) Integer.parseInt(b[i]));
  		ret = (ret << 8) + (byte) Integer.parseInt(b[i]);
  	}
  	return ret;
  }
  
  private static final int MAXSIZE = 2000;
  
  public static void main(String[] args) throws Exception
  {
  	System.out.println("Opening files");
  	BufferedReader in = 
  		new BufferedReader(new InputStreamReader(System.in));
  	File fo = new File(args[0]);
  	
  	
  	System.out.println("Creating data structures");
  	IncrementalStats[][] lat = new IncrementalStats[MAXSIZE][];
  	for (int i=0; i < MAXSIZE; i++) 
  		lat[i] = new IncrementalStats[MAXSIZE];
  	int lines = 0;
      	
  	System.out.println("Reading the file");
  	String line = null;
  	// Skip initial lines
 		while ((line = in.readLine()) != null) {
 			String[] tok = line.split(" ");
 			try {
	 			int add1 = getAddress(tok[0]);
	 			int add2 = getAddress(tok[2]);
	 			//System.out.println(i1 + " " + i2);
	 		  int i1 = add(add1);
	 		  int i2 = add(add2);
	 			if (i1 > i2) {
	 				int tmp = i2;
	 				i2 = i1;
	 				i1 = tmp;
	 			}
	      if (lat[i1][i2] == null) {
	      	lat[i1][i2] = new IncrementalStats();
	      }
	
	 			
	 		  double latency = (Double.parseDouble(tok[4])-
	 		  		Double.parseDouble(tok[5]))/1000;
	 		  if (latency < 0)
	 		  	continue;
	    	lat[i1][i2].add(latency);
	    	//System.out.println(pair + " " + stats);
	    	//System.out.println(nodes.size());
	    	
	    	lines++;
	    	if (lines % 10000 == 0)
	    		System.out.println(lines);
 			} catch (Exception e) {
 			}
 		}
  	int size = nodes.size();
  	
  	System.out.println("Read " + size + " nodes, " + lines + " lines");
  	
  	System.out.println("Writing file");
  	ObjectOutputStream output = 
  		new ObjectOutputStream(
  				new BufferedOutputStream(
  						new FileOutputStream(fo))); 
  	
  	output.writeInt(size);
  	int count=0;
  	for (int i=0; i < size-1; i++) {
    	for (int j=i+1; j < size; j++) {
    		if (lat[i][j] != null) {
	    		output.writeInt((int) (lat[i][j].getAverage()));
	    		output.writeFloat((float) (lat[i][j].getStD()));
    		} else {
	    		output.writeInt(0);
	    		output.writeFloat(0.0F);
    			//System.out.print("+");
    		}
    		count++;
    	}
  	}
		System.out.println("Written " + count + " entries");

  	output.close();
  }  	
}

