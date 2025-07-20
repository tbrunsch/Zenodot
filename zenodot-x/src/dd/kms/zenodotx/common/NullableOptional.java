package dd.kms.zenodotx.common;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public class NullableOptional<T>
{
	private static final NullableOptional<?>	EMPTY	= new NullableOptional<>(null, false);

	@Nullable
	private final T			value;
	private final boolean	present;

	public NullableOptional(@Nullable T value, boolean present) {
		this.value = value;
		this.present = present;
	}

	public static <T> NullableOptional<T> empty() {
		@SuppressWarnings("unchecked")
		NullableOptional<T> t = (NullableOptional<T>) EMPTY;
		return t;
	}

	public static <T> NullableOptional<T> of(@Nullable T value) {
		return new NullableOptional<>(value, true);
	}

	@Nullable
	public T get() {
		if (!present) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	public boolean isPresent() {
		return present;
	}
}
