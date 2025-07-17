package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.rule.Rule;

public interface SimpleRule<I, O, S extends GrammarSettings> extends Rule<I, O, S>
{
	SyntaxRule getSyntaxRule();
	SemanticRule<I, O, S> getSemanticRule();
}
