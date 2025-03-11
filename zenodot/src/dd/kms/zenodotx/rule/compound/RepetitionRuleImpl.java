package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

public class RepetitionRuleImpl<S extends State> implements RepetitionRule<S>
{
	private Rule<S> ruleToRepeat;

	RepetitionRuleImpl(Rule<S> ruleToRepeat) {
		this.ruleToRepeat = ruleToRepeat;
	}

	@Override
	public Rule<S> getRuleToRepeat() {
		return ruleToRepeat;
	}
}
