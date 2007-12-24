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

package peersim.extras.am.id;

import peersim.config.*;
import peersim.core.*;
import peersim.dynamics.*;

/**
 * Initializes an array of {@link peersim.vector.SingleValue} protocols from anoter array.
 */
public class IDCopy implements Control, NodeInitializer
{

// ========================= fields =================================
// ==================================================================
/**
 * String name of the parameter used to select the protocol to operate on.
 */
public static final String PAR_PROT = "protocol";

/**
 * String name of the parameter used to select the protocol to use to copy from.
 * The protocol given in {@value #PAR_PROT} will be initialized from this
 * protocol.
 */
public static final String PAR_CLONE = "copy";

private final int pid;

private final int clone;

// ==================== initialization ==============================
// ===================================================================
public IDCopy(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	clone = Configuration.getPid(prefix + "." + PAR_CLONE);
}

// ===================== methods =====================================
// ===================================================================
public boolean execute()
{
	for (int i = 0; i < Network.size(); ++i) {
		initialize(Network.get(i));
	}
	return false;
}

// -------------------------------------------------------------------
public void initialize(Node n)
{
	IDHolder cln = (IDHolder) n.getProtocol(clone);
	((IDHolder) n.getProtocol(pid)).setID(cln.getID());
}
}
