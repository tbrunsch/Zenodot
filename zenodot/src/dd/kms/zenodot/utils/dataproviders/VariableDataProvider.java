package dd.kms.zenodot.utils.dataproviders;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Comparator;
import java.util.List;
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

	public CodeCompletions completeVariable(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> sortedVariables = variables.stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			sortedVariables,
			variable -> CodeCompletionFactory.variableCompletion(variable, insertionBegin, insertionEnd, rateVariable(variable, expectedName, expectation))
		);
		return new CodeCompletions(insertionBegin, codeCompletions);
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

	private MatchRating rateVariable(Variable variable, String expectedName, ParseExpectation expectation) {
		return MatchRatings.create(rateVariableByName(variable, expectedName), rateVariableByTypes(variable, expectation), false);
	}
}
