/* @author rich
 * Created on 05-Mar-2004
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package peersim.config;
import java.math.*;
import org.lsmp.djep.groupJep.groups.*;
import org.lsmp.djep.groupJep.interfaces.*;

/**
 * The group of integers, implemented as a BigInteger.
 * @see java.math.BigInteger
 * @author Rich Morris
 * Created on 05-Mar-2004
 * Modified on 17-May-2005 by Alberto Montresor
 * (constant values are read first as BigDecimal, and then transformed
 * as BigInteger; in this way we avoid NumberFormatException when
 * reading values such as 10.0.
 */
class Integers extends Group implements IntegralDomainI,HasDivI,
	OrderedSetI,HasModI,HasPowerI {
	
	/**
	 * Operations on the reals (Implemented as BigInteger).
	 */
	public Integers() {
	}

	public Number getZERO() {
		return BigInteger.ZERO;
	}

	public Number getONE() {
		return BigInteger.ONE;
	}

	public Number getInverse(Number num) {
		BigInteger a = (BigInteger) num;
		return a.negate();
	}

	public Number add(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.add(b);
	}

	public Number sub(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.subtract(b);
	}

	public Number mul(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.multiply(b);
	}

	public Number div(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.divide(b);
	}
	/* note -3 mod 2 is 1 rather than -1 as for % in java language specifications. */ 
	public Number mod(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.remainder(b);
	}
	
	public Number pow(Number num1, Number num2) {
		BigInteger a = (BigInteger) num1;
		BigInteger b = (BigInteger) num2;
		return a.pow(b.intValue());
	}
	public boolean equals(Number a,Number b)	{
		return ((BigInteger) a).compareTo((BigInteger) b) == 0;
	}
	
	public int compare(Number a,Number b)	{
		return ((BigInteger) a).compareTo((BigInteger) b);
	}

	public Number valueOf(String str) {
		BigDecimal bd = new BigDecimal(str);
		// XXX If we adopt java 1.5, we can also round the values with
		// arbitrary precision, thus avoiding floating point errors in
		// some tricky conversions
		return bd.toBigInteger();
	}
	
	public String toString() { return "Z: integers"; }
}
