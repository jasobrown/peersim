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

package peersim.config;

/**
* Reads configuration for relations between protocols.
* This class is not strictly
* necessary because the protocols can read the configuration themselves too.
* It contains nothing more than is contained in the configuratin, only
* in a format that is much faster to read, so it can be used runtime.
* Currently it processes only configuration
* parameter "linkable".
*
* This class is a static singleton and is initialized only when first accessed.
* During initialization it reads the configuration and initializes the links.
*/
public class FastConfig {

// ======================= fields ===========================================
//===========================================================================

/**
* Parameter name in configuration that attaches a linkable protocol to
* a protocol.
*/
public static final String PAR_LINKABLE = "linkable";

/**
* This array stores the protocol id-s of the linkable protocols that
* are linked form the protocol given by the array index.
*/
protected static final int[] links;


// ======================= initialization ===================================
// ==========================================================================

/**
* This static initialization block reads the configuration for information
* that it understands. Currently it understands property "linkable" only.
* When a protocol has a parameter "linkable", it stores this info in a
* quickly accessible array so that protocols can use it to quickly access
* this information.
*/
static {
	
	String[] names = Configuration.getNames(Configuration.PAR_PROT);
	links = new int[names.length];
	for(int i=0; i<links.length; ++i) links[i]=-1;
	for(int i=0; i<links.length; ++i)
	{
		if( Configuration.contains(names[i]+"."+PAR_LINKABLE) )
			links[i] =
			Configuration.getPid(names[i]+"."+PAR_LINKABLE);
		else
			links[i]=-1;
	}
}


// ======================= methods ==========================================
// ==========================================================================


/**
* Returns the protocol id used by the protocol identified by pid.
* @return returns a negative value if there is no linkable associated with
* the given protocol, otherwise the pid of the linkable protocol.
*/
public static int getLinkable(int pid) {

	return links[pid];
}
  

}

