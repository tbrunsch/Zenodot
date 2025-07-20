package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.common.Pair;
import dd.kms.zenodotx.rule.compound.ThenRule;
import dd.kms.zenodotx.rule.simple.CharacterRule;

public interface Rule<I, O, S extends GrammarSettings>
{
	Rule<I, O, S> name(String name);
	String name();

	default Rule<I, O, S> then(char character) {
		return then(new CharacterRule<>(character));
	}

	default <O2> Rule<I, O2, S> then(Rule<O, O2, S> nextRule) {
		return new ThenRule<>(this, nextRule);
	}

	default <O2> Rule<I, Pair<O, O2>, S> combineWith(Rule<Void, O2, S> nextRule) {
		return then(Rules.combineWithInput(nextRule));
	}
}
