package dd.kms.zenodot.framework.wrappers;

import com.google.common.base.Joiner;
import dd.kms.zenodot.api.common.GeneralizedExecutable;
import dd.kms.zenodot.api.matching.TypeMatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class ExecutableInfo extends MemberInfo<GeneralizedExecutable>
{
	ExecutableInfo(GeneralizedExecutable executable) {
		super(executable);
	}

	abstract boolean doIsArgumentIndexValid(int argIndex);
	abstract Class<?> doGetExpectedArgumentType(int argIndex);
	abstract TypeMatch doRateArgumentMatch(List<Class<?>> argumentTypes);
	abstract Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos);

	public GeneralizedExecutable getExecutable() {
		return member;
	}

	public int getNumberOfArguments() {
		return member.getParameterCount();
	}

	public boolean isVariadic() {
		return member.isVarArgs();
	}

	public Class<?> getReturnType() {
		return member.getReturnType();
	}

	public final boolean isArgumentIndexValid(int argIndex) {
		return doIsArgumentIndexValid(argIndex);
	}

	public final Class<?> getExpectedArgumentType(int argIndex) {
		return doGetExpectedArgumentType(argIndex);
	}

	public final TypeMatch rateArgumentMatch(List<Class<?>> argumentTypes) {
		return doRateArgumentMatch(argumentTypes);
	}

	public final Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		return doCreateArgumentArray(argumentInfos);
	}

	public Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		if (instance == InfoProvider.INDETERMINATE_VALUE) {
			return InfoProvider.INDETERMINATE_VALUE;
		}
		return member.invoke(instance, arguments);
	}

	public String formatArguments() {
		int numArguments = getNumberOfArguments();
		List<String> argumentTypeNames = new ArrayList<>(numArguments);
		Class<?>[] parameterTypes = member.getParameterTypes();
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
	public String toString() {
		return getName()
				+ "("
				+ formatArguments()
				+ ")";
	}
}
