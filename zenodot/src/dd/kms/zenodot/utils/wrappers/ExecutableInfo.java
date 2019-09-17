package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.matching.TypeMatch;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Wrapper for executables (methods and constructors). Implemented by {@link RegularExecutableInfo}
 * and {@link VariadicExecutableInfo}.<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.
 */
public interface ExecutableInfo
{
	String getName();
	int getNumberOfArguments();
	boolean isVariadic();
	AccessModifier getAccessModifier();
	TypeInfo getDeclaringType();
	boolean isStatic();
	TypeInfo getReturnType();

	boolean isArgumentIndexValid(int argIndex);
	TypeInfo getExpectedArgumentType(int argIndex);
	TypeMatch rateArgumentMatch(List<TypeInfo> argumentTypes);
	Object[] createArgumentArray(List<ObjectInfo> argumentInfos);
	Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException;
	String formatArguments();
}
