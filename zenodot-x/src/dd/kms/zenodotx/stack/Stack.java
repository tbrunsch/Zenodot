package dd.kms.zenodotx.stack;

import java.util.*;

/**
 * A linked list based implementation of a stack that provides a cheap {@link #copy()} method that does not
 * need to copy the whole stack content under the following <b>precondition:</b><br>
 * <br>
 * The elements on the stack must be immutable or at least the caller guarantees not to modify them.
 */
public class Stack<T> extends AbstractList<T>
{
	private StackElement<T>	top				= null;
	private int				size			= 0;
	private List<T>			cachedListView	= null;

	public boolean isEmpty() {
		return size == 0;
	}

	public void push(T value) {
		top = new StackElement<>(value, top);
		size++;
		cachedListView = null;
	}

	public T pop() {
		T value = peek();
		top = top.getPrevious();
		size--;
		cachedListView = null;
		return value;
	}

	public T peek() {
		if (isEmpty()) {
			throw new EmptyStackException();
		}
		return top.getValue();
	}

	public Stack<T> copy() {
		Stack<T> copy = new Stack<>();
		copy.top = top;
		copy.size = size;
		copy.cachedListView = cachedListView;

		return copy;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public T get(int index) {
		if (cachedListView == null) {
			cachedListView = createListView();
		}
		return cachedListView.get(index);
	}

	private List<T> createListView() {
		Object[] elements = new Object[size];
		int index = size;
		for (StackElement<T> elem = top; elem != null; elem = elem.getPrevious()) {
			elements[--index] = elem.getValue();
		}
		return Arrays.asList((T[]) elements);
	}
}
