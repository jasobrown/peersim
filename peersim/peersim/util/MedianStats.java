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

/*
 * MedianIncrementalStats.java
 *
 * Created on 2 novembre 2004, 16.23
 */

package peersim.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**This class adds the ability to retrive the median element to the
 *{@link IncrementalStats} class.
 *
 * @author  giampa
 */
public class MedianStats extends IncrementalStats {
    
    /** Structure to store each entry. */
    private ArrayList data;
    
    /** Creates a new instance of MedianIncrementalStats */
    public MedianStats() {
        data = new ArrayList();
    }
    
    /** Retrives the median in the current data collection.
     *
     *@return The current median value.
     */
    public double getMedian() {
        double result;
        
        if ( data.isEmpty() )
            throw new IllegalStateException( "Data vector is empty!" );
        
        // Sort the arraylist
        Collections.sort(data);
        if (data.size() % 2 != 0) { // odd number
            int index = Math.round( data.size() / 2 );
            result = ( (Double) data.get( index ) ).doubleValue();
        }
        else { // even number:
            double a = ( (Double) data.get( data.size() / 2 ) ).doubleValue();
            double b = ( (Double) data.get( (data.size() / 2) -1) ).doubleValue();
            result = (a+b)/2;
        }
        return result;
    }
    
    /** Adds a value to the current data collection.
     *
     *@param item The item to be added.
     */
    public void add(double item) {
        super.add(item);
        data.add(new Double(item));
    }
    
    /** Resets the structure. */
    public void reset() {
        super.reset();
        if (data != null) data.clear();
    }
    
    public static void main( String[] args ) {
        MedianStats s = new MedianStats();
        Random r = new Random();
        
        for(int i=0; i< 50000; i++)
            s.add(r.nextDouble());
        
        System.out.println("Average: "+s.getAverage());
        System.out.println("Median: "+s.getMedian());
        
    }
}
