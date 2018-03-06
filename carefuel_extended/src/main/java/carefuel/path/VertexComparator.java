package carefuel.path;

import java.util.Comparator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generic Vertex Comparator: uses the interface Comparable to compare two
 * Vertices
 * 
 * @author josef
 *
 * @param <E>
 */
public class VertexComparator<E> implements Comparator<Vertex<E>> {

	private final Logger log = LogManager.getLogger(VertexComparator.class);
	
	Map<Vertex<E>, Double> cummulatedCosts;
	
	public VertexComparator(Map<Vertex<E>, Double> cummulatedCosts) {
		assert cummulatedCosts != null;
		
		this.cummulatedCosts = cummulatedCosts;
	}
	
	@Override
	public int compare(Vertex<E> a, Vertex<E> b) {
		Double costA = cummulatedCosts.get(a);
		
		Double costB = cummulatedCosts.get(b);
		
		
		return costA.compareTo(costB);
	}

}
