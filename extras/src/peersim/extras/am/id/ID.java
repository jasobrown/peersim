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

/**
 * 
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ID
{

private static final String PAR_BITS = "BITS";

public static int BITS;

public static long SIZE;

public static long DIAM;
static {
	reset();
}

public static void reset()
{
	BITS = Configuration.getInt(PAR_BITS, 40);
	SIZE = 1L << BITS;
	DIAM = 1L << (BITS - 1);
}

// --------------------------------------------------------------------------
public static long dist(long a, long b)
{
	long d = Math.abs(a - b);
	if (d > DIAM)
		d = SIZE - d;
	return d;
}

// --------------------------------------------------------------------------
/**
 * this method defines the follows relation. It is not an ordering because in
 * the ring (loop version) it defines a circular structure. If loop is not
 * defined, returns true if b is greater than a. If loop is defined, it returns
 * true if b is in the half of the ring in the direction of increasing values
 */
public static boolean follows(long a, long b)
{
	return (b - a + SIZE) % SIZE <= DIAM;
}

// --------------------------------------------------------------------------
public static long create()
{
	return CommonState.r.nextLong() & ((1L << BITS) - 1);
}

// --------------------------------------------------------------------------
public static int log(long n)
{
	if (n == 0)
		return 0;
	n = n - 1;
	int pos = 0;
	while (n > 0) {
		n >>= 1;
		pos++;
	}
	return pos;
}

public static int log2(long n)
{
	if (n == 0)
		return 0;
	int pos = 0;
	while (n > 0L) {
		n = n >> 1;
		pos++;
	}
	return pos - 1;
}
}