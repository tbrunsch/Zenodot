package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.rule.compound.*;
import dd.kms.zenodotx.rule.simple.*;
import dd.kms.zenodotx.state.State;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Rules
{
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <S extends State<S>> SequenceRule<S> sequence(Rule<S>... sequence) {
		return sequence(null, sequence);
	}

	public static <S extends State<S>> SequenceRule<S> sequence(List<Rule<S>> sequence) {
		return sequence(null, sequence);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <S extends State<S>> SequenceRule<S> sequence(String name, Rule<S>... sequence) {
		return sequence(name, Arrays.asList(sequence));
	}

	public static <S extends State<S>> SequenceRule<S> sequence(String name, List<Rule<S>> sequence) {
		SequenceRule<S> sequenceRule = new SequenceRuleImpl<>();
		sequenceRule.setName(name);
		sequenceRule.setSequence(sequence);
		return sequenceRule;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <S extends State<S>> OrRule<S> or(Rule<S>... alternatives) {
		return or(null, alternatives);
	}

	public static <S extends State<S>> OrRule<S> or(List<Rule<S>> alternatives) {
		return or(null, alternatives);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <S extends State<S>> OrRule<S> or(String name, Rule<S>... alternatives) {
		return or(name, Arrays.asList(alternatives));
	}

	public static <S extends State<S>> OrRule<S> or(String name, List<Rule<S>> alternatives) {
		OrRule<S> orRule = new OrRuleImpl<>();
		orRule.setName(name);
		orRule.setAlternatives(alternatives);
		return orRule;
	}

	public static <S extends State<S>> RepetitionRule<S> repeat(Rule<S> ruleToRepeat) {
		return repeat(null, ruleToRepeat);
	}

	public static <S extends State<S>> RepetitionRule<S> repeat(String name, Rule<S> ruleToRepeat) {
		RepetitionRule<S> repetitionRule = new RepetitionRuleImpl<>(ruleToRepeat);
		repetitionRule.setName(name);
		return repetitionRule;
	}

	public static <S extends State<S>> SimpleRule<S> character(char character) {
		return new CharacterRule<>(character);
	}

	public static <S extends State<S>> SimpleRule<S> space() {
		return new SpaceRule<>();
	}

	public static <S extends State<S>> SimpleRule<S> empty() {
		return new EmptyRule<>();
	}

	public static <S extends State<S>> SimpleRule<S> action(Consumer<S> action) {
		return new ActionRule<>(action);
	}

	public static <S extends State<S>> Rule<S> replaceRule(Rule<S> rule, SimpleRule<S> oldRule, SimpleRule<S> newRule) {
		if (rule instanceof CompoundRule) {
			return ((CompoundRule<S>) rule).replace(oldRule, newRule);
		} else if (rule instanceof SimpleRule) {
			return Objects.equals(rule, oldRule) ? newRule : rule;
		} else {
			throw new IllegalStateException("Unsupported rule type " + rule.getClass().getName() + ": Only CompoundRules and SimpleRules are supported.");
		}
	}
}
