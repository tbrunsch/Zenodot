package dd.kms.zenodot.framework.wrappers;

import com.google.common.base.Joiner;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ExecutableInfo extends MemberInfo<Executable>
{
	ExecutableInfo(Executable executable) {
		super(executable);
	}

	abstract boolean doIsArgumentIndexValid(int argIndex);
	abstract Class<?> doGetExpectedArgumentType(int argIndex);
	abstract TypeMatch doRateArgumentMatch(List<Class<?>> argumentTypes);
	abstract Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos);

	public Executable getExecutable() {
		return member;
	}

	public int getNumberOfArguments() {
		return member.getParameterCount();
	}

	public boolean isVariadic() {
		return member.isVarArgs();
	}

	public Class<?> getReturnType() {
		return member instanceof Method ? ((Method) member).getReturnType() : member.getDeclaringClass();
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
		Executable executable = member;
		try {
			executable.setAccessible(true);
		} catch (Exception e) {
			boolean solvedAccessibilityIssue = false;
			if (executable instanceof Method) {
				/*
				 * It is possible that we cannot make the method accessible because its
				 * class is not accessible, but it overrides a method of a base class that
				 * is accessible. Hence, we try all base methods.
				 *
				 * Example: Consider a List "list" and the expression "list.stream().filter(o -> true)".
				 *          "list.stream()" returns some implementation of Stream, e.g., ReferencePipeline$Head,
				 *          that is most likely inaccessible. The Method ReferencePipeline$Head.filter()
				 *          cannot be made accessible in Java 17 for this reason. However, the Method
				 *          Stream.filter() is accessible. Hence, we can replace Method ReferencePipeline$Head.filter()
				 *          by Method Stream.filter() and invoke that one.
				 */
				Collection<Method> baseMethods = ReflectionUtils.getBaseMethods((Method) executable);
				for (Method baseMethod : baseMethods) {
					try {
						baseMethod.setAccessible(true);
						executable = baseMethod;
						solvedAccessibilityIssue = true;
						break;
					} catch (Exception ignored) {
						/* try next base method */
					}
				}
			}
			if (!solvedAccessibilityIssue) {
				throw e;
			}
		}
		return executable instanceof Method	? ((Method) executable).invoke(instance, arguments)
											: ((Constructor<?>) executable).newInstance(arguments);
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
