package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.parsers.ParseExpectationBuilder;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionMethod;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for providing information about executables (methods, constructors)
 */
public class ExecutableDataProvider
{
	// defines a priority when determining which overloaded executable (method/constructor) to call
	private static final List<List<TypeMatch>>	ALLOWED_EXECUTABLE_RATINGS_BY_PHASE = Arrays.asList(
		Arrays.asList(TypeMatch.FULL),
		Arrays.asList(TypeMatch.INHERITANCE, TypeMatch.PRIMITIVE_CONVERSION),
		Arrays.asList(TypeMatch.BOXED, TypeMatch.BOXED_AND_CONVERSION, TypeMatch.BOXED_AND_INHERITANCE)
	);

	private final ParserToolbox parserToolbox;

	public ExecutableDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	public CompletionSuggestions suggestMethods(List<AbstractExecutableInfo> methodInfos, boolean contextIsStatic, String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			methodInfos,
			methodInfo -> new CompletionSuggestionMethod(methodInfo, insertionBegin, insertionEnd),
			rateMethodFunc(expectedName, contextIsStatic, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	public List<ParseResult> parseExecutableArguments(TokenStream tokenStream, List<AbstractExecutableInfo> availableExecutableInfos) {
		List<ParseResult> arguments = new ArrayList<>();

		int position;
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken != null && characterToken.getValue().charAt(0) == '(';

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				arguments.add(new ParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
				return arguments;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == ')') {
				tokenStream.readCharacterUnchecked();
				return arguments;
			}
		}

		for (int argIndex = 0; ; argIndex++) {
			final int i = argIndex;

			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> executableInfo.isArgumentIndexValid(i)).collect(Collectors.toList());
			List<TypeInfo> expectedArgumentTypes_i = getExpectedArgumentTypes(availableExecutableInfos, i);

			if (expectedArgumentTypes_i.isEmpty()) {
				position = tokenStream.getPosition();
				boolean requestCodeCompletion = tokenStream.isCaretAtPosition() || tokenStream.readOptionalSpace().isContainsCaret();
				if (i == 0 && requestCodeCompletion) {
					// code completion after opening '(' for executable without arguments
					arguments.add(CompletionSuggestions.none(position));
				} else {
					arguments.add(new ParseError(position, "No further arguments expected", ErrorPriority.RIGHT_PARSER));
				}
				return arguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseExpectation argumentExpectation = ParseExpectationBuilder.expectObject().allowedTypes(expectedArgumentTypes_i).build();
			ParseResult argumentParseResult_i = parserToolbox.getExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), argumentExpectation);

			Optional<ParseResult> argumentForPropagation = ParseUtils.prepareParseResultForPropagation(argumentParseResult_i, argumentExpectation, ErrorPriority.RIGHT_PARSER);
			if (argumentForPropagation.isPresent()) {
				arguments.add(argumentForPropagation.get());
				return arguments;
			}
			arguments.add(argumentParseResult_i);

			ObjectParseResult parseResult = ((ObjectParseResult) argumentParseResult_i);
			int parsedToPosition = parseResult.getPosition();
			tokenStream.moveTo(parsedToPosition);
			ObjectInfo argumentInfo = parseResult.getObjectInfo();
			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> acceptsArgumentInfo(executableInfo, i, argumentInfo)).collect(Collectors.toList());

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
				return arguments;
			}

			if (characterToken.getValue().charAt(0) == ')') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					arguments.add(CompletionSuggestions.none(tokenStream.getPosition()));
				}
				return arguments;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
				return arguments;
			}
		}
	}

	// assumes that each of the executableInfos accepts an argument for index argIndex
	private List<TypeInfo> getExpectedArgumentTypes(List<AbstractExecutableInfo> executableInfos, int argIndex) {
		return executableInfos.stream()
				.map(executableInfo -> executableInfo.getExpectedArgumentType(argIndex))
				.distinct()
				.collect(Collectors.toList());
	}

	private boolean acceptsArgumentInfo(AbstractExecutableInfo executableInfo, int argIndex, ObjectInfo argInfo) {
		TypeInfo expectedArgumentType;
		try {
			expectedArgumentType = executableInfo.getExpectedArgumentType(argIndex);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		TypeInfo argumentType = parserToolbox.getObjectInfoProvider().getType(argInfo);
		return MatchRatings.isConvertibleTo(argumentType, expectedArgumentType);
	}

	public List<AbstractExecutableInfo> getBestMatchingExecutableInfos(List<AbstractExecutableInfo> availableExecutableInfos, List<ObjectInfo> argumentInfos) {
		ObjectInfoProvider objectInfoProvider = parserToolbox.getObjectInfoProvider();
		List<TypeInfo> argumentTypes = argumentInfos.stream().map(objectInfoProvider::getType).collect(Collectors.toList());
		Map<AbstractExecutableInfo, TypeMatch> ratedExecutableInfos = availableExecutableInfos.stream()
			.collect(Collectors.toMap(
				executableInfo -> executableInfo,
				executableInfo -> executableInfo.rateArgumentMatch(argumentTypes)
			));

		for (boolean allowVariadicExecutables : Arrays.asList(false, true)) {
			for (List<TypeMatch> allowedRatings : ALLOWED_EXECUTABLE_RATINGS_BY_PHASE) {
				List<AbstractExecutableInfo> executableInfos = filterExecutableInfos(ratedExecutableInfos, allowedRatings, allowVariadicExecutables);
				if (!executableInfos.isEmpty()) {
					return executableInfos;
				}
			}
		}
		return Collections.emptyList();
	}

	private static List<AbstractExecutableInfo> filterExecutableInfos(Map<AbstractExecutableInfo, TypeMatch> ratedExecutableInfos, List<TypeMatch> allowedRatings, boolean allowVariadicExecutables) {
		List<AbstractExecutableInfo> filteredExecutableInfos = new ArrayList<>();
		for (AbstractExecutableInfo executableInfo : ratedExecutableInfos.keySet()) {
			TypeMatch rating = ratedExecutableInfos.get(executableInfo);
			if (allowedRatings.contains(rating) && (allowVariadicExecutables || !executableInfo.isVariadic())) {
				filteredExecutableInfos.add(executableInfo);
			}
		}
		return filteredExecutableInfos;
	}

	public ExecutableArgumentInfo createExecutableArgumentInfo(List<AbstractExecutableInfo> executableInfos, List<ObjectInfo> argumentInfos) {
		int currentArgumentIndex = argumentInfos.size();
		Map<AbstractExecutableInfo, Boolean> applicableExecutableOverloads = new LinkedHashMap<>(executableInfos.size());
		for (AbstractExecutableInfo executableInfo : executableInfos) {
			boolean applicable = IntStream.range(0, argumentInfos.size()).allMatch(i -> acceptsArgumentInfo(executableInfo, i, argumentInfos.get(i)));
			applicableExecutableOverloads.put(executableInfo, applicable);
		}

		return new ExecutableArgumentInfo(currentArgumentIndex, applicableExecutableOverloads);
	}

	/*
	 * Suggestions
	 */
	private StringMatch rateMethodByName(AbstractExecutableInfo methodInfo, String expectedName) {
		return MatchRatings.rateStringMatch(methodInfo.getName(), expectedName);
	}

	private TypeMatch rateMethodByTypes(AbstractExecutableInfo methodInfo, ParseExpectation expectation) {
		/*
		 * Even for EvaluationMode.DYNAMICALLY_TYPED we only consider the declared return type of the method instead
		 * of the runtime type of the returned object. Otherwise, we would have to invoke the method for code
		 * completion, possibly causing undesired side effects.
		 */
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		return	allowedTypes == null
					? TypeMatch.FULL
					: allowedTypes.stream().map(allowedType -> MatchRatings.rateTypeMatch(methodInfo.getReturnType(), allowedType)).min(TypeMatch::compareTo).orElse(TypeMatch.NONE);
	}

	private AccessMatch rateMethodByAccess(AbstractExecutableInfo methodInfo, boolean contextIsStatic) {
		return methodInfo.isStatic() && !contextIsStatic ? AccessMatch.STATIC_ACCESS_VIA_INSTANCE : AccessMatch.FULL;
	}

	private Function<AbstractExecutableInfo, MatchRating> rateMethodFunc(String methodName, boolean contextIsStatic, ParseExpectation expectation) {
		return methodInfo -> new MatchRating(rateMethodByName(methodInfo, methodName), rateMethodByTypes(methodInfo, expectation), rateMethodByAccess(methodInfo, contextIsStatic));
	}

	public static String getMethodDisplayText(AbstractExecutableInfo methodInfo) {
		int numArguments = methodInfo.getNumberOfArguments();
		final String argumentsAsString;
		if (methodInfo.isVariadic()) {
			int lastArgumentIndex = numArguments - 1;
			argumentsAsString = IntStream.range(0, numArguments).mapToObj(i -> methodInfo.getExpectedArgumentType(i).getRawType().getSimpleName().toString() + (i == lastArgumentIndex ? "..." : "")).collect(Collectors.joining(", "));
		} else {
			argumentsAsString = IntStream.range(0, numArguments).mapToObj(i -> methodInfo.getExpectedArgumentType(i).getRawType().getSimpleName().toString()).collect(Collectors.joining(", "));
		}
		return methodInfo.getName()
				+ "("
				+ argumentsAsString
				+ ")";
	}
}
