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

package peersim.extras.am.mastersim;

import java.io.*;
import java.util.*;

import peersim.*;
import peersim.core.*;
import peersim.rangesim.*;


class ExecutionThread extends Thread implements ProcessHandler
{

/** Random number generator to create seeds */
private static final Random r = new Random();

/** The */
private File jobdir;

/** The config file representing the job to be executed */
private File job;

/** The mechanism for transferring files */
private FileTransfer ft;

/** Available hosts */
private Heap hosts;

/** The jar files that represents the classpath */
private File[] jarfiles;

/** Output directory */
private File resdir;

/** Id of this thread; needed for logging */
private int id;

/** The external process being executed; null otherwise */
private Process p;

private Set done;

/** If true, used the RandomSimulator instead of the RangeSimulator */
private boolean random;

/**
 * 
 */
public ExecutionThread(File jobdir, File job, FileTransfer ft,
		Heap hosts, File[] jarfiles, Set done, File resdir, int id, boolean random)
{
	this.jobdir = jobdir;
	this.job = job;
	this.ft = ft;
	this.hosts = hosts;
	this.jarfiles = jarfiles;
	this.done = done;
	this.resdir = resdir;
	this.id = id;
	this.random = random;
}

public void run()
{
	boolean completed = false;
	while (!completed) {
		HostDescriptor desc = (HostDescriptor) hosts.removeFirst();
		
		
		// Array list containing the command parameter
		ArrayList<String> list = new ArrayList<String>(20);
	
		// Remote execute a JVM
		list.add("ssh");
		list.add("-o");
		list.add("NumberOfPasswordPrompts=0");
		list.add(desc.getHostname());
//		list.add("nice");
		list.add(desc.getCommand());
		
		// Transfer jar files and config files to the destination
		// and add them to the classpath
		list.add("-cp");
		StringBuilder build = new StringBuilder();
	
		
		String[] remotejars = new String[jarfiles.length];
		for (int i=0; i < jarfiles.length; i++) {
			if (i!=0) build.append(":");
			build.append(ft.transfer(done, desc.getHostname(), desc.getDomain(), jarfiles[i]));
		}
		list.add(build.toString());
	
		// The class to be run in the forked JVM
		if (random)
			list.add("peersim.extras.am.randomsim.RandomSimulator");
		else 
			list.add("peersim.rangesim.RangeSimulator");
		
		// Transfer config file and add the remote string to the
		// local file
		list.add(ft.transfer(done, desc.getHostname(), desc.getDomain(), job));
		
		// Since multiple experiments are managed here, the value
		// of standard variable for multiple experiments is changed to 1
		list.add(Simulator.PAR_EXPS+"=1");
		//list.add(RandomSimulator.PAR_REXPERIMENTS+"=1");
	
		// Add the seed
		list.add(CommonState.PAR_SEED+"="+r.nextLong());
		
		// Max memory for the simulator
		list.add(RangeSimulator.PAR_JVM+"=-Xmx"+desc.getMemory());
		
		// Prepare the argument array for process forking
		String[] newargs = list.toArray(new String[list.size()]);
		//System.out.println(list);
		
		StringBuffer s = new StringBuffer();
		for (int i=0; i < list.size(); i++) {
			s.append(newargs[i]);
			s.append(" ");
		}
		System.out.println(s);
	
		// Execute a new JVM
		try {
			ProcessBuilder pb = new ProcessBuilder(list.toArray(newargs));
			pb.redirectErrorStream(true);
			p = pb.start();
		} catch (IOException e1) {
			System.err.println("Unable to launch a Java virtual machine");
			completed = false;
		}
	
		String jobname = job.getName();
		String baseconfig = jobname.substring(0, jobname.lastIndexOf("."));
		String outname =
			resdir + "/" + baseconfig + "." + String.format("%03d", id) + ".log";
		System.out.println(outname);
		
		// Open a file for writing the log
		PrintStream ps = null;
		try {
			ps = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(outname)));
		} catch (IOException e) {
			System.err.println("Unable to open log file");
			System.exit(1);
		}
		ps.println("Executed by " + desc.getHostname());
		
		// Read the output from the process and redirect it to System.out
		// and System.err.
		BufferedReader toprint = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
		String line;
		int counter = 0;
		while ((line = getLine(toprint)) != null) {
			ps.println(line);
			counter++;
			if (counter == 5)  {
				ps.flush();
				counter = 0;
			}
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) { }
		ps.flush();
	
		completed = (p.exitValue() == 0); 
		
		// The static variable p (used also by ShutdownThread) is back to
		// null - no process must be killed on shutdown.
		p = null;
		
		if (!completed) {
			File f = new File(outname);
			f.delete();
			desc.setCounter(desc.getCounter()+1);
			try {
				Thread.sleep(desc.getCounter()*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			desc.setCounter(0);
		}

		hosts.add(desc);
	}
}

//--------------------------------------------------------------------

private static String getLine(BufferedReader toprint)
{
	try {
		String ret = toprint.readLine();
		//System.out.println(ret);
		return ret;
	} catch (IOException e) {
		e.printStackTrace();
		return null; 
	}
}

//--------------------------------------------------------------------

// Comment inherited from interface
public void doStop()
{
	if (p != null) {
		p.destroy();
	}
}

}
