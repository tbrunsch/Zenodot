package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.JavaToken;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

/**
 * Parses a sub expression starting with a method {@code <method>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<method>},</li>
 *     <li>{@code <context class>.<method>}, or</li>
 *     <li>{@code <fmethod} (like {@code <context instance>.<method>} for {@code <context instance> = this})</li>
 * </ul>
 */
public class JavaMethodParser extends AbstractJavaEntityParser
{
	private final boolean staticOnly;

	public JavaMethodParser(JavaParserContext parserContext, ObjectInfo thisInfo, boolean staticOnly) {
		super(parserContext, thisInfo);
		this.staticOnly = staticOnly;
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		final int startPosition = tokenStream.getPosition();

		if (thisInfo.getObject() == null && !staticOnly) {
			return new ParseError(startPosition, "Null object does not have any methods", ErrorType.WRONG_PARSER);
		}

		JavaToken methodNameToken;
		try {
			methodNameToken = tokenStream.readIdentifier();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an identifier", ErrorType.WRONG_PARSER);
		}
		String methodName = methodNameToken.getValue();
		final int endPosition = tokenStream.getPosition();

		Class<?> currentContextClass = parserContext.getObjectInfoProvider().getClass(currentContextInfo);
		List<Method> methods = parserContext.getInspectionDataProvider().getMethods(currentContextClass, staticOnly);

		// check for code completion
		if (methodNameToken.isContainsCaret()) {
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, startPosition, endPosition),
				ParseUtils.rateMethodByNameAndClassesFunc(methodName, expectedResultClasses)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => method name must exist
		List<Method> matchingMethods = methods.stream().filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
		if (matchingMethods.isEmpty()) {
			return new ParseError(startPosition, "Unknown method '" + methodName + "'", ErrorType.SEMANTIC_ERROR);
		}

		List<ParseResultIF> argumentParseResults = parserContext.getFieldAndMethodDataProvider().parseMethodArguments(tokenStream, matchingMethods);

		if (!argumentParseResults.isEmpty()) {
			ParseResultIF lastArgumentParseResult = argumentParseResults.get(argumentParseResults.size()-1);
			if (lastArgumentParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
				// Immediately propagate anything but parse results (code completion, errors, ambiguous parse results)
				return lastArgumentParseResult;
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseResults.stream()
			.map(ParseResult.class::cast)
			.map(ParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<Method> bestMatchingMethods = parserContext.getFieldAndMethodDataProvider().getBestMatchingMethods(matchingMethods, argumentInfos);

		switch (bestMatchingMethods.size()) {
			case 0:
				return new ParseError(tokenStream.getPosition(), "No method matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				Method bestMatchingMethod = bestMatchingMethods.get(0);
				ObjectInfo methodReturnInfo;
				try {
					methodReturnInfo = parserContext.getObjectInfoProvider().getMethodReturnInfo(currentContextInfo, bestMatchingMethod, argumentInfos);
				} catch (Exception e) {
					return new ParseError(startPosition, "Exception during method evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserContext.getObjectTailParser().parse(tokenStream, methodReturnInfo, expectedResultClasses);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethods.stream().map(JavaMethodParser::formatMethod).collect(Collectors.joining("\n"));
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private static String formatMethod(Method method) {
		return method.getName()
				+ "("
				+ Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))
				+ ")";
	}
}