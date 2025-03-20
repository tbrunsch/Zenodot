package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

import java.util.List;

public interface OrRule<S extends State<S>> extends CompoundRule<S>
{
	void setAlternatives(Rule<S>... rules);
	void setAlternatives(List<Rule<S>> rules);
	List<Rule<S>> getAlternatives();

	@Override
	OrRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule);
}
