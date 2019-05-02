package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Base class for {@link ClassMethodParser} and {@link ObjectMethodParser}
 */
abstract class AbstractMethodParser<C> extends AbstractEntityParser<C>
{
	public AbstractMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract List<AbstractExecutableInfo> getMethodInfos(C context);

	@Override
	ParseResult doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return new ParseError(startPosition, "Null pointer exception", ErrorPriority.EVALUATION_EXCEPTION);
		}

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting methods for completion...");
			return suggestMethods("", context, expectation, startPosition, insertionEnd);
		}

		Token methodNameToken;
		try {
			methodNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing method name at " + tokenStream);
			return new ParseError(startPosition, "Expected an identifier", ErrorPriority.WRONG_PARSER);
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
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		// no code completion requested => method name must exist
		List<AbstractExecutableInfo> methodInfos = getMethodInfos(context);
		List<AbstractExecutableInfo> matchingMethodInfos = methodInfos.stream().filter(methodInfo -> methodInfo.getName().equals(methodName)).collect(Collectors.toList());
		if (matchingMethodInfos.isEmpty()) {
			log(LogLevel.ERROR, "unknown method '" + methodName + "'");
			return new ParseError(startPosition, "Unknown method '" + methodName + "'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected " + matchingMethodInfos.size() + " method(s) '" + methodName + "'");

		log(LogLevel.INFO, "parsing method arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ParseResult> argumentParseResults = executableDataProvider.parseExecutableArguments(tokenStream, matchingMethodInfos);

		if (argumentParseResults.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			int lastArgumentIndex = argumentParseResults.size() - 1;
			ParseResult lastArgumentParseResult = argumentParseResults.get(lastArgumentIndex);
			ParseResultType lastArgumentParseResultType = lastArgumentParseResult.getResultType();
			log(LogLevel.INFO, "parse result: " + lastArgumentParseResultType);

			if (lastArgumentParseResult.getResultType() == ParseResultType.COMPLETION_SUGGESTIONS) {
				CompletionSuggestions argumentSuggestions = (CompletionSuggestions) lastArgumentParseResult;
				// add argument information
				if (argumentSuggestions.getExecutableArgumentInfo().isPresent()) {
					// information has already been added for an executable used in a subexpression, which is more relevant
					return argumentSuggestions;
				}
				List<ObjectInfo> previousArgumentInfos = argumentParseResults.subList(0, lastArgumentIndex).stream()
					.map(ObjectParseResult.class::cast)
					.map(ObjectParseResult::getObjectInfo)
					.collect(Collectors.toList());
				ExecutableArgumentInfo executableArgumentInfo = executableDataProvider.createExecutableArgumentInfo(matchingMethodInfos, previousArgumentInfos);
				return new CompletionSuggestions(argumentSuggestions.getPosition(), argumentSuggestions.getRatedSuggestions(), Optional.of(executableArgumentInfo));
			}

			Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(lastArgumentParseResult, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
			if (parseResultForPropagation.isPresent()) {
				return parseResultForPropagation.get();
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseResults.stream()
			.map(ObjectParseResult.class::cast)
			.map(ObjectParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<AbstractExecutableInfo> bestMatchingMethodInfos = executableDataProvider.getBestMatchingExecutableInfos(matchingMethodInfos, argumentInfos);

		switch (bestMatchingMethodInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching method found");
				return new ParseError(tokenStream.getPosition(), "No method matches the given arguments", ErrorPriority.RIGHT_PARSER);
			case 1: {
				AbstractExecutableInfo bestMatchingExecutableInfo = bestMatchingMethodInfos.get(0);
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
					return new ParseError(startPosition, "Exception during method evaluation", ErrorPriority.EVALUATION_EXCEPTION, cause);
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return new ParseError(startPosition, "Exception during method evaluation", ErrorPriority.EVALUATION_EXCEPTION, e);
				}
				return parserToolbox.getObjectTailParser().parse(tokenStream, methodReturnInfo, expectation);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethodInfos.stream().map(Object::toString).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private CompletionSuggestions suggestMethods(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		return parserToolbox.getExecutableDataProvider().suggestMethods(expectedName, getMethodInfos(context), expectation, insertionBegin, insertionEnd);
	}
}
