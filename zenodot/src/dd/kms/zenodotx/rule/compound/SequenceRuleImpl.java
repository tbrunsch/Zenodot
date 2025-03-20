package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SequenceRuleImpl<S extends State<S>> implements SequenceRule<S>
{
	private String			name;
	private List<Rule<S>>	sequence	= new ArrayList<>();

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setSequence(Rule<S>... sequence) {
		setSequence(Arrays.asList(sequence));
	}

	@Override
	public void setSequence(List<Rule<S>> sequence) {
		this.sequence = new ArrayList<>(sequence);
	}

	@Override
	public List<Rule<S>> getSequence() {
		return sequence;
	}

	@Override
	public SequenceRule<S> replace(SimpleRule<S> oldRule, SimpleRule<S> newRule) {
		List<Rule<S>> replacedSequence = sequence.stream()
			.map(rule -> Rules.replaceRule(rule, oldRule, newRule))
			.collect(Collectors.toList());
		return Rules.sequence(replacedSequence);
	}

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
