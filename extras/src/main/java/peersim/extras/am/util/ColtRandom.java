/*
 * Copyright (c) 2007 The BISON Project
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

package peersim.extras.am.util;

import peersim.util.*;
import cern.jet.random.engine.*;

/**
 * This subclass implements the <code>ExtendedRandom</code>
 * interface with the purpose of substituting the standard
 * Java random number generator with a MersenneTwister.
 * The Mersenne Twister implementation is provided by the
 * Colt library (TODO).
 */
public class ColtRandom extends ExtendedRandom
{

/** 
 * Static masks used to speed up the execution
 * of method {@link ColtRandom#next(int)}.
 */
private static final int[] masks = new int[33];

{
  for (int i=1; i < 32; i++) {
  	masks[i] = (1 << i)-1;
  }
  masks[32] = -1;
}
	
/** Actual number generator */
MersenneTwister mt;

public ColtRandom(String prefix)
{
	super(0);
}

public int next(int bits)
{ 
	return mt.nextInt() & masks[bits];
}

/**
 * The internal Mersenne Twister generator is initialized
 * with the 32 less-significant part of the specified seed.
 */
public void setSeed(long seed)
{
	super.setSeed(seed);
	mt = new MersenneTwister((int) seed);
}

}
