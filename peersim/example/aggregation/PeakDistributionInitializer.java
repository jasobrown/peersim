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

package example.aggregation;

import peersim.config.*;
import peersim.core.*;
import peersim.vector.SingleValue;

/**
 * Initialize an aggregation protocol using a peak distribution; only one peak
 * is allowed. However, any protocol implementing
 * {@link peersim.vector.SingleValue} can be initialized by this component.
 * 
 * @author Alberto Montresor
 * @version $Revision$
 */
public class PeakDistributionInitializer implements Control {

    // --------------------------------------------------------------------------
    // Constants
    // --------------------------------------------------------------------------

    /**
     * The load at the peak node.
     * 
     * @config
     */
    private final String PAR_VALUE = "value";

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private final String PAR_PROT = "protocol";

    // --------------------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------------------

    /** Value at the peak node; obtained from config property {@link #PAR_VALUE}. */
    private final double value;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // --------------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public PeakDistributionInitializer(String prefix) {
        value = Configuration.getDouble(prefix + "." + PAR_VALUE);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    // --------------------------------------------------------------------------
    // Methods
    // --------------------------------------------------------------------------

    // Comment inherited from interface
    public boolean execute() {
        System.err.println("Restarting: " + Network.size());
        for (int i = 0; i < Network.size(); i++) {
            SingleValue prot = (SingleValue) Network.get(i).getProtocol(pid);
            prot.setValue(0);
        }
        SingleValue prot = (SingleValue) Network.get(0).getProtocol(pid);
        prot.setValue(value);

        return false;
    }

    // --------------------------------------------------------------------------

}
