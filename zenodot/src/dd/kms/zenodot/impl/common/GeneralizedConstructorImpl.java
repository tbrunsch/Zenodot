package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.GeneralizedConstructor;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class GeneralizedConstructorImpl implements GeneralizedConstructor
{
	private final Constructor<?>	constructor;

	public GeneralizedConstructorImpl(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	@Nonnull
	@Override
	public Constructor<?> getWrappedConstructor() {
		return constructor;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return constructor.getDeclaringClass();
	}

	@Override
	public String getName() {
		return constructor.getName();
	}

	@Override
	public int getModifiers() {
		return constructor.getModifiers();
	}

	@Override
	public boolean isSynthetic() {
		return constructor.isSynthetic();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return constructor.getParameterTypes();
	}

	@Override
	public int getParameterCount() {
		return constructor.getParameterCount();
	}

	@Override
	public boolean isVarArgs() {
		return constructor.isVarArgs();
	}

	@Override
	public Class<?> getReturnType() {
		return getDeclaringClass();
	}

	@Override
	public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			return newInstance(args);
		} catch (InstantiationException e) {
			throw new RuntimeException(constructor + " failed.", e);
		}
	}

	@Override
	public Object newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		constructor.setAccessible(true);
		return constructor.newInstance(initargs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeneralizedConstructorImpl that = (GeneralizedConstructorImpl) o;
		return Objects.equals(constructor, that.constructor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(constructor);
	}

	@Override
	public String toString() {
		return constructor.toString();
	}
}
