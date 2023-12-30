package dd.kms.zenodot.impl.completionproviders;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

class NullableOptional<T>
{
	private static final NullableOptional<?>	EMPTY	= new NullableOptional<>(null, false);

	@Nullable
	private final T			value;
	private final boolean	present;

	NullableOptional(@Nullable T value, boolean present) {
		this.value = value;
		this.present = present;
	}

	static <T> NullableOptional<T> empty() {
		@SuppressWarnings("unchecked")
		NullableOptional<T> t = (NullableOptional<T>) EMPTY;
		return t;
	}

	static <T> NullableOptional<T> of(@Nullable T value) {
		return new NullableOptional<>(value, true);
	}

	@Nullable
	T get() {
		if (!present) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	boolean isPresent() {
		return present;
	}
}
