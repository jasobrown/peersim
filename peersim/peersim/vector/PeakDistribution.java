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

package peersim.vector;

import java.lang.reflect.*;
import peersim.config.*;
import peersim.core.*;


/**
 * Initializes the values so that {@value #PAR_PEAKS} nodes have value
 * {@value #PAR_VALUE}/{@value #PAR_PEAKS}, the rest zero.
 * <p>
 * This dynamics class can initialize any protocol field containing a 
 * primitive value, provided that the field is associated with a setter method 
 * that modifies it.
 * The method to be used is specified through parameter {@value #PAR_METHOD}.
 * For backward compatibility, if no method is specified, the method
 * {@link SingleValue#setValue(double)} is used. In this way, classes
 * implementing the {@link SingleValue} interface can be initialized using the
 * old configuration syntax.
 * <p>
 * Please refer to package {@link peersim.vector} for a detailed description of 
 * the concept of protocol vector and the role of getters and setters. 
 */
public class PeakDistribution extends VectControl
{

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/** 
 * The sum of values in the system, to be equally distributed between peak 
 * nodes.
 * @config
 */
public static final String PAR_VALUE = "value";


/** 
 * The number of peaks in the system. If this value is greater or equal than 
 * 1, it is interpreted as the actual number of peaks. If it is included in 
 * the range [0, 1[ it is interpreted as a percentage with respect to the
 * current network size. Defaults to 1. 
 * @config
 */
public static final String PAR_PEAKS = "peaks";


// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Total load */
private final Number value;

/** Number of peaks */
private final double peaks;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

/**
 * @param prefix the configuration prefix for this class
 */
public PeakDistribution(String prefix)
{
	super(prefix);
	
	peaks = Configuration.getDouble(prefix+"."+PAR_PEAKS, 1);
	
	if( type==int.class || type==long.class )
		value = new Long(Configuration.getLong(prefix+"."+PAR_VALUE));
	else
		value = new Double(Configuration.getDouble(prefix + "." +
		PAR_VALUE));
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public boolean execute()
{
	int pn = (peaks < 1 ? (int) (peaks*Network.size()) : (int) peaks);
	
	if( type==long.class || type==int.class )
	{
		long v = value.longValue()/pn;
		for (int i=0; i < pn; i++) set(i, v);
		for (int i=pn; i < Network.size(); i++) set(i,0);
	}
	else
	{
		double v = value.doubleValue()/pn;
		for (int i=0; i < pn; i++) set(i, v);
		for (int i=pn; i < Network.size(); i++) set(i,0.0);
	}

	return false;
}

// --------------------------------------------------------------------------

}
