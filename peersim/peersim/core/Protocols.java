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

package peersim.core;

/**
 * Connection protocol utility.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class Protocols 
{

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

	/**
	* This array stores the protocol id-s of the protocols from which instances
	* of this class get neighborhood info. If an instance of this class is
	* configured to be protocol i and it uses protocol j as a neighborhood source
	* then protLinks[i]=j. This means that the length of the array can be as
	* long as the number of all the protocols, but it can be expected to be
	* very few so this is not a performance problem. The constructor takes
	* care of this array.
	*/
	protected static int[][] links = new int[1][1];
  
	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////

	// XXX To be described better
	/**
	 * Returns the (unique) protocol used by the protocol identified by pid.
	 * This method must be used when a protocol depends on a single protocol.
	 */
	public static int getLink(int pid)
	{
		return links[pid][0];
	}
  
	/**
	 * Returns one of the protocols used by the protocol identified by pid.
	 * This method must be used when a protocol depends on multiple protocols.
	 * In this case, variable localid is used to discriminate between them.
	 * Each protocols is responsible for assigning local identifiers for
	 * all the protocols on which it depends. 
	 */
	public static int getLink(int pid, int position)
	{
		return links[pid][position];
	}
  
  
	/**
	 * Set the protocols
	 *
	 */
	public static void setLink(int pid, int position, int link)
	{
		int xmax = (links.length > pid+1 ? links.length : pid+1);
		int ymax = (links[0].length > position+1 ? links[0].length : position+1);
		if (xmax > links.length || ymax > links[0].length) {
			// Store old array
			int[][] tmp = links;
  		
			// Create new array
			links = new int[xmax][];
			for (int i=0; i < xmax; i++) {
				links[i] = new int[ymax];
			}
  		
			// Copy array
			for (int i=0; i < tmp.length; i++) {
				for (int j=0; j < tmp[0].length; j++) {
					links[i][j] = tmp[i][j];
				}
			}
		}
		links[pid][position]=link;  	
	}

	/**
	 * Set the protocols
	 *
	 */
	public static void setLink(int pid, int link)
	{
		setLink(pid, 0, link);
	}

}
