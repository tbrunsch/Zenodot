package dd.kms.zenodot.api.wrappers;

import dd.kms.zenodot.api.matching.TypeMatch;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Wrapper for executables (methods and constructors).
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.
 */
public interface ExecutableInfo extends MemberInfo
{
	int getNumberOfArguments();
	boolean isVariadic();
	Class<?> getReturnType();

	boolean isArgumentIndexValid(int argIndex);
	Class<?> getExpectedArgumentType(int argIndex);
	TypeMatch rateArgumentMatch(List<Class<?>> argumentTypes);
	Object[] createArgumentArray(List<ObjectInfo> argumentInfos);
	Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException;
	String formatArguments();
}
