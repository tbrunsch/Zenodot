package dd.kms.zenodot.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.CompiledLambdaExpression;
import dd.kms.zenodot.api.LambdaExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.LambdaParseResult;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

class LambdaExpressionParserImpl<T> extends ExpressionParserImpl implements LambdaExpressionParser<T>
{
	private final Class<T>				functionalInterface;
	private final @Nullable Class<?>[]	parameterTypes;

	LambdaExpressionParserImpl(ParserSettings settings, VariablesImpl variables, Class<T> functionalInterface, Class<?>... parameterTypes) {
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

	@Override
	public T evaluate(String expression, Object thisValue) throws ParseException {
		Object result = super.evaluate(expression, thisValue);
		checkExpression(expression, functionalInterface.isInstance(result));
		return functionalInterface.cast(functionalInterface);
	}

	@Override
	public CompiledLambdaExpression<T> compile(String expression, Class<?> thisClass) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisClass);
		return doCompile(expression, thisInfo);
	}

	@Override
	public CompiledLambdaExpression<T> compile(String expression, Object thisValue) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
		return doCompile(expression, thisInfo);
	}

	private CompiledLambdaExpression<T> doCompile(String expression, ObjectInfo thisInfo) throws ParseException {
		TokenStream tokenStream = new TokenStream(expression, -1);
		ObjectParseResult parseResult;
		try {
			parseResult = parse(tokenStream, thisInfo, getParseResultExpectation());
		} catch (Throwable t) {
			throw new ParseException(tokenStream, t.getMessage(), t);
		}
		return createCompiledExpression(expression, parseResult);
	}

	private CompiledLambdaExpression<T> createCompiledExpression(String expression, ObjectParseResult compiledParseResult) throws ParseException {
		checkExpression(expression, compiledParseResult instanceof LambdaParseResult);
		LambdaParseResult compiledLambdaParseResult = (LambdaParseResult) compiledParseResult;

		Class<?> resultType = compiledLambdaParseResult.getObjectInfo().getDeclaredType();
		checkExpression(expression, functionalInterface.isAssignableFrom(resultType));

		return new CompiledLambdaExpression<T>()
		{
			@Override
			public Class<?> getResultType() {
				return resultType;
			}

			@Override
			public T evaluate(Object thisValue) throws Exception {
				ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
				Object result = compiledLambdaParseResult.evaluate(thisInfo, thisInfo, variables).getObject();
				return functionalInterface.cast(result);
			}

			@Override
			public Class<?> getLambdaResultType() {
				return compiledLambdaParseResult.getLambdaResultType();
			}
		};
	}

	private void checkExpression(String expression, boolean condition) throws ParseException {
		if (!condition) {
			throw new ParseException(expression, 0, "The expression is not of type " + functionalInterface + ".", null);
		}
	}
}
