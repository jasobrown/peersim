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

import java.util.Random;
import peersim.config.Configuration;

/**
* This is the common source of randomness which all objects of the
* application should use to make the experiments reproducable.
* Fully static class.
*/
public class CommonRandom {

// ======================= fields ==================================
// =================================================================

/**
* Configuration parameter used to initialized the random seed.
* If it is not specified the current time is used.
*/
public static String PAR_SEED = "random.seed";

/**
* This source of randomness should be used by all components.
* This field is public because it doesn't matter if it changes
* during an experiment (although it shouldn't) until no other sources of
* randomness are used within the system.
*/
public static Random r = null;

// ======================== initialization =========================
// =================================================================

/**
* Initializes the field {@link r} according to the configuration.
* Assumes that the configuration is already
* loaded.
*/
static {
	
	long seed =
		Configuration.getLong(PAR_SEED,System.currentTimeMillis());
	r = new Random(seed);
}

}

// note that it can be initialized by any object extending java.util.Random
// which was designed to support and ancourage extension anyway

