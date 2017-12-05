package carefuel.path;

import java.util.Comparator;

/**
 * Generic Vertex Comparator: uses the interface Comparable to compare two
 * Vertices
 * 
 * @author josef
 *
 * @param <E>
 */
public class VertexComparator<E> implements Comparator<Vertex<E>> {

	@Override
	public int compare(Vertex<E> o1, Vertex<E> o2) {
		return o1.compareTo(o2);
	}

}
