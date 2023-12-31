package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.common.ObjectInfoProvider.Parameter;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.LambdaParseResult;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.VariablesImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses expressions of the form {@code x -> x*x} or {@code (x, y) -> x + y} in the context of {@code this}
 * for a specified functional interface.
 */
class ConcreteLambdaParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	private final Class<?>	functionalInterface;

	ConcreteLambdaParser(ParserToolbox parserToolbox, Class<?> functionalInterface) {
		super(parserToolbox);
		this.functionalInterface = functionalInterface;
	}

	@Override
	protected ParseResult doParse(TokenStream tokenStream, ObjectInfo context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		List<Method> unimplementedMethods = ReflectionUtils.getUnimplementedMethods(functionalInterface);
		if (unimplementedMethods.size() != 1) {
			throw new InternalErrorException("Class " + functionalInterface + " is no functional interface");
		}
		Method method = unimplementedMethods.get(0);
		Class<?>[] parameterTypes = expectation.getParameterTypes();
		if (parameterTypes == null) {
			// no additional information provided by the caller
			parameterTypes = method.getParameterTypes();
		}

		int startPosition = tokenStream.getPosition();

		List<Parameter> parameters = readParameters(tokenStream, parameterTypes);

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		tokenStream.readString(ImmutableList.of("->"), info -> CodeCompletions.NONE);
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		log(LogLevel.SUCCESS, "Parsed lambda parameters " + parameters.stream().map(Parameter::getName).collect(Collectors.joining(", ")));
		log(LogLevel.SUCCESS, "Parsed lambda arrow -> " + parameters.stream().map(Parameter::getName).collect(Collectors.joining(", ")));

		// create new scope for variables
		VariablesImpl variables = new VariablesImpl(parserToolbox.getVariables());
		for (Parameter parameter : parameters) {
			ObjectInfo valueInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, parameter.getDeclaredType());
			variables.createVariable(parameter.getName(), valueInfo, false);
		}

		log(LogLevel.INFO, "parsing lambda body");
		ObjectParseResult bodyParseResult = parseBody(tokenStream, context, variables);

		int endPosition = tokenStream.getPosition();
		String lambdaExpression = tokenStream.getExpression().substring(startPosition, endPosition);
		log(LogLevel.SUCCESS, "parsed lambda '" + lambdaExpression + "'");

		Class<?> expectedReturnType = method.getReturnType();
		Class<?> lambdaResultType = expectedReturnType == void.class
				? void.class
				: bodyParseResult.getObjectInfo().getDeclaredType();
		if (MatchRatings.rateTypeMatch(expectedReturnType, lambdaResultType) == TypeMatch.NONE) {
			log(LogLevel.INFO, "lambda has wrong return type: " + lambdaResultType + " instead of " + expectedReturnType);
			throw new SyntaxException("Return type " + lambdaResultType + " is not assignable to " + expectedReturnType);
		}

		log(LogLevel.SUCCESS, "lambda has correct return type");

		ObjectInfo lambdaInfo = parserToolbox.inject(ObjectInfoProvider.class).getLambdaInfo(
			functionalInterface, method.getName(), parameters, bodyParseResult,
			lambdaExpression, parserToolbox.getThisInfo(), parserToolbox.getVariables()
		);
		return new LambdaParseResultImpl(
			functionalInterface, method.getName(), parameters, bodyParseResult, lambdaExpression,
			lambdaInfo, tokenStream, lambdaResultType
		);
	}

	private List<Parameter> readParameters(TokenStream tokenStream, Class<?>[] parameterTypes) throws CodeCompletionException, SyntaxException, InternalErrorException {
		int numParameters = parameterTypes.length;
		boolean mustBeEnclosedInParentheses = numParameters != 1;
		List<String> parameterNames = readParameterNames(tokenStream, mustBeEnclosedInParentheses);

		if (parameterNames.size() != numParameters) {
			String error = "Expected " + numParameters + " parameters, but found " + parameterNames.size();
			if (parameterNames.isEmpty()) {
				error += ".";
			} else {
				error += ": " + parameterNames.stream().collect(Collectors.joining(", "));
			}
			throw new SyntaxException(error);
		}

		List<Parameter> parameters = new ArrayList<>(numParameters);
		for (int i = 0; i < numParameters; i++) {
			Parameter parameter = new Parameter(parameterNames.get(i), parameterTypes[i]);
			parameters.add(parameter);
		}
		return parameters;
	}

	private List<String> readParameterNames(TokenStream tokenStream, boolean mustBeEnclosedInParentheses) throws SyntaxException, CodeCompletionException, InternalErrorException {
		boolean parseWithParentheses = true;
		if (tokenStream.peekCharacter() != '(') {
			if (mustBeEnclosedInParentheses) {
				throw new SyntaxException("Expected opening bracket '('");
			}
			parseWithParentheses = false;
		}

		if (parseWithParentheses) {
			tokenStream.readCharacter('(');
		}

		List<String> parameterNames = new ArrayList<>();
		while (true) {
			char nextChar = tokenStream.peekCharacter();
			if (nextChar == ')' || nextChar == '-') {
				// possibly end of "(x, y, ...)" or begin of "->"
				break;
			}
			String parameterName = tokenStream.readIdentifier(info -> CodeCompletions.NONE, "Expected a parameter name");
			parameterNames.add(parameterName);
			nextChar = tokenStream.peekCharacter();
			if (nextChar != ',') {
				// no further parameter
				break;
			}
			tokenStream.readCharacter(',');
		}

		if (parseWithParentheses) {
			tokenStream.readCharacter(')');
		}
		return parameterNames;
	}

	private ObjectParseResult parseBody(TokenStream tokenStream, ObjectInfo contextInfo, VariablesImpl variables) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		ParserToolbox parserToolBox = parserToolbox
			.withEvaluationMode(EvaluationMode.STATIC_TYPING)
			.withVariables(variables);
		AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> expressionParser = parserToolBox.createExpressionParser();
		return expressionParser.parse(tokenStream, contextInfo, new ObjectParseResultExpectation());
	}

	private static class LambdaParseResultImpl extends LambdaParseResult
	{
		private final Class<?>			functionalInterface;
		private final String			methodName;
		private final List<Parameter>	parameters;
		private final ObjectParseResult	bodyParseResult;
		private final String			lambdaExpression;

		LambdaParseResultImpl(Class<?> functionalInterface, String methodName, List<Parameter> parameters, ObjectParseResult bodyParseResult, String lambdaExpression, ObjectInfo lambdaInfo, TokenStream tokenStream, Class<?> lambdaResultType) {
			super(lambdaInfo, tokenStream, lambdaResultType);
			this.functionalInterface = functionalInterface;
			this.methodName = methodName;
			this.parameters = parameters;
			this.bodyParseResult = bodyParseResult;
			this.lambdaExpression = lambdaExpression;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws InternalErrorException {
			return ObjectInfoProvider.DYNAMIC_OBJECT_INFO_PROVIDER.getLambdaInfo(
				functionalInterface, methodName, parameters, bodyParseResult,
				lambdaExpression, thisInfo, variables
			);
		}
	}
}
