package dd.kms.zenodot.utils.dataproviders;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.*;
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

	public CodeCompletions completeMethod(List<ExecutableInfo> methodInfos, boolean contextIsStatic, String expectedName, ObjectParseResultExpectation expectation, int insertionBegin, int insertionEnd) {
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			methodInfos,
			methodInfo -> CodeCompletionFactory.methodCompletion(methodInfo, insertionBegin, insertionEnd, rateMethod(methodInfo, expectedName, contextIsStatic, expectation))
		);
		return new CodeCompletions(codeCompletions);
	}

	/**
	 * Parses the arguments of executables including the final ')'
	 */
	public List<ObjectParseResult> parseArguments(TokenStream tokenStream, List<ExecutableInfo> executables) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		List<ObjectParseResult> arguments = new ArrayList<>();

		boolean foundClosingParenthesis = false;
		if (tokenStream.skipCharacter(')')) {
			foundClosingParenthesis = true;
		}

		// iterate over the argument list (i = argument index)
		for (int i = 0; !foundClosingParenthesis; i++) {
			// only consider executables that accept an i'th argument
			executables = filterExecutablesWithValidArgumentIndex(executables, i);
			if (executables.isEmpty()) {
				if (i == 0) {
					/*
					 * Scenario: method(XYZ for X != ')' and a method without arguments
					 *
					 * If XYZ is empty and contains the caret, then we simply return
					 * no suggestions. Otherwise we throw a parse exception.
					 */
					tokenStream.readRemainingWhitespaces(TokenStream.NO_COMPLETIONS, "No further arguments expected");
					throw new InternalParseException("Missing ')'");
				} else {
					/*
					 * Scenario: method(arg_1, arg_2, ..., arg_n, XYZ for X != ')' and no method
					 *           accepts more than n arguments.
					 * => Throw a parse exception
					 */
					throw new InternalParseException("No further arguments expected");
				}
			}

			// parse argument i
			List<TypeInfo> expectedArgTypes_i = getExpectedArgumentTypes(executables, i);
			ObjectParseResultExpectation argExpectation = new ObjectParseResultExpectation(expectedArgTypes_i, true);
			ObjectParseResult argument_i;
			try {
				argument_i = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), argExpectation);
			} catch (CodeCompletionException e) {
				CodeCompletions completions = e.getCompletions();
				if (!completions.getExecutableArgumentInfo().isPresent()) {
					/*
					 * The code completion does not come from a cascaded method/constructor call
					 * that already has an executable argument info => We can provide some for this
					 * method call.
					 */
					List<ObjectInfo> argumentInfos = arguments.stream()
						.map(ObjectParseResult::getObjectInfo)
						.collect(Collectors.toList());
					ExecutableArgumentInfo executableArgumentInfo = createExecutableArgumentInfo(executables, argumentInfos);
					completions.setExecutableArgumentInfo(executableArgumentInfo);
				}
				throw e;
			}
			arguments.add(argument_i);

			// only consider executables that accept argument_i as the i'th argument
			ObjectInfo argument = argument_i.getObjectInfo();
			executables = filterExecutablesWithValidArgumentType(executables, i, argument);
			assert !executables.isEmpty();

			foundClosingParenthesis = tokenStream.readCharacter(',', ')') == ')';
		}
		return arguments;
	}

	private List<ExecutableInfo> filterExecutablesWithValidArgumentIndex(List<ExecutableInfo> executables, int argIndex) {
		return executables.stream()
			.filter(executable -> executable.isArgumentIndexValid(argIndex))
			.collect(Collectors.toList());
	}

	private List<ExecutableInfo> filterExecutablesWithValidArgumentType(List<ExecutableInfo> executables, int argIndex, ObjectInfo arg) {
		return executables.stream()
			.filter(executable -> acceptsArgument(executable, argIndex, arg))
			.collect(Collectors.toList());
	}

	// assumes that each of the executables accepts an argument for index argIndex
	private List<TypeInfo> getExpectedArgumentTypes(List<ExecutableInfo> executables, int argIndex) {
		return executables.stream()
			.map(executable -> executable.getExpectedArgumentType(argIndex))
			.distinct()
			.collect(Collectors.toList());
	}

	private boolean acceptsArgument(ExecutableInfo executable, int argIndex, ObjectInfo arg) {
		TypeInfo expectedArgType;
		try {
			expectedArgType = executable.getExpectedArgumentType(argIndex);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		TypeInfo argType = parserToolbox.getObjectInfoProvider().getType(arg);
		return MatchRatings.isConvertibleTo(argType, expectedArgType);
	}

	public List<ExecutableInfo> getBestMatchingExecutables(List<ExecutableInfo> availableExecutableInfos, List<ObjectInfo> argumentInfos) {
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
			boolean applicable = IntStream.range(0, argumentInfos.size()).allMatch(i -> acceptsArgument(executableInfo, i, argumentInfos.get(i)));
			applicableExecutableOverloads.put(executableInfo, applicable);
		}
		return ParseResults.createExecutableArgumentInfo(currentArgumentIndex, applicableExecutableOverloads);
	}

	/*
	 * Code Completions
	 */
	private StringMatch rateMethodByName(ExecutableInfo methodInfo, String expectedName) {
		return MatchRatings.rateStringMatch(expectedName, methodInfo.getName());
	}

	private TypeMatch rateMethodByTypes(ExecutableInfo methodInfo, ObjectParseResultExpectation expectation) {
		/*
		 * Even for EvaluationMode.DYNAMICALLY_TYPED we only consider the declared return type of the method instead
		 * of the runtime type of the returned object. Otherwise, we would have to invoke the method for code
		 * completion, possibly causing undesired side effects.
		 */
		return expectation.rateTypeMatch(methodInfo.getReturnType());
	}

	private boolean isMethodAccessDiscouraged(ExecutableInfo methodInfo, boolean contextIsStatic) {
		return methodInfo.isStatic() && !contextIsStatic;
	}

	private MatchRating rateMethod(ExecutableInfo methodInfo, String methodName, boolean contextIsStatic, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateMethodByName(methodInfo, methodName), rateMethodByTypes(methodInfo, expectation), isMethodAccessDiscouraged(methodInfo, contextIsStatic));
	}

	public static String getMethodDisplayText(ExecutableInfo methodInfo) {
		int numArguments = methodInfo.getNumberOfArguments();
		final String argumentsAsString;
		if (methodInfo.isVariadic()) {
			int lastArgumentIndex = numArguments - 1;
			argumentsAsString = IntStream.range(0, numArguments).mapToObj(i -> methodInfo.getExpectedArgumentType(i).getRawType().getSimpleName() + (i == lastArgumentIndex ? "..." : "")).collect(Collectors.joining(", "));
		} else {
			argumentsAsString = IntStream.range(0, numArguments).mapToObj(i -> methodInfo.getExpectedArgumentType(i).getRawType().getSimpleName()).collect(Collectors.joining(", "));
		}
		return methodInfo.getName()
				+ "("
				+ argumentsAsString
				+ ")";
	}
}
