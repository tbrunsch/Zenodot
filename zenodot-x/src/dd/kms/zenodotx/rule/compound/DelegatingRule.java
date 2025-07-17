package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.rule.Rule;

public interface DelegatingRule<I, O, S extends GrammarSettings> extends CompoundRule<I, O, S>
{
	@Override
	DelegatingRule<I, O, S> name(String name);
	void setDelegate(Rule<I, O, S> delegate);
	Rule<I, O, S> getDelegate();
}
