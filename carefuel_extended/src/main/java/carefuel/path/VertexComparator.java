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

	Map<Vertex<E>, Double> hCosts;
	
	public VertexComparator(Map<Vertex<E>, Double> hCosts) {
		assert hCosts != null;
		this.hCosts = hCosts;
	}
	
	@Override
	public int compare(Vertex<E> o1, Vertex<E> o2) {
		return hCosts.get(o1).compareTo(hCosts.get(o2));
	}

}
