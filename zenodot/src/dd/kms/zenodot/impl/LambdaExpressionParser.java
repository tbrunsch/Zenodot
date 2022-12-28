package dd.kms.zenodot.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

class LambdaExpressionParser extends ExpressionParserImpl
{
	private final Class<?>				functionalInterface;
	private final @Nullable Class<?>[]	parameterTypes;

	LambdaExpressionParser(ParserSettings settings, VariablesImpl variables, Class<?> functionalInterface, Class<?>... parameterTypes) {
		super(settings, variables);
		Preconditions.checkArgument(ReflectionUtils.isFunctionalInterface(functionalInterface),
			"Class '" + functionalInterface.getName() + "' is no functional interface");
		this.functionalInterface = functionalInterface;

		int numParameters = parameterTypes != null ? parameterTypes.length : 0;
		if (numParameters == 0) {
			this.parameterTypes = null;
		} else {
			List<Method> unimplementedMethods = ReflectionUtils.getUnimplementedMethods(functionalInterface);
			Method lambdaMethod = Iterables.getOnlyElement(unimplementedMethods);
			Class<?>[] declaredParameterTypes = lambdaMethod.getParameterTypes();
			Preconditions.checkArgument(declaredParameterTypes.length == numParameters,
				"Wrong number of parameter types for method '" + functionalInterface.getName() + "." + lambdaMethod.getName() + "' :"
				+ numParameters + " instead of " + declaredParameterTypes.length
			);
			for (int i = 0; i < numParameters; i++) {
				Class<?> declaredParameterType = declaredParameterTypes[i];
				Class<?> parameterType = parameterTypes[i];
				Preconditions.checkArgument(MatchRatings.rateTypeMatch(declaredParameterType, parameterType) != TypeMatch.NONE,
					"Type of parameter " + i + " (" + parameterType.getName() + ") of method '" + functionalInterface.getName() + "." + lambdaMethod.getName() + "' "
					+ "is not assignable to the declared type " + declaredParameterType.getName()
				);
			}
			this.parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length);
		}
	}

	@Override
	ObjectParseResultExpectation getParseResultExpectation() {
		return new ObjectParseResultExpectation(ImmutableList.of(functionalInterface), true)
			.parameterTypes(parameterTypes)
			.parseWholeText(true);
	}
}
