package peersim.extras.am.parser;
import java.io.*;

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

public class ConvertMeridian
{
  private static final int MAX = 3000;

  public static void main(String[] args) throws Exception
  {
  	File fi = new File(args[0]);
  	File fo = new File(args[1]);

  	BufferedReader input = new BufferedReader(new FileReader(fi));

  	int[][] table = new int[MAX][];
  	for (int i=0; i < MAX; i++)
  		table[i] = new int[MAX];
  	
  	String line;
  	int size = 0;
  	while ((line = input.readLine()) != null) {
  		String[] strings = line.split("\\s+");
  		int x = Integer.parseInt(strings[0]);
  		int y = Integer.parseInt(strings[1]);
  		size = Math.max(size, y);
  		int val = (int) (Long.parseLong(strings[2])+500)/1000;
  	  if (val == 0)
  	  	System.err.print("+");
  		table[x][y] = table[y][x] = val;
  	}

  	ObjectOutputStream output = 
  		new ObjectOutputStream(
  				new BufferedOutputStream(
  						new FileOutputStream(fo))); 
  	
  	output.writeInt(size);
  	int count=0;
  	for (int i=1; i <= size; i++) {
    	for (int j=i+1; j <= size; j++) {
    		output.writeInt(table[i][j]);
    		count++;
    	}
  	}
		System.out.println("Written " + count + " entries");

  	output.close();
  }
}
