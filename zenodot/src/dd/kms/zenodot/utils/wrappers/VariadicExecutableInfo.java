package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.TypeMatch;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * "Variadic" view on variadic executables (methods, constructors). This view only
 * considers the variadic nature of a variadic executable and does not accept arrays
 * instead of the variadic part. Arrays are accepted by the "regular" view
 * {@link RegularExecutableInfo} of variadic executables.<br/>
 * <br/>
 * It turned out that dealing with views on executables avoids cumbersome case distinctions
 * for variadic methods: Instead of assuming that there is one (variadic) executable available,
 * we pretend that there are two available: a regular executable and a purely variadic executable.
 * Since overloaded methods had to be handled anyway, no extra code had to be written.
 */
public class VariadicExecutableInfo extends AbstractExecutableInfo
{
	VariadicExecutableInfo(Executable executable, TypeInfo declaringType) {
		super(executable, declaringType);

		assert isVariadic() : "Cannot create VariadicExecutableInfo for non-variadic methods";
	}

	@Override
	boolean doIsArgumentIndexValid(int argIndex) {
		return true;
	}

	@Override
	Type doGetExpectedArgumentType(int argIndex) {
		int lastIndex = getNumberOfArguments() - 1;
		return argIndex < lastIndex
				? executable.getGenericParameterTypes()[argIndex]
				: TypeInfo.of(executable.getGenericParameterTypes()[lastIndex]).getComponentType().getType();
	}

	@Override
	TypeMatch doRateArgumentMatch(List<TypeInfo> argumentTypes) {
		if (argumentTypes.size() < getNumberOfArguments() - 1) {
			return TypeMatch.NONE;
		}
		TypeMatch worstArgumentClassMatchRating = TypeMatch.FULL;
		for (int i = 0; i < argumentTypes.size(); i++) {
			TypeMatch argumentClassMatchRating = rateArgumentTypeMatch(i, argumentTypes.get(i));
			worstArgumentClassMatchRating = MatchRatings.worstOf(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private TypeMatch rateArgumentTypeMatch(int argIndex, TypeInfo argumentType) {
		int lastArgIndex = getNumberOfArguments() - 1;
		if (argIndex == lastArgIndex && argumentType == TypeInfo.NONE) {
			/*
			 * If the last argument in a variadic method is null, then the regular array type
			 * (the one returned in RegularExecutableInfo) is assumed and not its component type.
			 */
			return TypeMatch.NONE;
		}
		TypeInfo expectedArgumentType = getExpectedArgumentType(argIndex);
		return MatchRatings.rateTypeMatch(argumentType, expectedArgumentType);
	}

	@Override
	Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos) {
		int numArguments = getNumberOfArguments();
		int realNumArguments = argumentInfos.size();
		if (realNumArguments < numArguments - 1) {
			throw new IllegalArgumentException("Expected " + numArguments + " arguments, but number is " + realNumArguments);
		}

		Object[] arguments = new Object[numArguments];
		int variadicArgumentIndex = numArguments - 1;
		for (int i = 0; i < variadicArgumentIndex; i++) {
			Object argument = argumentInfos.get(i).getObject();
			arguments[i] = ReflectionUtils.convertTo(argument, executable.getParameterTypes()[i], false);
		}

		// variadic arguments exist
		int numVarArgs = realNumArguments - numArguments + 1;
		Class<?> varArgComponentClass = executable.getParameterTypes()[variadicArgumentIndex].getComponentType();
		Object varArgArray = Array.newInstance(varArgComponentClass, numVarArgs);
		for (int i = 0; i < numVarArgs; i++) {
			Object argument = argumentInfos.get(variadicArgumentIndex + i).getObject();
			Array.set(varArgArray, i, ReflectionUtils.convertTo(argument, varArgComponentClass, false));
		}
		arguments[variadicArgumentIndex] = varArgArray;

		return arguments;
	}
}
