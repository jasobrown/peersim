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

public class ConvertHarward
{
  private static int count = 0;

  public static void main(String[] args) throws Exception
  {
  	System.out.println("Opening files");
  	BufferedReader in = 
  		new BufferedReader(new InputStreamReader(System.in));
  	if (args.length != 2) {
  		System.out.println("Usage: ConvertHarward <out-file> <size>");
  	}
  	File fo = new File(args[0]);
  	int size = Integer.parseInt(args[1]);
  	
  	
  	System.out.println("Creating data structures");
  	ArrayList<Short>[][] lat = new ArrayList[size][];
  	for (int i=0; i < size; i++) 
  		lat[i] = new ArrayList[size];
  	int lines = 0;
      	
  	System.out.println("Reading the file");
  	String line = null;
  	// Skip initial lines
 		while ((line = in.readLine()) != null) {
 			//System.out.println(line);
 			String[] tok = line.split(" ");
 			try {
	 			int i1 = Integer.parseInt(tok[1]);
	 			int i2 = Integer.parseInt(tok[2]);
	 			if (i1 < i2) {
	 				int tmp = i2;
	 				i2 = i1;
	 				i1 = tmp;
	 			}	 			
	      if (lat[i1][i2] == null) {
	      	lat[i1][i2] = new ArrayList<Short>();
	      }
	 		  short latency = (short) Double.parseDouble(tok[3]);
	 		  if (latency < 0)
	 		  	continue;
//	 		  if (latency < 1000)
	 		  lat[i1][i2].add(latency);
	 		  //System.out.println(i1 + " " + i2);
	    	
	    	lines++;
	    	if (lines % 100000 == 0)
	    		System.out.println(lines);
 			} catch (Exception e) {
 			}
 		}
  	
  	System.out.println("Read " + size + " nodes, " + lines + " lines");
  	
  	System.out.println("Checking file");
  	
  	int c1 = 0;
  	int c2 = 0;
  	for (int i=0; i < size; i++) {
  		IncrementalStats stats = new IncrementalStats();
  		for (int j=0 ; j < i; j++) {
    		c2++;
  			if (lat[i][j] == null) {
  				stats.add(0);
  			  c1++;
  			} else {
  				stats.add(lat[i][j].size());
  			}
  		}
  		System.out.println(i + " " + stats);
  	}
  	System.out.println("C1 " + c1);
  	System.out.println("C2 " + c2);
  	
  	System.out.println("Writing file");
  	ObjectOutputStream output = 
  		new ObjectOutputStream(
  				new BufferedOutputStream(
  						new FileOutputStream(fo))); 
  	
  	output.writeShort(size);
  	int count=0;
  	for (int i=0; i < size; i++) {
    	for (int j=0; j < i; j++) {
    		if (lat[i][j] != null) {
    			int s = lat[i][j].size();
    			output.writeShort(s);
    			for (int k=0; k < s; k++) {
        		count++;
    				output.writeShort(lat[i][j].get(k));
    			}
    		} else {
	    		output.writeShort(0);
    		}
    	}
  	}
		System.out.println("Written " + count + " entries");
		
  	output.close();
  }  	
}

