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

package example.loadbalance;

import peersim.core.*;
import peersim.reports.*;
import peersim.util.Log;
import peersim.util.IncrementalStats;
import peersim.util.CommonRandom;
import peersim.config.*;

public class LBObserver implements Observer {
    // Constant fields:
    /**
     *  String name of the parameter used to determine the accuracy
     *  for standard deviation before stopping the simulation. If not
     *  defined, a negative value is used which makes sure the observer
     *  does not stop the simulation
     */
    
    public static final String PAR_ACCURACY = "accuracy";
    
    /**
     *  String name of the parameter used to select the protocol to operate on
     */
    public static final String PAR_PROT = "protocol";
    
    /**
     *  String name of the parameter used to print or not the node load
     * value. The default is NOT show.
     */
    public static final String PAR_SHOW_VALUES = "show_values";
    
    // Fields:
    /** The name of this observer in the configuration */
    private final String name;
    
    /** Accuracy for standard deviation used to stop the simulation */
    private final double accuracy;
    
    /** Protocol identifier */
    private final int pid;
    
    /** Initial standard deviation */
    private double initsd = -1.0;
    
    /** Flag to show or not the load values at each node. */
    private int show_values = 0;
    
    private IncrementalStats stats = null;
    
    //private LoadBalance targetp;
    private int target_node;
    private final int len = Network.size();
    
    // Constructor:
    public LBObserver(String name) {
        // Call the parent class (abstract class)
        this.name = name;
        // Other parameters from config file:
        accuracy = Configuration.getDouble(name+"."+PAR_ACCURACY, -1);
        pid = Configuration.getPid(name + "." + PAR_PROT);
        show_values = Configuration.getInt(name + "." + PAR_SHOW_VALUES, 0);
        stats = new IncrementalStats();
        target_node = CommonRandom.r.nextInt(len);
    }
    
    // Implementation of the Observer Interface:
    
    //Returns always true!
    public boolean analyze() {
        //	final int len = Network.size();
        double sum = 0.0;
        double sqrsum = 0.0;
        double agavg = 0.0;
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        int count_zero = 0;
        int count_avg = 0;
        int temp_avg = 0;
        int target_node = 0; // designated node
        double target_node_load = 0.0; // load of a designated node
        
        
        //target_node_load = targetp.getLocalLoad();
        /* Compute max, min, average */
        for (int i = 0; i < len; i++) {
            SingleValue prot = (SingleValue)Network.get(i).getProtocol(pid);
            double value = prot.getValue();
            stats.add(value);
            
            if (value == 0) { count_zero++; }
            if (value == 2) { count_avg++;}
            // shows the values of load at each node:
            if (show_values == 1) { System.out.print(value+":"); }
            sum += value;
            if (value > max) max = value;
            if (value < min) min = value;
            
            //agavg = protocol.getAVGLoad();
        }
        
        temp_avg = (int)(sum/len);
        Log.println(name,
        CommonState.getTime() + " " + // cycle identifier
        stats.getAverage() +" " +
        stats.getMax() +" "+
        stats.getMin() +" "+
        //    sum/len + " " + // average
        // max + " " +
        // min + " " +
        count_zero +" " + // number of zero value node
        count_avg +" "+// number of correct avg nodes
        stats.getVar()
        //target_node_load
        //agavg
        );
        stats.reset();
        return false;
        
    }
    
    // End of interface implementation
    //*******************************
}

