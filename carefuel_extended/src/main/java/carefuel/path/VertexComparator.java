package carefuel.path;

import java.util.Comparator;

public abstract class VertexComparator<E> implements Comparator<Vertex<E>> {

	@Override
	public int compare(Vertex<E> o1, Vertex<E> o2) {
		return o1.compareTo(o2);
	}

}
