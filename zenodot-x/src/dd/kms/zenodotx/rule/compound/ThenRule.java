package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

public class ThenRule<I, O, T, S> extends AbstractRule<I, T, S> implements CompoundRule<I, T, S>
{
	private final Rule<I, O, S> firstRule;
	private final Rule<O, T, S>	secondRule;

	public ThenRule(Rule<I, O, S> firstRule, Rule<O, T, S> secondRule) {
		this.firstRule = firstRule;
		this.secondRule = secondRule;
	}

	@Override
	public T parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		O interimOutput = parser.parse(firstRule, input, settings);
		return parser.parse(secondRule, interimOutput, settings);
	}

	@Override
	public void parseSyntactically(Parser<S> parser) throws SyntaxException {
		parser.parseSyntactically(firstRule);
		parser.parseSyntactically(secondRule);
	}

	@Override
	protected String getGenericName() {
		return "rule 1, then rule 2";
	}
}
