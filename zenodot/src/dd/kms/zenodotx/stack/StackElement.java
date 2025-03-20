package dd.kms.zenodotx.stack;

class StackElement<T>
{
	private final T					value;
	private final StackElement<T>	previous;

	StackElement(T value, StackElement<T> previous) {
		this.value = value;
		this.previous = previous;
	}

	T getValue() {
		return value;
	}

	StackElement<T> getPrevious() {
		return previous;
	}
}
