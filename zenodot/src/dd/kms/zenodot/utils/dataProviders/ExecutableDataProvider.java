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
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
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

	public CompletionSuggestions suggestMethods(List<ExecutableInfo> methodInfos, boolean contextIsStatic, String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			methodInfos,
			methodInfo -> new CompletionSuggestionMethod(methodInfo, insertionBegin, insertionEnd),
			rateMethodFunc(expectedName, contextIsStatic, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	public List<ParseOutcome> parseExecutableArguments(TokenStream tokenStream, List<ExecutableInfo> availableExecutableInfos) {
		List<ParseOutcome> arguments = new ArrayList<>();

		int position;
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken != null && characterToken.getValue().charAt(0) == '(';

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				arguments.add(ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
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
				boolean requestCodeCompletion = tokenStream.isCaretWithinNextWhiteSpaces();
				if (i == 0 && requestCodeCompletion) {
					// code completion after opening '(' for executable without arguments
					arguments.add(CompletionSuggestions.none(position));
				} else {
					arguments.add(ParseOutcomes.createParseError(position, "No further arguments expected", ErrorPriority.RIGHT_PARSER));
				}
				return arguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseExpectation argumentExpectation = ParseExpectationBuilder.expectObject().allowedTypes(expectedArgumentTypes_i).build();
			ParseOutcome argumentParseOutcome_i = parserToolbox.getExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), argumentExpectation);

			Optional<ParseOutcome> argumentForPropagation = ParseUtils.prepareParseOutcomeForPropagation(argumentParseOutcome_i, argumentExpectation, ErrorPriority.RIGHT_PARSER);
			if (argumentForPropagation.isPresent()) {
				arguments.add(argumentForPropagation.get());
				return arguments;
			}
			arguments.add(argumentParseOutcome_i);

			ObjectParseResult parseResult = ((ObjectParseResult) argumentParseOutcome_i);
			int parsedToPosition = parseResult.getPosition();
			tokenStream.moveTo(parsedToPosition);
			ObjectInfo argumentInfo = parseResult.getObjectInfo();
			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> acceptsArgumentInfo(executableInfo, i, argumentInfo)).collect(Collectors.toList());

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				arguments.add(ParseOutcomes.createParseError(position, "Expected comma ',' or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
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
				arguments.add(ParseOutcomes.createParseError(position, "Expected comma ',' or closing parenthesis ')'", ErrorPriority.RIGHT_PARSER));
				return arguments;
			}
		}
	}

	// assumes that each of the executableInfos accepts an argument for index argIndex
	private List<TypeInfo> getExpectedArgumentTypes(List<ExecutableInfo> executableInfos, int argIndex) {
		return executableInfos.stream()
				.map(executableInfo -> executableInfo.getExpectedArgumentType(argIndex))
				.distinct()
				.collect(Collectors.toList());
	}

	private boolean acceptsArgumentInfo(ExecutableInfo executableInfo, int argIndex, ObjectInfo argInfo) {
		TypeInfo expectedArgumentType;
		try {
			expectedArgumentType = executableInfo.getExpectedArgumentType(argIndex);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		TypeInfo argumentType = parserToolbox.getObjectInfoProvider().getType(argInfo);
		return MatchRatings.isConvertibleTo(argumentType, expectedArgumentType);
	}

	public List<ExecutableInfo> getBestMatchingExecutableInfos(List<ExecutableInfo> availableExecutableInfos, List<ObjectInfo> argumentInfos) {
		ObjectInfoProvider objectInfoProvider = parserToolbox.getObjectInfoProvider();
		List<TypeInfo> argumentTypes = argumentInfos.stream().map(objectInfoProvider::getType).collect(Collectors.toList());
		Map<ExecutableInfo, TypeMatch> ratedExecutableInfos = availableExecutableInfos.stream()
			.collect(Collectors.toMap(
				executableInfo -> executableInfo,
				executableInfo -> executableInfo.rateArgumentMatch(argumentTypes)
			));

		for (boolean allowVariadicExecutables : Arrays.asList(false, true)) {
			for (List<TypeMatch> allowedRatings : ALLOWED_EXECUTABLE_RATINGS_BY_PHASE) {
				List<ExecutableInfo> executableInfos = filterExecutableInfos(ratedExecutableInfos, allowedRatings, allowVariadicExecutables);
				if (!executableInfos.isEmpty()) {
					return executableInfos;
				}
			}
		}
		return Collections.emptyList();
	}

	private static List<ExecutableInfo> filterExecutableInfos(Map<ExecutableInfo, TypeMatch> ratedExecutableInfos, List<TypeMatch> allowedRatings, boolean allowVariadicExecutables) {
		List<ExecutableInfo> filteredExecutableInfos = new ArrayList<>();
		for (ExecutableInfo executableInfo : ratedExecutableInfos.keySet()) {
			TypeMatch rating = ratedExecutableInfos.get(executableInfo);
			if (allowedRatings.contains(rating) && (allowVariadicExecutables || !executableInfo.isVariadic())) {
				filteredExecutableInfos.add(executableInfo);
			}
		}
		return filteredExecutableInfos;
	}

	public ExecutableArgumentInfo createExecutableArgumentInfo(List<ExecutableInfo> executableInfos, List<ObjectInfo> argumentInfos) {
		int currentArgumentIndex = argumentInfos.size();
		Map<ExecutableInfo, Boolean> applicableExecutableOverloads = new LinkedHashMap<>(executableInfos.size());
		for (ExecutableInfo executableInfo : executableInfos) {
			boolean applicable = IntStream.range(0, argumentInfos.size()).allMatch(i -> acceptsArgumentInfo(executableInfo, i, argumentInfos.get(i)));
			applicableExecutableOverloads.put(executableInfo, applicable);
		}
		return ParseOutcomes.createExecutableArgumentInfo(currentArgumentIndex, applicableExecutableOverloads);
	}

	/*
	 * Suggestions
	 */
	private StringMatch rateMethodByName(ExecutableInfo methodInfo, String expectedName) {
		return MatchRatings.rateStringMatch(methodInfo.getName(), expectedName);
	}

	private TypeMatch rateMethodByTypes(ExecutableInfo methodInfo, ParseExpectation expectation) {
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

	private AccessMatch rateMethodByAccess(ExecutableInfo methodInfo, boolean contextIsStatic) {
		return methodInfo.isStatic() && !contextIsStatic ? AccessMatch.STATIC_ACCESS_VIA_INSTANCE : AccessMatch.FULL;
	}

	private Function<ExecutableInfo, MatchRating> rateMethodFunc(String methodName, boolean contextIsStatic, ParseExpectation expectation) {
		return methodInfo -> MatchRatings.create(rateMethodByName(methodInfo, methodName), rateMethodByTypes(methodInfo, expectation), rateMethodByAccess(methodInfo, contextIsStatic));
	}

	public static String getMethodDisplayText(ExecutableInfo methodInfo) {
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
