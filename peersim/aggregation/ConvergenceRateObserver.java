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

package aggregation;

import peersim.core.*;
import peersim.reports.*;
import peersim.util.Log;
import peersim.config.*;

/**
 * 
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class ConvergenceRateObserver implements Observer
{

	////////////////////////////////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////////////////////////////////

	/** 
	 *  String name of the parameter used to determine the accuracy
	 *  for standard deviation
	 *  before stopping the simulation. If not defined, a negative value is
	 *  used which makes sure the observer does not stop the simulation
	 */
	public static final String PAR_ACCURACY = "accuracy";

	/** 
	 *  String name of the parameter used to select the protocol to operate on
	 */
	public static final String PAR_PROT = "protocol";

	////////////////////////////////////////////////////////////////////////////
	// Fields
	////////////////////////////////////////////////////////////////////////////

	/** Accuracy for standard deviation used to stop the simulation */
	private final double accuracy;

	/** The name of this observer in the configuration */
	private final String name;

	/** Protocol identifier */
	private final int pid;

	/** Initial variance */
	private double initvariance = -1.0;

	////////////////////////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////////////////////////

	/**
	 *  Creates a new observer using clear()
	 */
	public ConvergenceRateObserver(String name)
	{
		this.name = name;
		accuracy = Configuration.getDouble(name + "." + PAR_ACCURACY, -1);
		pid = Configuration.getInt(name + "." + PAR_PROT);
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods
	////////////////////////////////////////////////////////////////////////////
	
	// Comment inherited from interface
	public boolean analyze()
	{
		int time = peersim.core.CommonState.getT();
		
		/* Initialization */
		final int len = OverlayNetwork.size();
		double sum = 0.0;
		double sqrsum = 0.0;
	
		/* Compute max, min, average */
		for (int i = 0; i < len; i++)
		{
			Aggregation protocol =
				(Aggregation) OverlayNetwork.get(i).getProtocol(pid);
			double value = protocol.getValue();
			sum += value;
			sqrsum += value * value;
		}
		double average = sum / len;
		double variance =
				(((double) len) / (len - 1)) * (sqrsum / len - average * average);
		if (initvariance < 0)
		{
			initvariance = variance;
		}
		else {
			double rate = Math.pow(variance / initvariance, ((double) 1) / time );
			Log.println(name, 
				time + " " + // cycle identifier
				rate + " " + // actual rate
				variance + " " +   // actual reduction
				variance/initvariance + " " +
				len          // actual size
			);
		}
		
		return false;
	}

} 
