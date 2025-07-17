package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.Rule;

public interface CompoundRule<I, O, S extends GrammarSettings> extends Rule<I, O, S>
{
	O parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException;
	void parseSyntactically(Parser<S> parser) throws SyntaxException;
}
