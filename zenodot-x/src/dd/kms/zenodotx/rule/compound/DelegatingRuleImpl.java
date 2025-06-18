package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

public class DelegatingRuleImpl<I, O, S> extends AbstractRule<I, O, S> implements DelegatingRule<I, O, S>
{
	private Rule<I, O, S> delegate;

	@Override
	public DelegatingRule<I, O, S> name(String name) {
		super.name(name);
		return this;
	}

	@Override
	protected String getGenericName() {
		return delegate != null
			? "Delegate to \"" + delegate + "\""
			: null;
	}

	@Override
	public O parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		if (delegate == null) {
			throw new IllegalStateException("No delegate has been set for this " + DelegatingRule.class.getSimpleName());
		}
		return parser.parse(delegate, input, settings);
	}

	@Override
	public void parseSyntactically(Parser<S> parser) throws SyntaxException {
		if (delegate == null) {
			throw new IllegalStateException("No delegate has been set for this " + DelegatingRule.class.getSimpleName());
		}
		parser.parseSyntactically(delegate);
	}

	@Override
	public void setDelegate(Rule<I, O, S> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Rule<I, O, S> getDelegate() {
		return delegate;
	}
}
