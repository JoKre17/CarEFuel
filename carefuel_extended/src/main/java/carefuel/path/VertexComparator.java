package carefuel.path;

import java.util.Comparator;
import java.util.Map;

/**
 * Generic Vertex Comparator: uses the interface Comparable to compare two
 * Vertices
 * 
 * @author josef
 *
 * @param <E>
 */
public class VertexComparator<E> implements Comparator<Vertex<E>> {

	Map<Vertex<E>, Double> gCosts;
	Map<Vertex<E>, Double> hCosts;
	
	public VertexComparator(Map<Vertex<E>, Double> gCosts, Map<Vertex<E>, Double> hCosts) {
		assert gCosts != null;
		assert hCosts != null;
		
		this.gCosts = gCosts;
		this.hCosts = hCosts;
	}
	
	@Override
	public int compare(Vertex<E> a, Vertex<E> b) {
		Double gA = gCosts.get(a);
		Double hA = hCosts.get(a);
		
		Double gB = gCosts.get(b);
		Double hB = hCosts.get(b);
		
		if(gA != null) {
			hA += gA;
		}
		
		if(gB != null) {
			hB += gB;
		}
		
		return hA.compareTo(hB);
	}

}
