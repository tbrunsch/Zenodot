package dd.kms.zenodotx.common;

import java.util.Objects;

public class Pair<S, T>
{
	private final S	first;
	private final T	second;

	public Pair(S first, T second) {
		this.first = first;
		this.second = second;
	}

	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
