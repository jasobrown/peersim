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
		
package peersim.extras.am.reports;

import java.lang.reflect.*;
import java.util.*;

import org.lsmp.djep.groupJep.*;
import org.nfunk.jep.*;
import org.nfunk.jep.function.*;

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.vector.*;

/**
 * This class computes and reports statistics information about a vector.
 * Provided statistics include average, max, min, variance,
 * etc. Values are printed according to the string format of {@link 
 * IncrementalStats#toString}. This observer is capable to terminate
 * the simulation based on a configurable condition, expressed by the
 * parameter {@value #PAR_CONDITION}.
 * 
 * @see MultipleVectorObserver
 * @see peersim.vector
 */
public class ConditionalVectorObserver extends VectControl {

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
private static final String[] functions = { "getAverage", 
	"getMin", "getMax", "getMin", "getMaxCount", "getMinCount", "getN",
	"getSqrSum", "getStD", "getSum", "getVar" 
};

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/** The name of this observer in the configuration */
private final String prefix;

/** End value */
private String condition;

/** The incremental stats, accessible also to subclasses */
private IncrementalStats stats = new IncrementalStats();



//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public ConditionalVectorObserver(String prefix) {

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

	stats.reset();

	for (int j = 0; j < Network.size(); j++)
	{
		Number v = getter.get(j);
		stats.add( v.doubleValue() );
	}
	System.out.println(prefix+": "+stats);

	if (condition == null) 
		return false;
	
	GroupJep jep = new GroupJep(new Operators());
	jep.setAllowUndeclared(true);
	for (int i=0; i < functions.length; i++)
		jep.addFunction(functions[i], new StatsFunction(functions[i]));
	jep.parseExpression(condition);

	String[] symbols = getSymbols(jep);
	for (int i = 0; i < symbols.length; i++) {
		// Search the method or variable
		jep.addVariable(symbols[i], Configuration.getDouble(symbols[i]));
	}
	Object ret = jep.getValueAsObject();
	if (jep.hasError()) {
		throw new IllegalParameterException(prefix + "." +
				PAR_CONDITION, jep.getErrorInfo());
	}
	if (ret instanceof Boolean) {
		System.out.println(((Boolean) ret).booleanValue());
		return ((Boolean) ret).booleanValue();
	}
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

/**
 * This class implements the custom functions needed by Jep to
 * read the IncrementalStats 
 */
class StatsFunction extends PostfixMathCommand {

/** Method to be read */
Method method;

/**
 * Constructor
 */
public StatsFunction(String name)
{
	try {
		method = IncrementalStats.class.getMethod(name);
	} catch (NoSuchMethodException e) {
		throw new IllegalParameterException(prefix + "." +
		PAR_CONDITION, "Method " + name + " does not exist");
	}
	numberOfParameters = 0;
}

/**
 */
public void run(Stack inStack) throws ParseException {

	// check the stack
	checkStack(inStack);

	try {
		double v = ((Number) method.invoke(stats)).doubleValue();
		inStack.push(new Double(v));
	} catch (IllegalAccessException e) {
		throw new Error("We shouldn't reach this point");
	} catch (InvocationTargetException e) {
		throw new Error("We shouldn't reach this point");
	}
}

} // END StatsFunction

} // END VectorObserver
