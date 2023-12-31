package dd.kms.zenodot.impl.common;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.common.GeneralizedMethod;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ExtensionMethod implements GeneralizedMethod
{
	private static final int	ACCESS_MODIFIERS	= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
	private static final int	METHOD_MODIFIERS	= ACCESS_MODIFIERS | Modifier.STATIC;

	private final Class<?>				declaringClass;
	private final Class<?>				returnType;
	private final String				name;
	private final int					modifiers;
	private final Class<?>[]			parameterTypes;
	private final boolean				varArgs;
	private final ExtensionMethodBody	extensionMethodBody;

	public ExtensionMethod(Class<?> declaringClass, Class<?> returnType, String name, int modifiers, Class<?>[] parameterTypes, boolean varArgs, ExtensionMethodBody extensionMethodBody) {
		if (varArgs) {
			Preconditions.checkArgument(parameterTypes.length > 0, "Variadic methods must have at least one parameter");
			Preconditions.checkArgument(parameterTypes[parameterTypes.length - 1].getComponentType() != null, "The last parameter type of a variadic method must be an array type");
		}
		this.declaringClass = declaringClass;
		this.returnType = returnType;
		this.name = name;
		this.modifiers = modifiers;
		this.parameterTypes = parameterTypes;
		this.varArgs = varArgs;
		this.extensionMethodBody = extensionMethodBody;
	}

	@Nullable
	@Override
	public Method getWrappedMethod() {
		return null;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isSynthetic() {
		return true;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return parameterTypes.clone();
	}

	@Override
	public int getParameterCount() {
		return parameterTypes.length;
	}

	@Override
	public boolean isVarArgs() {
		return varArgs;
	}

	/**
	 * Returns true if this method is a default method; returns false otherwise. A default method is a
	 * public non-abstract instance method, that is, a non-static method with a body, declared in an
	 * interface type.
	 */
	@Override
	public boolean isDefault() {
		// Default methods are public non-abstract instance methods
		// declared in an interface.
		return ((getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
			Modifier.PUBLIC) && getDeclaringClass().isInterface();
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public Object invoke(Object obj, Object... args) throws IllegalArgumentException, InvocationTargetException {
		if (Modifier.isStatic(modifiers)) {
			Preconditions.checkArgument(obj == null, "Trying to invoke static method " + this + " on non-null object");
		} else {
			if (obj == null) {
				throw new NullPointerException("Trying to invoke instance method " + this + " on null");
			}
			Preconditions.checkArgument(getDeclaringClass().isInstance(obj), "Trying to invoke instance method " + this + " declared on " + getDeclaringClass().getName() + " on an instance of type " + obj.getClass().getName());
		}
		int parameterCount = getParameterCount();
		if (isVarArgs()) {
			Preconditions.checkArgument(args.length >= parameterCount, "Invalid number of parameters for " + this + ": " + args.length);
		} else {
			Preconditions.checkArgument(args.length == parameterCount, "Invalid number of parameters for " + this + ": " + args.length);
		}

		for (int i = 0; i < args.length; i++) {
			final Class<?> parameterType;
			final Class<?> alternativeParameterType;
			if (i < parameterCount - 1 || !isVarArgs()) {
				parameterType = alternativeParameterType = parameterTypes[i];
			} else if (i == parameterCount - 1) {
				parameterType = parameterTypes[i].getComponentType();
				alternativeParameterType = parameterTypes[i];
			} else {
				parameterType = alternativeParameterType = parameterTypes[i].getComponentType();
			}

			Object arg = args[i];
			if (parameterType.isPrimitive() && alternativeParameterType.isPrimitive()) {
				Preconditions.checkNotNull(arg, "Parameter i of method " + this + " must be of type " + parameterType.getName());
			}
			Preconditions.checkArgument(arg == null || parameterType.isInstance(arg) || alternativeParameterType.isInstance(arg),
				"Parameter i of method " + this + " cannot be cast to " + parameterType.getName() + (alternativeParameterType == parameterType ? "" : " or " + alternativeParameterType.getName())
			);
		}

		return extensionMethodBody.execute(obj, args);
	}

	/**
	 * adapted from {@link java.lang.reflect.Method#toString()}
	 */
	@Override
	public String toString() {
		try {
			StringBuilder builder = new StringBuilder();

			int modifiers = getModifiers() & METHOD_MODIFIERS;

			if (modifiers != 0 && !isDefault()) {
				builder.append(Modifier.toString(modifiers)).append(' ');
			} else {
				int accessModifiers = modifiers & ACCESS_MODIFIERS;
				if (accessModifiers != 0) {
					builder.append(Modifier.toString(accessModifiers)).append(' ');
				}
				if (isDefault()) {
					builder.append("default ");
				}
				modifiers = (modifiers & ~ACCESS_MODIFIERS);
				if (modifiers != 0) {
					builder.append(Modifier.toString(modifiers)).append(' ');
				}
			}

			builder.append(getReturnType().getTypeName()).append(' ');
			builder.append(getDeclaringClass().getTypeName()).append('.');
			builder.append(getName());

			builder.append('(');

			for (int j = 0; j < parameterTypes.length; j++) {
				builder.append(parameterTypes[j].getTypeName());
				if (j < parameterTypes.length - 1) {
					builder.append(",");
				}
			}

			builder.append(')');

			return builder.toString();
		} catch (Exception e) {
			return "<" + e + ">";
		}
	}

	@FunctionalInterface
	interface ExtensionMethodBody
	{
		Object execute(Object obj, Object... args) throws InvocationTargetException;
	}
}
