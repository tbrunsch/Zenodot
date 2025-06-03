package dd.kms.zenodotx.java;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.java.rule.JavaRuleSet;
import dd.kms.zenodotx.rule.Rule;

public class ExpressionParser
{
	private static final JavaRuleSet									RULE_SET		= new JavaRuleSet();
	private static final Rule<Void, InstanceParseResult, JavaSettings>	FULL_EXPRESSION	= RULE_SET.getFullExpression();

	public static Object evaluate(String expression, int eventPosition, Event event, JavaSettings settings) throws SyntaxException, EvaluationException, Parser.EventResultException, SemanticException {
		InstanceParseResult instanceParseResult = compile(expression, eventPosition, event, settings);
		ObjectInfo compiledResult = instanceParseResult.getEvaluatedResult();
		Object compiledResultObject = compiledResult.getObject();
		if (compiledResultObject != InfoProvider.INDETERMINATE_VALUE) {
			// Happens for EvaluationMode.DYNAMIC_TYPING and can happen for	EvaluationMode.MIXED
			return compiledResultObject;
		}
		JavaSettings settingsForEvaluation = settings.withFullEvaluation();
		ObjectInfo evaluatedResultObject = instanceParseResult.evaluate(settingsForEvaluation);
		return evaluatedResultObject.getObject();
	}

	public static InstanceParseResult compile(String expression, int eventPosition, Event event, JavaSettings settings) throws SyntaxException, EvaluationException, Parser.EventResultException, SemanticException {
		Parser<JavaSettings> parser = new Parser<>(expression, eventPosition, event);
		return parser.parse(FULL_EXPRESSION, null, settings);
	}
}
