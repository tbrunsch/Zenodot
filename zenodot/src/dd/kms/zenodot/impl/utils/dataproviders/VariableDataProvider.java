package dd.kms.zenodot.impl.utils.dataproviders;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodot.impl.common.ObjectInfoProvider;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for providing information about variables.
 */
public class VariableDataProvider
{
	private final VariablesImpl			variables;
	private final ObjectInfoProvider	objectInfoProvider;

	public VariableDataProvider(VariablesImpl variables, ObjectInfoProvider objectInfoProvider) {
		this.variables = variables;
		this.objectInfoProvider = objectInfoProvider;
	}

	public CodeCompletions completeVariable(String expectedName, ObjectParseResultExpectation expectation, int insertionBegin, int insertionEnd) {
		List<String> sortedVariableNames = variables.getNames().stream().sorted().collect(Collectors.toList());
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			sortedVariableNames,
			variableName -> CodeCompletionFactory.variableCompletion(variableName, insertionBegin, insertionEnd, rateVariable(variableName, expectedName, expectation))
		);
		return new CodeCompletions(codeCompletions);
	}

	private StringMatch rateVariableByName(String variableName, String expectedName) {
		return MatchRatings.rateStringMatch(expectedName, variableName);
	}

	private TypeMatch rateVariableByTypes(String variableName, ObjectParseResultExpectation expectation) {
		ObjectInfo valueInfo = variables.getValueInfo(variableName);
		return expectation.rateTypeMatch(valueInfo.getDeclaredType());
	}

	private MatchRating rateVariable(String variableName, String expectedName, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateVariableByName(variableName, expectedName), rateVariableByTypes(variableName, expectation), false);
	}
}
