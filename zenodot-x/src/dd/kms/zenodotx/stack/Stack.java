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
	private StackElement<T>	top	= null;

	public boolean isEmpty() {
		return top == null;
	}

	public void push(T value) {
		top = new StackElement<>(value, top);
	}

	public T pop() {
		T value = peek();
		top = top.getPrevious();
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
		return copy;
	}

	@Override
	public int size() {
		return size(top);
	}

	private int size(StackElement<T> top) {
		return top == null ? 0 : 1 + size(top.getPrevious());
	}

	@Override
	public T get(int index) {
		return get(top, size() - 1 - index);
	}

	private T get(StackElement<T> top, int indexFromBehind) {
		return indexFromBehind == 0 ? top.getValue() : get(top.getPrevious(), indexFromBehind - 1);
	}
}
