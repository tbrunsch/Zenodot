package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

public class RepetitionRuleImpl<S extends State<S>> implements RepetitionRule<S>
{
	private String	name;
	private Rule<S> ruleToRepeat;

	public RepetitionRuleImpl(Rule<S> ruleToRepeat) {
		this.ruleToRepeat = ruleToRepeat;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Rule<S> getRuleToRepeat() {
		return ruleToRepeat;
	}

	@Override
	public RepetitionRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule) {
		Rule<S> replacedRuleToRepeat = Rules.replaceRule(ruleToRepeat, oldRule, newRule);
		return Rules.repeat(replacedRuleToRepeat);
	}

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
