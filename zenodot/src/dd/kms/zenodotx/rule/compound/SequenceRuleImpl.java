package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

import java.util.ArrayList;
import java.util.List;

public class SequenceRuleImpl<S extends State> implements SequenceRule<S>
{
	private List<Rule<S>>	sequence	= new ArrayList<>();

	@Override
	public List<Rule<S>> getSequence() {
		return sequence;
	}
}
