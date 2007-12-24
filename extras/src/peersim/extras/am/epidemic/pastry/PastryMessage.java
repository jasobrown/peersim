package peersim.extras.am.epidemic.pastry;

import java.util.*;

import peersim.extras.am.epidemic.xtman.*;



/**
 * 
 */
public class PastryMessage extends ViewMessage
{

BitSet bitset;

public PastryMessage(int msgsize, int nfingers)
{
	super(msgsize);
	bitset = new BitSet(nfingers);
}

}