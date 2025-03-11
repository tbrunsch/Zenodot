package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

public interface RepetitionRule<S extends State> extends CompoundRule<S>
{
	Rule<S> getRuleToRepeat();
}
