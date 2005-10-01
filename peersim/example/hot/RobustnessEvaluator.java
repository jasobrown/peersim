/*
 * RobustnessEvaluator.java
 *
 * Created on 21 aprile 2004, 15.05
 */

package example.hot;

/**
 * 
 * @author giampa
 */
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import peersim.graph.Graph;
import peersim.graph.GraphAlgorithms;
import peersim.core.Node;

public class RobustnessEvaluator
{

class NodesPair
{

public NodesPair(Graph g)
{
	GraphAlgorithms gal = new GraphAlgorithms();
	do {
		src = lcg.nextInt(g.size());
		dst = lcg.nextInt(g.size());
		gal.dist(g, src);
	} while (src == dst || gal.d[dst] == -1);
}

protected int src;

protected int dst;

public boolean equals(Object o)
{
	if (!(o instanceof NodesPair))
		return false;
	else {
		NodesPair pair = (NodesPair) o;
		return (src == pair.src) && (dst == pair.dst);
	}
}
}

protected Random lcg = new Random();

private final int pairsNo = 100;

private Set initPairs(Graph g)
{
	HashSet<NodesPair> pairs = new HashSet<NodesPair>(pairsNo);
	while (pairs.size() < pairsNo)
		pairs.add(new NodesPair(g));
	return pairs;
}

public int[] computeStamps(Graph g)
{
	Set pairs = initPairs(g);
	int[] stamps = new int[pairsNo];
	boolean[] alreadyRemoved = new boolean[g.size()];
	for (int j = 0; j < alreadyRemoved.length; j++)
		alreadyRemoved[j] = false;
	for (int j = 0; j < stamps.length; j++)
		stamps[j] = -1;
	int residualPairs = pairsNo;
	time = 0;
	Object[] objPairs = pairs.toArray();
	GraphAlgorithms gal = new GraphAlgorithms();
	while (residualPairs > 0) {
		if ((time % 10) == 0)
			System.out.print(".");
		int i = lcg.nextInt(g.size());
		if (alreadyRemoved[i])
			continue;
		alreadyRemoved[i] = true;
		((Node) g.getNode(i)).setFailState(peersim.core.Fallible.DOWN);

		for (int j = 0; j < pairsNo; j++) {
			if (stamps[j] != -1)
				continue;
			NodesPair n = (NodesPair) objPairs[j];
			gal.dist(g, n.src);
			if (gal.d[n.dst] == -1) {
				stamps[j] = time;
				residualPairs--;
				// System.out.print("("+ n.src + "," + n.dst + ")");
			}
		}
		time++;
	}

	for (int i = 0; i < g.size(); i++) {
		((Node) g.getNode(i)).setFailState(peersim.core.Fallible.OK);

	}

	return stamps;
}

private float avg;

private int[] stamps;

private int time;

private Graph g;

public RobustnessEvaluator(Graph g)
{
	this.g = g;
	stamps = computeStamps(g);
	avg = 0;
	for (int i = 0; i < stamps.length; i++)
		avg += stamps[i];
	avg /= pairsNo;
}

/**
 * @return Average number of nodes that have to fail before a random pair
 *         of nodes becomes disconnected.
 */
public float getMetric1()
{
	return avg;
}

/**
 * @return An array filled with the number of unconnected components as
 *         nodes crash.
 * 
 */
public long[] getMetric2()
{
	long[] a = new long[g.size()];

	for (int i = 0; i < stamps.length; i++) {
		a[stamps[i]]++;
	}

	int sum = 0;
	for (int i = 0; i < a.length; i++) {
		a[i] = a[i] + sum;
		sum += a[i];
	}
	return a;
}
}
