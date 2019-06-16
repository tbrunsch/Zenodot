package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.MethodScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Base class for {@link ClassMethodParser} and {@link ObjectMethodParser}
 */
abstract class AbstractMethodParser<C> extends AbstractEntityParser<C>
{
	AbstractMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract TypeInfo getContextType(C context);
	abstract boolean isContextStatic();

	@Override
	ParseOutcome doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return ParseOutcomes.createParseError(startPosition, "Null pointer exception", ErrorPriority.EVALUATION_EXCEPTION);
		}

		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			String methodName;
			int insertionEnd;
			try {
				Token methodNameToken = tokenStream.readIdentifier();
				methodName = methodNameToken.getValue();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				methodName = "";
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting methods for completion...");
			return suggestMethods(methodName, context, expectation, startPosition, insertionEnd);
		}

		Token methodNameToken;
		try {
			methodNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing method name at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected an identifier", ErrorPriority.WRONG_PARSER);
		}
		String methodName = methodNameToken.getValue();
		final int endPosition = tokenStream.getPosition();

		// check for code completion
		if (methodNameToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting methods matching '" + methodName + "'");
			return suggestMethods(methodName, context, expectation, startPosition, endPosition);
		}

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		// no code completion requested => method name must exist
		List<ExecutableInfo> methodInfos = getMethodInfos(context, getMethodScanner(methodName, true));
		if (methodInfos.isEmpty()) {
			if (getMethodInfos(context, getMethodScanner(methodName, false)).isEmpty()) {
				log(LogLevel.ERROR, "unknown method '" + methodName + "'");
				return ParseOutcomes.createParseError(startPosition, "Unknown method '" + methodName + "'", ErrorPriority.RIGHT_PARSER);
			} else {
				log(LogLevel.ERROR, "method '" + methodName + "' is not visible");
				return ParseOutcomes.createParseError(startPosition, "Method '" + methodName + "' is not visible", ErrorPriority.RIGHT_PARSER);
			}
		}
		log(LogLevel.SUCCESS, "detected " + methodInfos.size() + " method(s) '" + methodName + "'");

		log(LogLevel.INFO, "parsing method arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ParseOutcome> argumentParseOutcomes = executableDataProvider.parseExecutableArguments(tokenStream, methodInfos);

		if (argumentParseOutcomes.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			int lastArgumentIndex = argumentParseOutcomes.size() - 1;
			ParseOutcome lastArgumentParseOutcome = argumentParseOutcomes.get(lastArgumentIndex);
			ParseOutcomeType lastArgumentParseOutcomeType = lastArgumentParseOutcome.getOutcomeType();
			log(LogLevel.INFO, "parse outcome: " + lastArgumentParseOutcomeType);

			if (lastArgumentParseOutcome.getOutcomeType() == ParseOutcomeType.COMPLETION_SUGGESTIONS) {
				CompletionSuggestions argumentSuggestions = (CompletionSuggestions) lastArgumentParseOutcome;
				// add argument information
				if (argumentSuggestions.getExecutableArgumentInfo().isPresent()) {
					// information has already been added for an executable used in a subexpression, which is more relevant
					return argumentSuggestions;
				}
				List<ObjectInfo> previousArgumentInfos = argumentParseOutcomes.subList(0, lastArgumentIndex).stream()
					.map(ObjectParseResult.class::cast)
					.map(ObjectParseResult::getObjectInfo)
					.collect(Collectors.toList());
				ExecutableArgumentInfo executableArgumentInfo = executableDataProvider.createExecutableArgumentInfo(methodInfos, previousArgumentInfos);
				return new CompletionSuggestions(argumentSuggestions.getPosition(), argumentSuggestions.getRatedSuggestions(), Optional.of(executableArgumentInfo));
			}

			Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(lastArgumentParseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
			if (parseOutcomeForPropagation.isPresent()) {
				return parseOutcomeForPropagation.get();
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseOutcomes.stream()
			.map(ObjectParseResult.class::cast)
			.map(ObjectParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingMethodInfos = executableDataProvider.getBestMatchingExecutableInfos(methodInfos, argumentInfos);

		switch (bestMatchingMethodInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching method found");
				return ParseOutcomes.createParseError(tokenStream.getPosition(), "No method matches the given arguments", ErrorPriority.RIGHT_PARSER);
			case 1: {
				ExecutableInfo bestMatchingExecutableInfo = Iterables.getOnlyElement(bestMatchingMethodInfos);
				ObjectInfo methodReturnInfo;
				try {
					methodReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(getContextObject(context), bestMatchingExecutableInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching method");
				} catch (InvocationTargetException e) {
					Throwable cause = e.getCause();
					StringBuilder messageBuilder = new StringBuilder("caught " + cause.getClass().getSimpleName());
					String causeMessage = cause.getMessage();
					if (causeMessage != null) {
						messageBuilder.append(": ").append(causeMessage);
					}
					log(LogLevel.ERROR, messageBuilder.toString());
					return ParseOutcomes.createParseError(startPosition, "Exception during method evaluation", ErrorPriority.EVALUATION_EXCEPTION, cause);
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return ParseOutcomes.createParseError(startPosition, "Exception during method evaluation", ErrorPriority.EVALUATION_EXCEPTION, e);
				}
				ParseOutcome parseOutcome = parserToolbox.getObjectTailParser().parse(tokenStream, methodReturnInfo, expectation);
				return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
						? compile(parseOutcome, bestMatchingExecutableInfo, argumentParseOutcomes)
						: parseOutcome;
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethodInfos.stream().map(Object::toString).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return ParseOutcomes.createAmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private CompletionSuggestions suggestMethods(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ExecutableInfo> methodInfos = getMethodInfos(context, getMethodScanner());
		boolean contextIsStatic = isContextStatic();
		return executableDataProvider.suggestMethods(methodInfos, contextIsStatic, expectedName, expectation, insertionBegin, insertionEnd);
	}

	private MethodScanner getMethodScanner() {
		AccessModifier minimumAccessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		return new MethodScanner().staticOnly(isContextStatic()).minimumAccessLevel(minimumAccessLevel);
	}

	private MethodScanner getMethodScanner(String name, boolean considerMinimumAccessLevel) {
		MethodScanner methodScanner = getMethodScanner().name(name);
		if (!considerMinimumAccessLevel) {
			methodScanner.minimumAccessLevel(AccessModifier.PRIVATE);
		}
		return methodScanner;
	}

	private List<ExecutableInfo> getMethodInfos(C context, MethodScanner methodScanner) {
		return InfoProvider.getMethodInfos(getContextType(context), methodScanner);
	}

	private ParseOutcome compile(ParseOutcome tailParseOutcome, ExecutableInfo methodInfo, List<ParseOutcome> argumentParseOutcomes) {
		if (!ParseOutcomes.isCompiledParseResult(tailParseOutcome)) {
			return tailParseOutcome;
		}
		CompiledObjectParseResult compiledTailParseResult = (CompiledObjectParseResult) tailParseOutcome;
		List<CompiledObjectParseResult> compiledArgumentParseResults = (List) argumentParseOutcomes;
		return new CompiledMethodParseResult(compiledTailParseResult, methodInfo, compiledArgumentParseResults);
	}

	private static class CompiledMethodParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult			compiledTailParseResult;
		private final ExecutableInfo					methodInfo;
		private final List<CompiledObjectParseResult>	compiledArgumentParseResults;

		CompiledMethodParseResult(CompiledObjectParseResult compiledTailParseResult, ExecutableInfo methodInfo, List<CompiledObjectParseResult> compiledArgumentParseResults) {
			super(compiledTailParseResult.getPosition(), compiledTailParseResult.getObjectInfo());
			this.compiledTailParseResult = compiledTailParseResult;
			this.methodInfo = methodInfo;
			this.compiledArgumentParseResults = compiledArgumentParseResults;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			List<ObjectInfo> argumentInfos = new ArrayList<>(compiledArgumentParseResults.size());
			for (CompiledObjectParseResult compiledArgumentParseResult : compiledArgumentParseResults) {
				argumentInfos.add(compiledArgumentParseResult.evaluate(thisInfo, thisInfo));
			}
			// TODO: If C == TypeInfo, then contextObject should be null instead
			Object contextObject = context.getObject();
			ObjectInfo methodReturnInfo = OBJECT_INFO_PROVIDER.getExecutableReturnInfo(contextObject, methodInfo, argumentInfos);
			return compiledTailParseResult.evaluate(thisInfo, methodReturnInfo);
		}
	}
}
