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

package peersim.config;

/**
 * Reads configuration for relations between protocols. This class is not
 * strictly necessary because the protocols can read the configuration
 * themselves too. It contains nothing more than is contained in the
 * configuratin, only in a format that is much faster to read, so it can be used
 * runtime. Currently it processes only configuration parameter "linkable".
 * 
 * This class is a static singleton and is initialized only when first accessed.
 * During initialization it reads the configuration and initializes the links.
 */
public class FastConfig
{

// ======================= fields ===========================================
// ===========================================================================

/**
 * Parameter name in configuration that attaches a linkable protocol to a
 * protocol.
 * @config
 */
private static final String PAR_LINKABLE = "linkable";

/**
 * Parameter name in configuration that attaches a transport layer protocol to a
 * protocol.
 * @config
 */
private static final String PAR_TRANSPORT = "transport";

/**
 * This array stores the protocol id of the {@link peersim.core.Linkable}
 * protocol that is linked to the protocol given by the array index.
 */
protected static final int[] links;

/**
 * This array stores the protocol id of the {@link peersim.transport.Transport}
 * protocol that is linked to the protocol given by the array index.
 */
protected static final int[] transports;


// ======================= initialization ===================================
// ==========================================================================


/**
 * This static initialization block reads the configuration for information that
 * it understands. Currently it understands property {@value PAR_LINKABLE}
 * and {@value PAR_TRANFER}. When a
 * protocol has these parameters, it stores this info in a quickly
 * accessible array so that protocols can use it to quickly access this
 * information.
 *
 * Note that this class does not perform any type checks. The purpose if the
 * class is purely to
 * speed up the dumb reading of the configuration data.
 */
static {

	String[] names = Configuration.getNames(Configuration.PAR_PROT);
	links = new int[names.length];
	transports = new int[names.length];
	for (int i = 0; i < names.length; ++i)
	{
		if (Configuration.contains(names[i] + "." + PAR_LINKABLE))
			links[i] = 
			Configuration.getPid(names[i] + "." + PAR_LINKABLE);
		else
			links[i] = -1;
		
		if (Configuration.contains(names[i] + "." + PAR_TRANSPORT))
			transports[i] = 
			Configuration.getPid(names[i] + "." + PAR_TRANSPORT);
		else
			transports[i] = -1;
	}
}


// ======================= methods ==========================================
// ==========================================================================


/**
 * Returns true if the given protocol has a linkable protocol associated with
 * it, otherwise false.
 */
public static boolean hasLinkable(int pid) { return links[pid] >= 0; }

// ---------------------------------------------------------------------

/**
 * Returns the protocol id used by the protocol identified by pid. Throws an
 * IllegalParameterException if there is no linkable associated with the given
 * protocol: we assume here that his happens when the configuration is
 * incorrect.
 */
public static int getLinkable(int pid)
{
	if (links[pid] < 0) {
		String[] names = Configuration.getNames(Configuration.PAR_PROT);
		throw new IllegalParameterException(names[pid],
		"Protocol " + pid + " has no "+PAR_LINKABLE+" parameter");
	}
	return links[pid];
}

// ---------------------------------------------------------------------

/**
 * Returns true if the given protocol has a transport protocol associated with
 * it, otherwise false.
 */
public static boolean hasTransport(int pid)
{
	return transports[pid] >= 0;
}

// ---------------------------------------------------------------------

/**
 * Returns the protocol id used by the protocol identified by pid. Throws an
 * IllegalParameterException if there is no transport associated with the given
 * protocol: we assume here that his happens when the configuration is
 * incorrect.
 */
public static int getTransport(int pid)
{
	if (transports[pid] < 0) {
		String[] names = Configuration.getNames(Configuration.PAR_PROT);
		throw new IllegalParameterException(names[pid],
		"Protocol " + pid + " has no "+PAR_TRANSPORT + " parameter");
	}
	return transports[pid];
}

}
