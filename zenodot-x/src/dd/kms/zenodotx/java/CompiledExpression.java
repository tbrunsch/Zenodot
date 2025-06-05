package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodotx.exception.EvaluationException;

public interface CompiledExpression
{
	Class<?> getResultType();
	Object evaluate(Object thisValue, Variables variables) throws EvaluationException;
}
