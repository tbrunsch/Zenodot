package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
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
	private final List<Variable> variables;

	public VariableDataProvider(List<Variable> variables) {
		this.variables = variables;
	}

	public CompletionSuggestions suggestVariables(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> sortedVariables = variables.stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			sortedVariables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			rateVariableFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateVariableByName(Variable variable, String expectedName) {
		return MatchRatings.rateStringMatch(variable.getName(), expectedName);
	}

	private TypeMatch rateVariableByTypes(Variable variable, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		Object value = variable.getValue();
		TypeInfo valueType = value == null ? InfoProvider.NO_TYPE : InfoProvider.createTypeInfo(value.getClass());
		return	allowedTypes == null
					? TypeMatch.FULL
					: allowedTypes.stream().map(allowedType -> MatchRatings.rateTypeMatch(valueType, allowedType)).min(TypeMatch::compareTo).orElse(TypeMatch.NONE);
	}

	private Function<Variable, MatchRating> rateVariableFunc(String variableName, ParseExpectation expectation) {
		return variable -> MatchRatings.create(rateVariableByName(variable, variableName), rateVariableByTypes(variable, expectation), AccessMatch.IGNORED);
	}

	public static String getVariableDisplayText(Variable variable) {
		return variable.getName();
	}
}
