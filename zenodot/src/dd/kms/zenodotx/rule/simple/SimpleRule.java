package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.state.State;

public interface SimpleRule<S extends State<S>> extends Rule<S>
{
	SyntaxRule getSyntaxRule();
	SemanticRule<S> getSemanticRule();
}
