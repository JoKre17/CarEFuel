package carefuel.util;

import java.util.Collections;
import java.util.LinkedList;

public class MyPriorityQueue extends LinkedList<Node> {

	@Override
	public boolean add(Node newNode) {
		super.add(newNode);
		Collections.sort(this);
		return true;
	}
}
