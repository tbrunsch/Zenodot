package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.rule.Rule;

import java.util.List;

public interface OrRule<I, O, S extends GrammarSettings> extends CompoundRule<I, O, S>
{
	@Override
	OrRule<I, O, S> name(String name);
	void setAlternatives(Rule<I, O, S>... rules);
	void setAlternatives(List<Rule<I, O, S>> rules);
	List<Rule<I, O, S>> getAlternatives();
}
