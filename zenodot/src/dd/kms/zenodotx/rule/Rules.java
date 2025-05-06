package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.rule.compound.*;
import dd.kms.zenodotx.rule.simple.CharacterRule;
import dd.kms.zenodotx.rule.simple.EmptyRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SpaceRule;

import java.util.Arrays;
import java.util.List;

public class Rules
{
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <I, O, S> OrRule<I, O, S> or(Rule<I, O, S>... alternatives) {
		return or(Arrays.asList(alternatives));
	}

	public static <I, O, S> OrRule<I, O, S> or(List<Rule<I, O, S>> alternatives) {
		OrRule<I, O, S> orRule = new OrRuleImpl<>();
		orRule.setAlternatives(alternatives);
		return orRule;
	}

	public static <IO, S> Rule<IO, IO, S> repeat(Rule<IO, IO, S> ruleToRepeat) {
		return repeat(null, ruleToRepeat);
	}

	public static <IO, S> Rule<IO, IO, S> repeat(String name, Rule<IO, IO, S> ruleToRepeat) {
		RepetitionRule<IO, S> repetitionRule = new RepetitionRule<>(ruleToRepeat);
		repetitionRule.name(name);
		return repetitionRule;
	}

	public static <I, O, S> DelegatingRule<I, O, S> createDelegate() {
		return new DelegatingRuleImpl<>();
	}

	public static <IO, S> SimpleRule<IO, IO, S> character(char character) {
		return new CharacterRule<>(character);
	}

	public static <IO, S> SimpleRule<IO, IO, S> space() {
		return new SpaceRule<>();
	}

	public static <IO, S> SimpleRule<IO, IO, S> empty() {
		return new EmptyRule<>();
	}
}
