package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

public interface RepetitionRule<S extends State<S>> extends CompoundRule<S>
{
	Rule<S> getRuleToRepeat();

	@Override
	RepetitionRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule);
}
