package peersim.core;

/**
* This is the common state of the simulation all objects see.
* It's purpose is simplification of parameter structures and increasing
* efficiency by putting
* state information here instead of passing parameters.
* Fully static class, a singleton.
*/
public class CommonState {


// ======================= fields ==================================
// =================================================================

/**
* Current time in the simulation. It's public because eevrything would have
* read and write rights anyway.
*/
private static int time = 0;

/**
* Working variable to provide for an object version of time.
* This is useful to save memory because all objects can use the same object.
*/
private static Integer _time = new Integer(0);


// ======================== initialization =========================
// =================================================================

/**
*/
static { }


// ======================= methods =================================
// =================================================================


public static int getT() { return time; }

// -----------------------------------------------------------------

public static void setT( int t ) {
	
	_time = new Integer(t);
	time = t;
}

// -----------------------------------------------------------------

/** Returns time as an Integer object */
public static Integer getTimeObj() {  return _time; }


}

