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

package aggregation.secure;

import peersim.core.Control;
import peersim.core.Log;

/**
 * This class is used to collect statistics with respect to the actual
 * number of correct and false positives that occur during a simulation.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class RejectionStatistics
implements Control
{

//--------------------------------------------------------------------------
// Fields 
//--------------------------------------------------------------------------

/** Number of correct positives */
private static int correct;

/** Number of false positives */
private static int incorrect;

/** The name of this object in the configuration file */
private final String name;


//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

/**
 * Creates a new rejection statistics and initializes to 0 the number
 * of correct and false positives.
 */
public RejectionStatistics(String name)
{
	this.name = name;
	// Whenever we create a new RejectionStatistics, we reset the values
	correct = 0;
	incorrect = 0;
}

//--------------------------------------------------------------------------
// Static methods
//--------------------------------------------------------------------------

/**
 * Increments the number of correct positives
 */
public static void incCorrect()
{
	correct++;
}

//--------------------------------------------------------------------------

/**
 * Increments the number of false positives
 */
public static void incIncorrect()
{
	incorrect++;
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
public boolean execute()
{
	Log.println(name, correct + " " + incorrect);
	return false;
}
  
//--------------------------------------------------------------------------

}
