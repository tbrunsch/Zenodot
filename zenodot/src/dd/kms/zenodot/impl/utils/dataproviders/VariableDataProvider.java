package dd.kms.zenodot.impl.utils.dataproviders;

import dd.kms.zenodot.api.common.ObjectInfoProvider;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.impl.utils.ParseUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for providing information about {@link Variable}s
 */
public class VariableDataProvider
{
	private final List<Variable>		variables;
	private final ObjectInfoProvider objectInfoProvider;

	public VariableDataProvider(List<Variable> variables, ObjectInfoProvider objectInfoProvider) {
		this.variables = variables;
		this.objectInfoProvider = objectInfoProvider;
	}

	public CodeCompletions completeVariable(String expectedName, ObjectParseResultExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> sortedVariables = variables.stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			sortedVariables,
			variable -> CodeCompletionFactory.variableCompletion(variable, insertionBegin, insertionEnd, rateVariable(variable, expectedName, expectation))
		);
		return new CodeCompletions(codeCompletions);
	}

	private StringMatch rateVariableByName(Variable variable, String expectedName) {
		return MatchRatings.rateStringMatch(expectedName, variable.getName());
	}

	private TypeMatch rateVariableByTypes(Variable variable, ObjectParseResultExpectation expectation) {
		Object value = variable.getValue();
		Class<?> valueType = value != null ? value.getClass() : InfoProvider.NO_TYPE;
		return expectation.rateTypeMatch(valueType);
	}

	private MatchRating rateVariable(Variable variable, String expectedName, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateVariableByName(variable, expectedName), rateVariableByTypes(variable, expectation), false);
	}
}
