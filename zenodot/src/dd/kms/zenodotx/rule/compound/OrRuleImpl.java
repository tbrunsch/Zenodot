package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrRuleImpl<S extends State<S>> implements OrRule<S>
{
	private String			name;
	private List<Rule<S>> 	alternatives	= new ArrayList<>();

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setAlternatives(Rule<S>... rules) {
		setAlternatives(Arrays.asList(rules));
	}

	@Override
	public void setAlternatives(List<Rule<S>> rules) {
		alternatives = new ArrayList<>(rules);
	}

	@Override
	public List<Rule<S>> getAlternatives() {
		return alternatives;
	}

	@Override
	public OrRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule) {
		List<Rule<S>> replacedAlternatives = alternatives.stream()
			.map(rule -> Rules.replaceRule(rule, oldRule, newRule))
			.collect(Collectors.toList());
		return Rules.or(replacedAlternatives);
	}

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
