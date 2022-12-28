package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.impl.common.ObjectInfoProvider.Parameter;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.result.ParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

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
	ParseResult doParse(TokenStream tokenStream, ObjectInfo context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		List<Method> unimplementedMethods = ReflectionUtils.getUnimplementedMethods(functionalInterface);
		if (unimplementedMethods.size() != 1) {
			throw new InternalErrorException("Class " + functionalInterface + " is no functional interface");
		}
		Method method = unimplementedMethods.get(0);
		Class<?>[] parameterTypes = method.getParameterTypes();

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

		Class<?> bodyResultType = bodyParseResult.getObjectInfo().getDeclaredType();
		Class<?> expectedReturnType = method.getReturnType();
		if (expectedReturnType != void.class && MatchRatings.rateTypeMatch(expectedReturnType, bodyResultType) == TypeMatch.NONE) {
			log(LogLevel.INFO, "lambda has wrong return type: " + bodyResultType + " instead of " + expectedReturnType);
			throw new SyntaxException("Return type " + bodyResultType + " is not assignable to " + expectedReturnType);
		}

		log(LogLevel.SUCCESS, "lambda has correct return type");

		ObjectInfo lambdaInfo = parserToolbox.getObjectInfoProvider().getLambdaInfo(
			functionalInterface, method.getName(), parameters, bodyParseResult,
			lambdaExpression, parserToolbox.getThisInfo(), variables
		);
		return new LambdaParseResult(
			functionalInterface, method.getName(), parameters,
			bodyParseResult, lambdaExpression, lambdaInfo, tokenStream
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

	private static class LambdaParseResult extends ObjectParseResult
	{
		private final Class<?>			functionalInterface;
		private final String			methodName;
		private final List<Parameter>	parameters;
		private final ObjectParseResult	bodyParseResult;
		private final String			lambdaExpression;

		LambdaParseResult(Class<?> functionalInterface, String methodName, List<Parameter> parameters, ObjectParseResult bodyParseResult, String lambdaExpression, ObjectInfo lambdaInfo, TokenStream tokenStream) {
			super(lambdaInfo, tokenStream);
			this.functionalInterface = functionalInterface;
			this.methodName = methodName;
			this.parameters = parameters;
			this.bodyParseResult = bodyParseResult;
			this.lambdaExpression = lambdaExpression;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, VariablesImpl variables) throws InternalErrorException {
			// create new scope for variables
			variables = new VariablesImpl(variables);
			return OBJECT_INFO_PROVIDER.getLambdaInfo(
				functionalInterface, methodName, parameters, bodyParseResult,
				lambdaExpression, thisInfo, variables
			);
		}
	}
}
