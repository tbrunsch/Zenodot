package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class ConstructorParameterRule extends AbstractRule<ConstructorParseInfo, ConstructorParseInfo, JavaSettings> implements CompoundRule<ConstructorParseInfo, ConstructorParseInfo, JavaSettings>
{
	private final Rule<Void, InstanceParseResult, JavaSettings> expressionRule;

	public ConstructorParameterRule(Rule<Void, InstanceParseResult, JavaSettings> expressionRule) {
		this.expressionRule = expressionRule;
	}

	@Override
	public ConstructorParseInfo parse(ConstructorParseInfo input, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		InstanceParseResult parameter = parser.parse(expressionRule, null, settings);
		input.addParameter(parameter);
		return input;
	}

	@Override
	protected String getGenericName() {
		return "new Class(..., parameter, ...)";
	}
}
