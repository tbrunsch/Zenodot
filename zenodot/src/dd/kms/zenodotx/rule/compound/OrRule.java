package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

import java.util.List;

public interface OrRule<S extends State> extends CompoundRule<S>
{
	List<Rule<S>> getAlternatives();
}
