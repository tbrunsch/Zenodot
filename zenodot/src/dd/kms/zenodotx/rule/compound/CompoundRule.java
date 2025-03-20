package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

public interface CompoundRule<S extends State<S>> extends Rule<S>
{
	void setName(String name);
	String getName();
	CompoundRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule);
}
