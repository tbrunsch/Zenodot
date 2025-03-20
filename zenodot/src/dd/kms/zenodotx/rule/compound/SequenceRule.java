package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

import java.util.List;

public interface SequenceRule<S extends State<S>> extends CompoundRule<S>
{
	void setSequence(Rule<S>... sequence);
	void setSequence(List<Rule<S>> sequence);
	List<Rule<S>> getSequence();

	@Override
	SequenceRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule);
}
