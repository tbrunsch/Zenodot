package dd.kms.zenodot.impl.settings;

class Triple<S, T, U>
{
	private final S	first;
	private final T	second;
	private final U	third;

	Triple(S first, T second, U third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	S getFirst() {
		return first;
	}

	T getSecond() {
		return second;
	}

	U getThird() {
		return third;
	}
}
