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

/*
 * ExtendedStats.java
 *
 * Created on 9 febbraio 2004, 16.10
 */

package peersim.util;

/**
 * This class provides extended statistical informations about the inspected 
 * distribution. In particular, it provides functions to compute the 3rd and
 * 4th degree momentus.
 *
 * @author  Gian Paolo Jesi
 */
public class ExtendedStats extends IncrementalStats {
    
    private double cubicsum, quadsum; // incremental sums
    
    /** Creates a new instance of ExtendedStats */
    public ExtendedStats() {
        super();
        cubicsum = 0.0;
        quadsum = 0.0;
    }
    
    /** Reset all the statistics inside the object. */
    public void reset() {
        super.reset();
        cubicsum = quadsum = 0.0;
    }
    
    /** Add a value to the statistics. 
     * 
     *@param item value to be added to the statistics.
     */
    public void add(double item) {
        super.add(item);
        cubicsum += item * item * item;
        quadsum += item * cubicsum;
    }
   
    /** Output on a single line the superclass statistics plus the third and
     * fourth degree momentus.
     *
     * @return The superclass and its own statistics string.
     */
    public String toString() {
        return super.toString()+" "+momentus3()+" "+momentus4();
    }
    
    /** Computes the degree 3 momentus on the node values distribution and 
     * returns the asymmetry coefficient. It gives an indication about the 
     * distribution symmetry compared to the average.
     *
     *@return The 3rd degree momentus value as a double.
     */ 
    public double momentus3() {
        int n = this.getN();
        double m3 = (((double)n) / (n-1)) * (cubicsum/n - Math.pow(getAverage(), 3) );
        return ( m3 / Math.pow(getStD(), 3 ) );
    }
    
    /** Computes the degree 4 momentus on the node values distribution and 
     *  returns the flatness coefficient. It gives an indication about the 
     *  distribution sharpness or flatness.
     *
     * @return The 4th degree momentus value as a double.
     */ 
    public double momentus4() {
        int n = this.getN();
        double m4 = (((double)n) / (n-1)) * (quadsum/n - Math.pow(getAverage(), 4) );
        return ( m4 / Math.pow(getStD(), 4) )-3;
    }

}
