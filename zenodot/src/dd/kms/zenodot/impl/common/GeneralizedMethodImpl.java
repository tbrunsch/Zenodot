package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.GeneralizedMethod;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class GeneralizedMethodImpl implements GeneralizedMethod
{
	private final Method	method;

	public GeneralizedMethodImpl(Method method) {
		this.method = method;
	}

	@Nullable
	@Override
	public Method getWrappedMethod() {
		return method;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return method.getDeclaringClass();
	}

	@Override
	public String getName() {
		return method.getName();
	}

	@Override
	public int getModifiers() {
		return method.getModifiers();
	}

	@Override
	public boolean isSynthetic() {
		return method.isSynthetic();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return method.getParameterTypes();
	}

	@Override
	public int getParameterCount() {
		return method.getParameterCount();
	}

	@Override
	public boolean isVarArgs() {
		return method.isVarArgs();
	}

	@Override
	public boolean isDefault() {
		return method.isDefault();
	}

	@Override
	public Class<?> getReturnType() {
		return method.getReturnType();
	}

	@Override
	public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.setAccessible(true);
		return method.invoke(obj, args);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeneralizedMethodImpl that = (GeneralizedMethodImpl) o;
		return Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method);
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
