package dd.kms.zenodot.api.common;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Wrapper around a {@link java.lang.reflect.Constructor}.
 */
public interface GeneralizedConstructor extends GeneralizedExecutable
{
	static GeneralizedConstructor fromConstructor(Constructor<?> constructor) {
		return new dd.kms.zenodot.impl.common.GeneralizedConstructorImpl(constructor);
	}

	@Nonnull
	@Override
	default Constructor<?> getWrappedExecutable() {
		return getWrappedConstructor();
	}

	@Nonnull
	Constructor<?> getWrappedConstructor();

	Object newInstance(Object ... initargs) throws InstantiationException, IllegalAccessException,
		IllegalArgumentException, InvocationTargetException;
}
