package dd.kms.zenodot.utils.wrappers;

import com.google.common.base.Joiner;
import dd.kms.zenodot.matching.TypeMatch;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class AbstractExecutableInfo implements ExecutableInfo
{
	protected final Executable	executable;
	private final TypeInfo		declaringType;

	AbstractExecutableInfo(Executable executable, TypeInfo declaringType) {
		this.executable = executable;
		this.declaringType = declaringType;
	}

	abstract boolean doIsArgumentIndexValid(int argIndex);
	abstract Type doGetExpectedArgumentType(int argIndex);
	abstract TypeMatch doRateArgumentMatch(List<TypeInfo> argumentTypes);
	abstract Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos);

	@Override
	public String getName() {
		return executable.getName();
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
	public TypeInfo getDeclaringType() {
		return declaringType;
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	@Override
	public TypeInfo getReturnType() {
		return	executable instanceof Method	? declaringType.resolveType(((Method) executable).getGenericReturnType()) :
		executable instanceof Constructor<?> 	? declaringType
												: InfoProvider.NO_TYPE;
	}

	@Override
	public final boolean isArgumentIndexValid(int argIndex) {
		return doIsArgumentIndexValid(argIndex);
	}

	@Override
	public final TypeInfo getExpectedArgumentType(int argIndex) {
		return declaringType.resolveType(doGetExpectedArgumentType(argIndex));
	}

	@Override
	public final TypeMatch rateArgumentMatch(List<TypeInfo> argumentTypes) {
		return doRateArgumentMatch(argumentTypes);
	}

	@Override
	public final Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		return doCreateArgumentArray(argumentInfos);
	}

	@Override
	public Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		executable.setAccessible(true);
		return	executable instanceof Method			? ((Method) executable).invoke(instance, arguments) :
				executable instanceof Constructor<?>	? ((Constructor<?>) executable).newInstance(arguments)
														: null;
	}

	@Override
	public String formatArguments() {
		int numArguments = getNumberOfArguments();
		List<String> argumentTypeNames = new ArrayList<>(numArguments);
		for (int i = 0; i < numArguments; i++) {
			TypeInfo argumentType = declaringType.resolveType(executable.getGenericParameterTypes()[i]);
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
		return Objects.equals(executable, that.executable) &&
				Objects.equals(declaringType, that.declaringType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executable, declaringType);
	}

	@Override
	public String toString() {
		return getName()
				+ "("
				+ formatArguments()
				+ ")";
	}
}
