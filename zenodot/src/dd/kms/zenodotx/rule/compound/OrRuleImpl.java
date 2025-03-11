package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

import java.util.ArrayList;
import java.util.List;

public class OrRuleImpl<S extends State> implements OrRule<S>
{
	private List<Rule<S>> alternatives	= new ArrayList<>();

	@Override
	public List<Rule<S>> getAlternatives() {
		return alternatives;
	}
}
