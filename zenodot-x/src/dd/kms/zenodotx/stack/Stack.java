package dd.kms.zenodotx.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;

/**
 * A linked list based implementation of a stack that provides a cheap {@link #copy()} method that does not
 * need to copy the whole stack content under the following <b>precondition:</b><br>
 * <br>
 * The elements on the stack must be immutable or at least the caller guarantees not to modify them.
 */
public class Stack<T>
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

	public List<T> asList() {
		List<T> list = new ArrayList<>();
		for (StackElement<T> element = top; element != null; element = element.getPrevious()) {
			list.add(element.getValue());
		}
		Collections.reverse(list);
		return list;
	}
}
