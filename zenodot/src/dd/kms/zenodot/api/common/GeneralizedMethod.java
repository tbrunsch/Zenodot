package dd.kms.zenodot.api.common;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Represents a {@link Method} or an extension method.
 */
public interface GeneralizedMethod extends GeneralizedExecutable
{
	static GeneralizedMethod fromMethod(Method method) {
		return new dd.kms.zenodot.impl.common.GeneralizedMethodImpl(method);
	}

	static GeneralizedMethod createExtensionMethod(Class<?> declaringClass, String name, int modifiers, Class<?> returnType, Class<?>[] parameterTypes, boolean varArgs, ExtensionMethodBody extensionMethodBody) {
		return new dd.kms.zenodot.impl.common.ExtensionMethod(declaringClass, name, modifiers, returnType, parameterTypes, varArgs, extensionMethodBody);
	}

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
