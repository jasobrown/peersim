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
import peersim.config.*;
import peersim.rangesim.ProcessManager;

/**
 * This class is the main class for the Master Simulator.  
 * This simulator is capable to divide the workload between
 * a specified set of machines.
 *  
 * @author Alberto Montresor
 * @version $Revision$
 */
public class MasterSimulator
{


// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------

/**
 * @config
 */
private static final String PAR_FILETRANSFER = "transfer";

/** 
 * If true, uses the random simulator instead of the range simulator 
 * @config
 */
private static final String PAR_RANDOM = "random";


//--------------------------------------------------------------------------
//Constants
//--------------------------------------------------------------------------

/** Default name of the file containing the hosts to be used */
private static final String HOSTS = "hosts";

/** Default name of the file containing the hosts to be used */
private static final String CONFIG = "config";

/** Configuration file filter */
private static final ExtensionFilter cfgfilter = 
	new ExtensionFilter(".cfg");

/** Jar file filter */
private static final ExtensionFilter jarfilter = 
	new ExtensionFilter(".jar");

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

/**
 * Main method of the system.
 */
public static void main(String[] args)
{
	if (args.length != 2 || "-h".equals(args[0])) {
		usage();
		System.exit(1);
	}
	
	File srcdir = new File(args[0]);
	File dstdir = new File(args[1]);
	if (!srcdir.isDirectory()) {
		System.out.println(srcdir + " is not a directory");
		usage();
		System.exit(1);
	}

	if (!dstdir.isDirectory()) {
		System.out.println(dstdir + " is not a directory");
		usage();
		System.exit(1);
	}

	while (true) {
		String jobname = selectJobDir(srcdir, dstdir);
		if (jobname != null) 
			executeJobDir(srcdir, jobname, dstdir);
		else {
			try { 
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}
}

private static void usage()
{
	System.err.println("Usage:");
	System.err.println("  peersim.rangesim.MasterSimulator <SrcDirectory> <OutDirectory>");
}

private static String selectJobDir(File srcdir, File dstdir)
{
	File[] files = srcdir.listFiles();
	for (int i = 0; i < files.length; i++) {
		if (files[i].isDirectory() && !contains(dstdir, files[i].getName()))
			return files[i].getName();
	}
	return null;
}

private static boolean contains(File dstdir, String filename)
{
	String[] files = dstdir.list();
	for (int i=0; i < files.length; i++) {
		if (files[i].equals(filename))
			return true;
	}
	return false;
}

private static void executeJobDir(File srcdir, String jobname, 
		File dstdir)
{
	// Create directories
	File jobsrc = new File(srcdir, jobname);
	File jobdst = new File(dstdir, jobname);
	if (!jobdst.mkdirs()) {
		System.err.println("Impossible to create output directory " +
				jobdst + " for job " + jobsrc);
		return;
	}

	// Parse the config file
	ConfigContainer cc = readConfig(new File(jobsrc, CONFIG));
	if (cc == null)
		return;
	
	// Read common configuration parameters
	FileTransfer ft = (FileTransfer) cc.getInstance(PAR_FILETRANSFER);
	int random = cc.getInt(PAR_RANDOM, 0);
	
	// Host file: common to all jobs
	File hostfile = new File(jobsrc, HOSTS);
	Heap hosts = null;
	try {
		hosts = parseHostFile(hostfile);
	} catch (IOException e) {
		System.err.println("Impossible to execute job " + jobsrc);
		return;
	}
	if (hosts == null || hosts.size()==0) {
		System.out.println("No hosts in " + hostfile);
		return;
	}
	
	// Jar files: common the all jobs
	File[] j1 = jobsrc.listFiles(jarfilter);
	File[] j2 = srcdir.listFiles(jarfilter);
	File[] jarfiles = new File[j1.length + j2.length];
	System.arraycopy(j1, 0, jarfiles, 0, j1.length);
	System.arraycopy(j2, 0, jarfiles, j1.length, j2.length);
	
	// Domain list
	Set domains = new HashSet();
	
	// Create shutdown thread 
	// Shutdown-thread management
	ProcessManager pm = new ProcessManager();
	Runtime.getRuntime().addShutdownHook(pm);
	
	// Start all jobs
	File[] jobs = jobsrc.listFiles(cfgfilter);
	int[] exps = new int[jobs.length];
	int tot = 0;
	for (int i = 0; i < jobs.length; i++) {
		cc = readConfig(jobs[i]);
		if (random > 0) {
			exps[i] = random;
		} else {
			exps[i] = cc.getInt(Simulator.PAR_EXPS);
		}
  	tot += exps[i];
	}
	while (tot > 0) {
		for (int i = 0; i < jobs.length; i++) { 
			if (exps[i] > 0) {
				exps[i]--;
				tot--;
				ExecutionThread t = new ExecutionThread(jobsrc, jobs[i], 
						ft, hosts, jarfiles, domains, jobdst, tot, random>0);
				pm.addThread(t);
				t.start();
			}
		}
	}
	
	// Wait for all jobs to terminate
	pm.joinAll();
	Runtime.getRuntime().removeShutdownHook(pm);
}

private static ConfigContainer readConfig(File file)
{
	String args[] = new String[] { file.getAbsolutePath() };
	try {
		return new ConfigContainer(new ParsedProperties(args), false);
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
}	

/**
 * Read the specified host file and returns an Heap data structure
 * containing them.
 * @param file the file to be read
 * @throws IOException
 */
private static Heap parseHostFile(File file) throws IOException
{
	Heap hosts = new Heap();
	List<HostDescriptor> a = new ArrayList<HostDescriptor>();
	BufferedReader f = new BufferedReader(new FileReader(file));
	String line;
	while ((line = f.readLine()) != null) {
		line = line.trim();
		if (!line.startsWith("#"))
			a.add(new HostDescriptor(line));
	}
	f.close();

	int size = a.size();
	hosts = new Heap(size);
	for (int i = 0; i < size; i++) {
		hosts.add(a.get(i));
	}
	return hosts;
}

}
