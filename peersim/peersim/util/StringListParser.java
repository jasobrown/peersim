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

package peersim.util;

import java.util.*;

/**
 * Format a,b:c,e:f|*g 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class StringListParser
{
 	
 	
 	public static String[] parseList(String s)
 	{
 		ArrayList list = new ArrayList();
 		String[] tokens = s.split(",");
 		for (int i=0; i < tokens.length; i++) {
			parseItem(list, tokens[i]);
 		}
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 	
 	private static void parseItem(List list, String item)
 	{
 		String[] array = item.split(":");
 		if (array.length == 1) {
 			parseSingleItem(list, item);
 		} else if (array.length == 2) {
 			parseRangeItem(list, array[0], array[1]);
 		} else {
 			throw new IllegalArgumentException("Element " + item + 
 				"should be formatted as <start>:<stop> or <value>");
 		}
 	}
 	
 	private static void parseSingleItem(List list, String item)
	{
		list.add(item);
	}
	
	private static void parseRangeItem(List list, String start, String stop)
	{
		double vstart;
		double vstop;
		double vinc;
		boolean sum;
		
		vstart = Double.parseDouble(start);
		int pos = stop.indexOf("|*");
		if (pos >= 0) {
			// The string contains a multiplicative factor
			vstop = Double.parseDouble(stop.substring(0, pos));
			vinc = Double.parseDouble(stop.substring(pos+2));
			sum = false;
		} else {
			pos = stop.indexOf("|");
			if (pos >= 0) {
				// The string contains an additive factor
				vstop = Double.parseDouble(stop.substring(0, pos));
				vinc = Double.parseDouble(stop.substring(pos+1));
				sum = true;
			} else {
				// The string contains just the final value
				vstop = Double.parseDouble(stop);
				vinc = 1;
				sum = true;
			}
		}
		if (sum) {
			for (double i=vstart; i <= vstop; i += vinc)
			  list.add(""+i);
		} else {
			for (double i=vstart; i <= vstop; i *= vinc)
				list.add(""+i);
		}
	}
 			
	public static void main(String[] args)
	{
		String[] ret = parseList(args[0]);
		for (int i=0; i < ret.length; i++)
			System.out.print(ret[i]+ " ");
		System.out.println("");
		
	}



}
