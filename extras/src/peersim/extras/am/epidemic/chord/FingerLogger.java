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

package peersim.extras.am.epidemic.chord;

import peersim.config.*;
import peersim.core.*;
import peersim.extras.am.id.*;

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class FingerLogger implements Control
{

/** The Chord protocol to be observed */
private final static String PAR_PROT = "protocol";

private final static String PAR_HOLDER = "holder";

private final int pid;

private final int hid;

private final String prefix;

/**
 * 
 */
public FingerLogger(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROT);
	hid = Configuration.getPid(prefix + "." + PAR_HOLDER, pid);
	this.prefix = prefix;
}

public boolean execute()
{
	int size = Network.size();
	for (int i = 0; i < size; i++) {
		Node node = Network.get(i);
		Chord chord = (Chord) node.getProtocol(pid);
		for (int j = 0; j < ID.BITS; j++) {
			Node finger = chord.getFinger(j);
			long lid = IDUtil.getID(node, hid);
			if (finger == null) {
				System.out.println(lid + ".finger[" + j + "] = null");
			} else {
				long rid = IDUtil.getID(finger, hid);
				System.out.println(lid + ".finger[" + j + "] = "
						+ (rid - lid + ID.SIZE) % ID.SIZE);
			}
		}
	}
	return false;
}
}
