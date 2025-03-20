package dd.kms.zenodotx.state;

import dd.kms.zenodotx.java.ParserSettings;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.rule.PreparsedGrammar;
import dd.kms.zenodotx.rule.simple.SemanticRule;

public interface State<S extends State<S>>
{
	void store();
	void restore();
	void evaluate(SemanticRule<S> rule, String text) throws EvaluationException, SemanticException;
	PreparsedGrammar<S> getPreparsedGrammar();
}
