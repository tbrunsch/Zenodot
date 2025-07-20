package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;

public class InvokeInstanceConstructorRule extends AbstractRule<ConstructorParseInfo, InstanceParseResult, JavaSettings> implements EvaluationRule<ConstructorParseInfo, InstanceParseResult, JavaSettings>
{
	@Override
	public InstanceParseResult evaluate(ConstructorParseInfo input, JavaSettings settings) throws SemanticException, EvaluationException {
		// TODO: Finish InvokeMethodRule and then copy things from it
		return null;
	}

	@Override
	protected String getGenericName() {
		return "evaluate new Class(...)";
	}
}
