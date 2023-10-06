package dd.kms.zenodot.framework.wrappers;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.framework.matching.MatchRatings;

import java.lang.reflect.Executable;
import java.util.List;

/**
 * "Regular" view on executables (methods, constructors). For non-variadic executables,
 * this is the only available view. For variadic methods, this view pretends that the
 * underlying executable is non-variadic, treating the variadic part as a single array.
 * The other view on variadic methods is {@link VariadicExecutableInfo}.
 */
public class RegularExecutableInfo extends ExecutableInfo
{
	public RegularExecutableInfo(Executable executable) {
		super(executable);
	}

	@Override
	boolean doIsArgumentIndexValid(int argIndex) {
		return argIndex < getNumberOfArguments();
	}

	@Override
	Class<?> doGetExpectedArgumentType(int argIndex) {
		if (argIndex >= getNumberOfArguments()) {
			throw new IndexOutOfBoundsException("Argument index " + argIndex + " is not in the range [0, " + getNumberOfArguments() + ")");
		}
		return member.getParameterTypes()[argIndex];
	}

	@Override
	TypeMatch doRateArgumentMatch(List<Class<?>> argumentTypes) {
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

	private TypeMatch rateArgumentTypeMatch(int argIndex, Class<?> argumentType) {
		Class<?> expectedArgumentType = getExpectedArgumentType(argIndex);
		return MatchRatings.rateTypeMatch(expectedArgumentType, argumentType);
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
			arguments[i] = ReflectionUtils.convertTo(argument, getExpectedArgumentType(i), false);
		}
		return arguments;
	}
}
