package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.CompletionSuggestionVariable;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.settings.VariablePool;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class VariableDataProvider
{
	private final VariablePool variablePool;

	public VariableDataProvider(VariablePool variablePool) {
		this.variablePool = variablePool;
	}

	public CompletionSuggestions suggestVariables(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			rateVariableByNameAndTypesFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private int rateVariableByName(Variable variable, String expectedName) {
		return ParseUtils.rateStringMatch(variable.getName(), expectedName);
	}

	private int rateVariableByTypes(Variable variable, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		Object value = variable.getValue();
		return	allowedTypes == null	? ParseUtils.TYPE_MATCH_FULL :
				allowedTypes.isEmpty()	? ParseUtils.TYPE_MATCH_NONE
										: allowedTypes.stream().mapToInt(allowedType -> ParseUtils.rateTypeMatch(value == null ? TypeInfo.NONE : TypeInfo.of(value.getClass()), allowedType)).min().getAsInt();
	}

	private ToIntFunction<Variable> rateVariableByNameAndTypesFunc(String variableName, ParseExpectation expectation) {
		return variable -> (ParseUtils.TYPE_MATCH_NONE + 1)*rateVariableByName(variable, variableName) + rateVariableByTypes(variable, expectation);
	}

	public static String getVariableDisplayText(Variable variable) {
		return variable.getName();
	}
}
