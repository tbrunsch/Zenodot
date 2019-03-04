package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.parsers.ParseExpectationBuilder;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

	public CompletionSuggestions suggestMethods(String expectedName, List<ExecutableInfo> methodInfos, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			methodInfos,
			methodInfo -> new CompletionSuggestionMethod(methodInfo, insertionBegin, insertionEnd),
			rateMethodByNameAndTypesFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	public List<ParseResultIF> parseExecutableArguments(TokenStream tokenStream, List<ExecutableInfo> availableExecutableInfos) {
		List<ParseResultIF> arguments = new ArrayList<>();

		int position;
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken != null && characterToken.getValue().charAt(0) == '(';

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				arguments.add(new ParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
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
				arguments.add(new ParseError(tokenStream.getPosition(), "No further arguments expected", ParseError.ErrorType.SEMANTIC_ERROR));
				return arguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseExpectation argumentExpectation = ParseExpectationBuilder.expectObject().allowedTypes(expectedArgumentTypes_i).build();
			ParseResultIF argumentParseResult_i = parserToolbox.getRootParser().parse(tokenStream, parserToolbox.getThisInfo(), argumentExpectation);
			arguments.add(argumentParseResult_i);

			if (ParseUtils.propagateParseResult(argumentParseResult_i, argumentExpectation)) {
				return arguments;
			}

			ObjectParseResult parseResult = ((ObjectParseResult) argumentParseResult_i);
			int parsedToPosition = parseResult.getPosition();
			tokenStream.moveTo(parsedToPosition);
			ObjectInfo argumentInfo = parseResult.getObjectInfo();
			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> acceptsArgumentInfo(executableInfo, i, argumentInfo)).collect(Collectors.toList());

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
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
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
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
		TypeInfo expectedArgumentType = executableInfo.getExpectedArgumentType(argIndex);
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

	private Function<ExecutableInfo, MatchRating> rateMethodByNameAndTypesFunc(String methodName, ParseExpectation expectation) {
		return methodInfo -> new MatchRating(rateMethodByName(methodInfo, methodName), rateMethodByTypes(methodInfo, expectation));
	}

	public static String getMethodDisplayText(ExecutableInfo methodInfo) {
		return methodInfo.getName() + " (" + methodInfo.getDeclaringType() + ")";
	}
}
