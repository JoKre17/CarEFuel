package carefuel.tank;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Implementation of a priority queue, that is sorted after every addition to
 * the queue.
 *
 * @author jwall
 *
 */
public class MyPriorityQueue extends LinkedList<Node> {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Overridden add-Method to sort the list every time a node is added.
	 */
	@Override
	public boolean add(Node newNode) {
		super.add(newNode);
		Collections.sort(this);
		return true;
	}
}
