package dd.kms.zenodot.impl.wrappers;

import com.google.common.base.Joiner;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class AbstractExecutableInfo implements ExecutableInfo
{
	protected final Executable	executable;

	AbstractExecutableInfo(Executable executable) {
		this.executable = executable;
	}

	abstract boolean doIsArgumentIndexValid(int argIndex);
	abstract Class<?> doGetExpectedArgumentType(int argIndex);
	abstract TypeMatch doRateArgumentMatch(List<Class<?>> argumentTypes);
	abstract Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos);

	@Override
	public String getName() {
		return executable.getName();
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	@Override
	public AccessModifier getAccessModifier() {
		return AccessModifier.getValue(executable.getModifiers());
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public int getNumberOfArguments() {
		return executable.getParameterCount();
	}

	@Override
	public boolean isVariadic() {
		return executable.isVarArgs();
	}

	@Override
	public Class<?> getReturnType() {
		return executable instanceof Method ? ((Method) executable).getReturnType() : executable.getDeclaringClass();
	}

	@Override
	public final boolean isArgumentIndexValid(int argIndex) {
		return doIsArgumentIndexValid(argIndex);
	}

	@Override
	public final Class<?> getExpectedArgumentType(int argIndex) {
		return doGetExpectedArgumentType(argIndex);
	}

	@Override
	public final TypeMatch rateArgumentMatch(List<Class<?>> argumentTypes) {
		return doRateArgumentMatch(argumentTypes);
	}

	@Override
	public final Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		return doCreateArgumentArray(argumentInfos);
	}

	@Override
	public Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		if (instance == InfoProvider.INDETERMINATE_VALUE) {
			return InfoProvider.INDETERMINATE_VALUE;
		}
		executable.setAccessible(true);
		return executable instanceof Method	? ((Method) executable).invoke(instance, arguments)
											: ((Constructor<?>) executable).newInstance(arguments);
	}

	@Override
	public String formatArguments() {
		int numArguments = getNumberOfArguments();
		List<String> argumentTypeNames = new ArrayList<>(numArguments);
		Class<?>[] parameterTypes = executable.getParameterTypes();
		for (int i = 0; i < numArguments; i++) {
			Class<?> argumentType = parameterTypes[i];
			String argumentTypeName = i < numArguments - 1 || !isVariadic()
										? argumentType.toString()
										: argumentType.getComponentType().toString() + "...";
			argumentTypeNames.add(argumentTypeName);
		}
		return Joiner.on(", ").join(argumentTypeNames);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractExecutableInfo that = (AbstractExecutableInfo) o;
		return Objects.equals(executable, that.executable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executable);
	}

	@Override
	public String toString() {
		return getName()
				+ "("
				+ formatArguments()
				+ ")";
	}
}
