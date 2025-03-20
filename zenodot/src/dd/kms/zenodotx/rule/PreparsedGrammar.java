package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.state.State;

@FunctionalInterface
public interface PreparsedGrammar<S extends State<S>>
{
	void evaluate(S state) throws SemanticException, EvaluationException;
}
