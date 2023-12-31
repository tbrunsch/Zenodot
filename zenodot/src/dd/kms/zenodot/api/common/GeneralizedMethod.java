package dd.kms.zenodot.api.common;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Represents a {@link Method} or an extension method.
 */
public interface GeneralizedMethod extends GeneralizedExecutable
{
	@Nullable
	@Override
	default Method getWrappedExecutable() {
		return getWrappedMethod();
	}

	/**
	 * @return The underlying {@code Method}, if available, or {@code null} otherwise. Note that
	 * there is currently only one case in which there is no underlying method: If the
	 * {@code GeneralizedExecutable} represents an extension method.
	 */
	@Nullable
	Method getWrappedMethod();

	/**
	 * @see Method#isDefault()
	 */
	boolean isDefault();
}
