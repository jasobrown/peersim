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
import java.util.*;

import org.nfunk.jep.*;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;

/**
 * This class computes and reports statistics information about a vector.
 * Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats#toString}. This observer is capable to terminate
 * the simulation based on a configurable condition, expressed by the
 * parameter {@value #PAR_CONDITION}.
 * 
 * @see VectControl
 * @see peersim.vector
 */
public class VectorObserver extends VectControl {

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * This parameter is used to express the exit condition. When the
 * this condition evaluates to true, this control stops the
 * simulation. Condition may contain complex expressions with
 * arithmetic, comparisons and boolean operators. The condition
 * must return a boolean result; a numeric value of 0 is interpreted
 * as "false", while a non-zero value is interpreted as "true".
 * <p>
 * Constants and configuration parameters defined in the configuration
 * file may appear in the condition; furthermore, the following 
 * function names are defined:
 * <tt>getAverage</tt>,  
 * <tt>getMin</tt>, 
 * <tt>getMax</tt>, 
 * <tt>getMin</tt>, 
 * <tt>getMaxCount</tt>, 
 * <tt>getMinCount</tt>, 
 * <tt>getN</tt>, 
 * <tt>getgetSqrSum</tt>, 
 * <tt>getStD</tt>, 
 * <tt>getSum</tt>, 
 * <tt>getVar</tt> 
 * <p>
 * These function names correspond to the getter methods of 
 * {@link peersim.util.IncrementalStats}. They must be used
 * without paranthesis, and they are evaluated over the
 * aggregated information maintained by this observer.
 * <p>
 * For example, in the following example:
 * <pre>
 * control.observer VectorObserver {
 *   protocol protId
 *   getter getValue
 *   condition getMin < THRESHOLD/2
 * }
 * </pre>
 * the method <tt>getValue()</tt> is called on each instance of the
 * <tt>protId</tt> protocol; the result is added to an 
 * {@link IncrementalStats} object, which is then printed.
 * The control stops the simulation if method 
 * {@link IncrementalStats#getMin()} return a values smaller than 
 * half the value of constant <tt>THRESHOLD</tt>.
 * @config
 */
private static final String PAR_CONDITION = "condition";

//--------------------------------------------------------------------------
//Constants
//--------------------------------------------------------------------------

/** 
 * This set contains the aggregate functions that can be used in the
 * condition expression. These functions correspond to the getter
 * methods of {@link peersim.util.IncrementalStats}. 
 */
private static final Set functions = new HashSet();

static {
	functions.add("getAverage");
	functions.add("getMin");
	functions.add("getMax"); 
	functions.add("getMin"); 
	functions.add("getMaxCount");
	functions.add("getMinCount");
	functions.add("getN");
  functions.add("getgetSqrSum");
  functions.add("getStD");
  functions.add("getSum"); 
  functions.add("getVar");
}

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** The name of this observer in the configuration */
private final String prefix;

/** End value */
private String condition;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public VectorObserver(String prefix) {

	super(prefix);
	this.prefix = prefix;
	condition = Configuration.getString(prefix+"."+PAR_CONDITION, null);
}

//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

/**
 * Prints statistics information about a vector.
 * Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats#toString}.
 * @return always false
 */
public boolean execute() {

	IncrementalStats stats = new IncrementalStats();

	for (int j = 0; j < Network.size(); j++)
	{
		Number v = getter.get(j);
		stats.add( v.doubleValue() );
	}
	System.out.println(prefix+": "+stats);

	if (condition == null) 
		return false;
	
	JEP jep = new JEP();
	
	jep.setAllowUndeclared(true);

	jep.parseExpression(condition);
	String[] symbols = getSymbols(jep);
	for (int i = 0; i < symbols.length; i++) {
		// Search the method or variable
		if (functions.contains(symbols[i])) {
			
			Class clazz = IncrementalStats.class;
			Method method;
			try {
				method = GetterSetterFinder.getGetterMethod(clazz, symbols[i]);
			} catch (NoSuchMethodException e) {
				throw new IllegalParameterException(prefix + "." +
				PAR_CONDITION, "Method " + symbols[i] + " does not exist");
			}
			try {
				double v = ((Number) method.invoke(stats)).doubleValue();
				jep.addVariable(symbols[i], v);
			} catch (IllegalAccessException e) {
				throw new Error("We shouldn't reach this point");
			} catch (InvocationTargetException e) {
				throw new Error("We shouldn't reach this point");
			}
			
		} else {
			jep.addVariable(symbols[i], Configuration.getDouble(symbols[i]));
		}
	}
	Object ret = jep.getValueAsObject();
	if (jep.hasError()) {
		throw new IllegalParameterException(prefix + "." +
				PAR_CONDITION, jep.getErrorInfo());
	}
	if (ret instanceof Boolean)
		return ((Boolean) ret);
	else
		return (((Number) ret).doubleValue() != 0);
}

/**
 * Returns an array of string, containing the symbols contained in the
 * expression parsed by the specified JEP parser.
 * @param jep
 *          the java expression parser containing the list of variables
 * @return an array of strings.
 */
private static String[] getSymbols(org.nfunk.jep.JEP jep)
{
	Hashtable h = jep.getSymbolTable();
	String[] ret = new String[h.size()];
	Enumeration e = h.keys();
	int i = 0;
	while (e.hasMoreElements()) {
		ret[i++] = (String) e.nextElement();
	}
	return ret;
}

}
