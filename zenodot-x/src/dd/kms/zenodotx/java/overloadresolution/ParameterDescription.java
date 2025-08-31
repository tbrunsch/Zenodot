package dd.kms.zenodotx.java.overloadresolution;

import java.util.List;

class ParameterDescription
{
	private final List<Class<?>>	parameterTypes;
	private final boolean			variadic;

	ParameterDescription(List<Class<?>> parameterTypes, boolean variadic) {
		this.parameterTypes = parameterTypes;
		this.variadic = variadic;
	}

	List<Class<?>> getParameterTypes() {
		return parameterTypes;
	}

	boolean isVariadic() {
		return variadic;
	}
}
