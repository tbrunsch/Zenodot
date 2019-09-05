package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionFactory;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
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
	private final List<Variable>		variables;
	private final ObjectInfoProvider	objectInfoProvider;

	public VariableDataProvider(List<Variable> variables, ObjectInfoProvider objectInfoProvider) {
		this.variables = variables;
		this.objectInfoProvider = objectInfoProvider;
	}

	public CompletionSuggestions suggestVariables(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> sortedVariables = variables.stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			sortedVariables,
			variable -> CompletionSuggestionFactory.variableSuggestion(variable, insertionBegin, insertionEnd),
			rateVariableFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateVariableByName(Variable variable, String expectedName) {
		return MatchRatings.rateStringMatch(variable.getName(), expectedName);
	}

	private TypeMatch rateVariableByTypes(Variable variable, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		ObjectInfo value = variable.getValue();
		TypeInfo valueType = objectInfoProvider.getType(value);
		return	allowedTypes == null
					? TypeMatch.FULL
					: allowedTypes.stream().map(allowedType -> MatchRatings.rateTypeMatch(valueType, allowedType)).min(TypeMatch::compareTo).orElse(TypeMatch.NONE);
	}

	private Function<Variable, MatchRating> rateVariableFunc(String variableName, ParseExpectation expectation) {
		return variable -> MatchRatings.create(rateVariableByName(variable, variableName), rateVariableByTypes(variable, expectation), AccessMatch.IGNORED);
	}
}
