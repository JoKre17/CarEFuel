package carefuel.path;

import java.util.Comparator;

public abstract class EdgeComparator<E> implements Comparator<Edge<E>> {

	@Override
	public int compare(Edge<E> o1, Edge<E> o2) {
		return o1.compareTo(o2);
	}

}
