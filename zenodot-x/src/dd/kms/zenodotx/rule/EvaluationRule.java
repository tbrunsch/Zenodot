package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;

public interface EvaluationRule<I, O, S extends GrammarSettings> extends Rule<I, O, S>
{
	O evaluate(I input, S settings) throws SemanticException, EvaluationException;
}
