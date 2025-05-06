package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.rule.compound.ThenRule;
import dd.kms.zenodotx.rule.simple.CharacterRule;

public interface Rule<I, O, S>
{
	Rule<I, O, S> name(String name);
	String name();

	default Rule<I, O, S> then(char character) {
		return then(new CharacterRule<>(character));
	}

	default <T> Rule<I, T, S> then(Rule<O, T, S> nextRule) {
		return new ThenRule<>(this, nextRule);
	}
}
