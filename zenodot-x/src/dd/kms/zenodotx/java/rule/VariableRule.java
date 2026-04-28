package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.Collection;

public class VariableRule extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<Void, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, InstanceParseResult>() {
			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				// TODO
			}

			@Override
			public InstanceParseResult evaluate(Void input, String variableName, JavaSettings settings) throws SemanticException, EvaluationException {
				Variables variables = settings.getVariables();
				Collection<String> variableNames = variables.getNames();
				for (String name : variableNames) {
					if (variableName.equals(name)) {
						ObjectInfo valueInfo = ((VariablesImpl) variables).getValueInfo(variableName);
						return new VariableParseResult(variableName, valueInfo);
					}
				}
				throw new SemanticException("Unknown variable \"" + variableName + "\"");
			}
		};
	}
}
