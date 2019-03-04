package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.TypeMatch;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * "Regular" view on executables (methods, constructors). For non-variadic executables,
 * this is the only available view. For variadic methods, this view pretends that the
 * underlying executable is non-variadic, treating the variadic part as a single array.
 * The other view on variadic methods is {@link VariadicExecutableInfo}.
 */
public class RegularExecutableInfo extends AbstractExecutableInfo
{
	RegularExecutableInfo(Executable executable, TypeInfo declaringType) {
		super(executable, declaringType);
	}

	@Override
	boolean doIsArgumentIndexValid(int argIndex) {
		return argIndex < getNumberOfArguments();
	}

	@Override
	Type doGetExpectedArgumentType(int argIndex) {
		if (argIndex >= getNumberOfArguments()) {
			throw new IndexOutOfBoundsException("Argument index " + argIndex + " is not in the range [0, " + getNumberOfArguments() + ")");
		}
		return executable.getGenericParameterTypes()[argIndex];
	}

	@Override
	TypeMatch doRateArgumentMatch(List<TypeInfo> argumentTypes) {
		if (argumentTypes.size() != getNumberOfArguments()) {
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
		TypeInfo expectedArgumentType = getExpectedArgumentType(argIndex);
		return MatchRatings.rateTypeMatch(argumentType, expectedArgumentType);
	}

	@Override
	Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos) {
		int numArguments = getNumberOfArguments();
		if (argumentInfos.size() != numArguments) {
			throw new IllegalArgumentException("Expected " + numArguments + " arguments, but number is " + argumentInfos.size());
		}

		Object[] arguments = new Object[numArguments];
		for (int i = 0; i < numArguments; i++) {
			Object argument = argumentInfos.get(i).getObject();
			arguments[i] = ReflectionUtils.convertTo(argument, getExpectedArgumentType(i).getRawType(), false);
		}
		return arguments;
	}
}
