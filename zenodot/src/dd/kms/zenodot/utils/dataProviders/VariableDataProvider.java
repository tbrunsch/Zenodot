package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.VariablePool;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for providing information about {@link Variable}s
 */
public class VariableDataProvider
{
	private final VariablePool variablePool;

	public VariableDataProvider(VariablePool variablePool) {
		this.variablePool = variablePool;
	}

	public CompletionSuggestions suggestVariables(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			rateVariableByNameAndTypesFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateVariableByName(Variable variable, String expectedName) {
		return MatchRatings.rateStringMatch(variable.getName(), expectedName);
	}

	private TypeMatch rateVariableByTypes(Variable variable, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		Object value = variable.getValue();
		TypeInfo valueType = value == null ? TypeInfo.NONE : TypeInfo.of(value.getClass());
		return	allowedTypes == null
					? TypeMatch.FULL
					: allowedTypes.stream().map(allowedType -> MatchRatings.rateTypeMatch(valueType, allowedType)).min(TypeMatch::compareTo).orElse(TypeMatch.NONE);
	}

	private Function<Variable, MatchRating> rateVariableByNameAndTypesFunc(String variableName, ParseExpectation expectation) {
		return variable -> new MatchRating(rateVariableByName(variable, variableName), rateVariableByTypes(variable, expectation));
	}

	public static String getVariableDisplayText(Variable variable) {
		return variable.getName();
	}
}
