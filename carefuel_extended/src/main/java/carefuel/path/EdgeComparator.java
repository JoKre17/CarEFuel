package carefuel.path;

import java.util.Comparator;

/**
 * Generic Edge Comparator: uses the interface Comparable to compare two Edges
 * 
 * @author josef
 *
 * @param <E>
 */
public class EdgeComparator<E> implements Comparator<Edge<E>> {

	@Override
	public int compare(Edge<E> o1, Edge<E> o2) {
		return o1.compareTo(o2);
	}

}
