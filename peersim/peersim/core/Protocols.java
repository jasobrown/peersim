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
  protected static int[] links = null;
  
  ////////////////////////////////////////////////////////////////////////////
  // Methods
  ////////////////////////////////////////////////////////////////////////////

  // XXX To be described better
  /**
   * Returns the protocol used by the protocol protocolId.
   */
  public static int getLink(int protocolId)
  {
  	return links[protocolId];
  }
  
  /**
   * Set the protocols
   *
   */
  public static void setLink(int protocolId, int link)
  {
  	if (links == null || links.length<protocolId+1)
  	{
  		int[] tmp = links;
  		links = new int[protocolId+1];
  		if( tmp != null )
  			System.arraycopy(tmp,0,links,0,tmp.length);
  	}
  	links[protocolId]=link;  	
  }

}
