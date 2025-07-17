package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

/**
 * This class can serve as base class for all {@link CompoundRule} implementations that have a wrapped
 * rule, but don't forward the input to that rule, but {@code null}, and then combine the input and the
 * result of the wrapped rule.<br>
 * <br>
 * <b>Example:</b> The rule that performs a class cast can be implemented as an {@code AbstractCombinedRule}:
 * The input is the class to cast to, the wrapped rule represents the expression, and combining the class
 * and the parsed expression yields the cast expression.
 */
public abstract class AbstractCombineRule<I, O, Combined, S extends GrammarSettings> extends AbstractRule<I, Combined, S> implements CompoundRule<I, Combined, S>
{
	private final Rule<Void, O, S>	ruleToCombine;

	protected abstract Combined combine(I input, O otherOutput, S settings) throws SemanticException, EvaluationException;

	protected AbstractCombineRule(Rule<Void, O, S> ruleToCombine) {
		this.ruleToCombine = ruleToCombine;
	}

	@Override
	public final Combined parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		O output = parser.parse(ruleToCombine, null, settings);
		return combine(input, output, settings);
	}

	@Override
	public void parseSyntactically(Parser<S> parser, S settings) throws SyntaxException {
		parser.parseSyntactically(ruleToCombine, settings);
	}
}
