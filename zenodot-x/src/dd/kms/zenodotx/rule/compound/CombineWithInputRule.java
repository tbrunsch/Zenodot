package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.common.Pair;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

/**
 * This rule wraps a rule that does not take any input and returns a rule that accepts any input and returns a
 * {@link Pair} containing the input and the wrapped rule's output.
 */
public class CombineWithInputRule<I, O, S extends GrammarSettings> extends AbstractRule<I, Pair<I, O>, S> implements CompoundRule<I, Pair<I, O>, S>
{
	private final Rule<Void, O, S>	rule;

	public CombineWithInputRule(Rule<Void, O, S> rule) {
		this.rule = rule;
	}

	@Override
	public Pair<I, O> parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		O output = parser.parse(rule, null, settings);
		return new Pair<>(input, output);
	}

	@Override
	public void parseSyntactically(Parser<S> parser, S settings) throws SyntaxException {
		parser.parseSyntactically(rule, settings);
	}
}
