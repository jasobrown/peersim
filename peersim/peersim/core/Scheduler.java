package peersim.core;

import peersim.config.Configuration;

// XXX a quite primitive scheduler, should be able to be configured
// much more flexibly using a simlpe syntax for time ranges.
/**
* A binary function over the time points. That is,
* for each time point (cycle in the simulation) returns a boolean
* value.
*
* <p>The concept of time is understood as follows. Time point 0 refers to
* the time before cycle 0. In general, time point i refers to the time
* before cycle i. The special time index FINAL refers to the time
* after the last cycle. Note that the index of the last cycle is not known
* in advance, because the simulation can stop at any time, based on other
* components.
*
* <p>In this simple implementation the valid times will be
* <tt>from, from+step, from+2*step, etc,</tt>
* where the last element is strictly less than <tt>until</tt>. If FINAL is
* defined, it is also added to the set of active time points.
*/
public class Scheduler {


// ========================= fields =================================
// ==================================================================


/** 
* Defaults to 1.
*/
public static final String PAR_STEP = "step";

/** 
* Defaults to -1.
*/
public static final String PAR_AT = "at";


/** 
* Defaults to 0.
*/
public static final String PAR_FROM = "from";

/** 
* Defaults to <tt>Integer.MAX_VALUE</tt>.
*/
public static final String PAR_UNTIL = "until";

/**
* Defines if component is active after the last cycle has finished.
* Note that the index of last cycle is not know in advance because other
* components can stop the simulation at any time.
* By default not set.
*/
public static final String PAR_FINAL = "FINAL";

/**
* This is currently used for observers only, it is ignored for all other
* components.
* If defined the component will be run right before the cycle begins,
* after the dynamism managers have been run, and not before the dynamism
* managers. It is not defined by default.
*/
public static final String PAR_PRECYCLE = "precycle";

protected final int step;

protected final int from;

protected final int until;

protected final boolean fin;

protected final boolean precycle;


// ==================== initialization ==============================
// ==================================================================


public Scheduler(String prefix) {

  int at = Configuration.getInt(prefix+"."+PAR_AT,-1);
  if (at < 0) {
		step = Configuration.getInt(prefix+"."+PAR_STEP,1);
		from = Configuration.getInt(prefix+"."+PAR_FROM,0);
		until = Configuration.getInt(prefix+"."+PAR_UNTIL,Integer.MAX_VALUE);
  } else {
  	from = at;
  	until = at+1;
  	step = 1;
  }
	fin = Configuration.contains(prefix+"."+PAR_FINAL);
	precycle = Configuration.contains(prefix+"."+PAR_PRECYCLE);
}


// ===================== public methods ==============================
// ===================================================================


public boolean active(int time) {
	
	if( time < from || time >= until ) return false;
	return (time - from)%step == 0; 
}

// -------------------------------------------------------------------

public boolean active() {
	
	return active( CommonState.getT() );
}

// -------------------------------------------------------------------

public boolean fin() { return fin; }

// -------------------------------------------------------------------

public boolean preCycle() { return precycle; }

}


