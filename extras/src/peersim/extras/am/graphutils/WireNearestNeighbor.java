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

package peersim.extras.am.graphutils;

import peersim.config.*;
import peersim.core.*;

public class WireNearestNeighbor implements Control
{

private final static String PAR_PROTOCOL = "protocol";

private final static String PAR_U = "u";

private final static String PAR_K = "k";

private final int pid;

private final double u;

private final int k;

public WireNearestNeighbor(String prefix)
{
	pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	u = Configuration.getDouble(prefix + "." + PAR_U, 0.8);
	k = Configuration.getInt(prefix + "." + PAR_K, 1);
}

public boolean execute()
{
	int size = Network.size();
	int counter = 0;
	while (counter < size) {

		if (CommonState.r.nextDouble() < u) {
			// Add a new 2-hop edge
			if (counter <= 2)
				continue;

			int middle = CommonState.r.nextInt(counter);
			Linkable link = (Linkable) Network.get(middle).getProtocol(pid);
			while (link.degree() <= 1) {
				// System.out.print("*");
				middle = CommonState.r.nextInt(counter);
				link = (Linkable) Network.get(middle).getProtocol(pid);
			}
			int x = CommonState.r.nextInt(link.degree());
			int y = CommonState.r.nextInt(link.degree());
			while (x == y) {
				// System.out.print("-");
				y = CommonState.r.nextInt(link.degree());
			}
			Linkable lx = (Linkable) Network.get(x).getProtocol(pid);
			Linkable ly = (Linkable) Network.get(y).getProtocol(pid);
			if (!lx.contains(Network.get(y))) {
				lx.addNeighbor(Network.get(y));
				ly.addNeighbor(Network.get(x));
			}

		} else {

			// Add a new node, connecting it to a random partner among the
			// existing ones
			counter++;
			if (counter <= 1)
				continue;
			int partner = CommonState.r.nextInt(counter-1);
			Linkable ln = (Linkable) Network.get(counter-1).getProtocol(pid);
			Linkable lp = (Linkable) Network.get(partner).getProtocol(pid);
			ln.addNeighbor(Network.get(partner));
			lp.addNeighbor(Network.get(counter-1));
			for (int j = 0; j < k; j++) {
				int x = CommonState.r.nextInt(counter);
				int y = CommonState.r.nextInt(counter);
				while (x == y) {
					// System.out.print("+");
					y = CommonState.r.nextInt(counter);
				}
				Linkable lx = (Linkable) Network.get(x).getProtocol(pid);
				Linkable ly = (Linkable) Network.get(y).getProtocol(pid);
				if (!lx.contains(Network.get(y))) {
					lx.addNeighbor(Network.get(y));
					ly.addNeighbor(Network.get(x));
				}
			}

		}
	}

	return false;
}

}
