package peersim.cdsim;

import peersim.config.*;
import peersim.core.*;
import peersim.reports.Observer;
import peersim.init.Initializer;
import peersim.dynamics.Dynamics;
import peersim.util.CommonRandom;

/**
* This is the executable class for performing a cycle driven simulation.
* The class is completely static as at the same time we expect to have only
* one simulation running in a virtual machine.
* The simulation is highly configurable.
*/
public class Simulator {


// ============== fields ===============================================
// =====================================================================

/** Gives the number of cycles to complete */
public static final String PAR_CYCLES = "simulation.cycles";

/**
* if set, it means the order of visiting each node is shuffled in each cycle.
* The default is no shuffle
*/
public static final String PAR_SHUFFLE = "simulation.shuffle";

/**
* The number of times the experiment is run. Defaults to 1.
*/
public static final String PAR_EXPS = "simulation.experiments";

/**
* The type of the getPair function. Defaults to "seq".
*/
public static final String PAR_GETPAIR = "simulation.getpair";

public static final String PAR_INIT = "init";

/**
* This is the prefix for network dynamism managers. These have to be of
* type {@link Dynamics}.
*/
public static final String PAR_DYN = "dynamics";

public static final String PAR_OBS = "observer";

public static final String PAR_FINAL = "finalizer";

// --------------------------------------------------------------------

/** the number of independent restarted simulations to be performed */
private static int cycles;

private static boolean shuffle;

private static int exps;

private static boolean getpair_rand;

/** holds the observers of this simulation */
private static Observer[] observers=null;

/** holds the modifiers of this simulation */
private static Dynamics[] dynamics=null;

/** Holds the finalizer of this simulation */
private static Observer[] finalizers = null;


// =============== protected methods ===================================
// =====================================================================


protected static void runInitializers() {
	
	Object[] inits = Configuration.getInstanceArray(PAR_INIT);

	for(int i=0; i<inits.length; ++i)
	{
		System.err.println(
		"- Running initializer " + i + ": " + inits[i].getClass());
		((Initializer)inits[i]).initialize();
	}
}

// --------------------------------------------------------------------

protected static void nextRound() {

	if( shuffle )
	{
		OverlayNetwork.shuffle();
	}
	
	for(int j=0; j<OverlayNetwork.size(); ++j)
	{
		Node node = null;
		if( getpair_rand )
			node = OverlayNetwork.get(
			   CommonRandom.r.nextInt(OverlayNetwork.size()));
		else
			node = OverlayNetwork.get(j);
		int len = node.protocolSize();
		// XXX maybe should use different shuffle for each protocol?
		// (instead of running all on one node at the same time?)
		for(int k=0; k<len; ++k)
		{
			Protocol protocol = node.getProtocol(k);
			if( protocol instanceof CDProtocol )
				((CDProtocol)protocol).nextCycle(node, k);
		}
	}
}

// =============== public methods ======================================
// =====================================================================

/**
*  Loads configuration and executes the simulation.
*/
public static void main(String[] pars) throws Exception {
	
	long time = System.currentTimeMillis();	

	// loading config
	// XXX we assume here that config is properties format
	System.err.println("Simulator: loading configuration");
	Configuration.setConfig( new ConfigProperties(pars) );
	cycles = Configuration.getInt(PAR_CYCLES);
	shuffle = Configuration.contains(PAR_SHUFFLE);
	exps = Configuration.getInt(PAR_EXPS,1);
	// XXX this is a hack temporarily
	getpair_rand = Configuration.contains(PAR_GETPAIR);

	for(int k=0; k<exps; ++k)
	{
		System.err.println("Simulator: starting experiment "+k);
	
		// initialization
		System.err.println("Simulator: resetting overlay network");
		OverlayNetwork.reset();
		System.err.println("Simulator: running initializers");
		runInitializers();
	
		// load analizers
		Object otmp[] = Configuration.getInstanceArray(PAR_OBS);
		observers = new Observer[otmp.length];
		for(int i=0; i<otmp.length; ++i)
		{
			System.err.println(
			"- Observer " + i + ": " + otmp[i].getClass());
			observers[i]=(Observer)otmp[i];
		}
	
		// load dynamism managers
		otmp = Configuration.getInstanceArray(PAR_DYN);
		dynamics = new Dynamics[otmp.length];
		for(int i=0; i<otmp.length; ++i)
		{
			System.err.println(
			"- Dynamics " + i + ": " + otmp[i].getClass());	
			dynamics[i]=(Dynamics)otmp[i];
		}
	
			// load finalizer
		otmp = Configuration.getInstanceArray(PAR_FINAL);
		finalizers = new Observer[otmp.length];
		for (int i = 0; i < otmp.length; ++i)
		{
			System.err.println(
			"- Finalizer " + i + ": " + otmp[i].getClass());
			finalizers[i] = (Observer) otmp[i];
		}

		// main cycle
		System.err.println("Simulator: starting simulation");
		CommonState.setT(0); // needed here
		for(int i=0; i<cycles; ++i)
		{
			CommonState.setT(i);
			
			// analizer
			boolean stop = false;
			for(int j=0; j<observers.length; ++j)
			{
				stop = stop || observers[j].analyze();
			}
			if( stop ) break;
		
			// dynamism
			for(int j=0; j<dynamics.length; ++j)
			{
				dynamics[j].modify();
			}
		
			// do one round
			nextRound();
			System.err.println("Simulator: cycle "+i+" done");
		}
		
		// analysis after the simulation
		for(int j=0; j<finalizers.length; ++j)
		{
			finalizers[j].analyze();
		}
	}
	
	// undocumented testing capabilities
	if(Configuration.contains("__t")) 
		System.out.println(System.currentTimeMillis()-time);
	if(Configuration.contains("__x")) OverlayNetwork.test();
}

}


