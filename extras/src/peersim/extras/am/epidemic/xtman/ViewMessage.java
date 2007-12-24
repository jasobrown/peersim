package peersim.extras.am.epidemic.xtman;

import peersim.core.*;
import peersim.extras.am.epidemic.*;

/**
 * This class represents a generic message containining a partial
 * view. It is considered just a container, so get/set methods are
 * not provided.
 */
public class ViewMessage extends AbstractMessage
{

//---------------------------------------------------------------------
//Variables
//---------------------------------------------------------------------

public Node[] nodes;

public int size;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------


public ViewMessage(int maxsize)
{
	nodes = new Node[maxsize];
	size = 0;
}

}