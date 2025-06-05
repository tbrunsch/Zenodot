package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
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
		InstanceParseResult instanceParseResult = doCompile(expression, eventPosition, event, settings);
		ObjectInfo compiledResult = instanceParseResult.getEvaluatedResult();
		Object compiledResultObject = compiledResult.getObject();
		if (compiledResultObject != InfoProvider.INDETERMINATE_VALUE) {
			// Happens for EvaluationMode.DYNAMIC_TYPING and can happen for	EvaluationMode.MIXED
			return compiledResultObject;
		}
		// TODO: Consider variables
		ObjectInfo evaluatedResultObject = instanceParseResult.evaluate(settings.getThisInfo(), null, EvaluationMode.DYNAMIC_TYPING);
		return evaluatedResultObject.getObject();
	}

	public static CompiledExpression compile(String expression, int eventPosition, Event event, JavaSettings settings) throws SyntaxException, EvaluationException, Parser.EventResultException, SemanticException {
		InstanceParseResult compiledResult = doCompile(expression, eventPosition, event, settings);
		return new CompiledExpression() {
			@Override
			public Class<?> getResultType() {
				ObjectInfo evaluatedResult = compiledResult.getEvaluatedResult();
				return evaluatedResult.getDeclaredType();
			}

			@Override
			public Object evaluate(Object thisValue, Variables variables) throws EvaluationException {
				ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
				ObjectInfo resultInfo = compiledResult.evaluate(thisInfo, variables, EvaluationMode.DYNAMIC_TYPING);
				return resultInfo.getObject();
			}
		};
	}

	private static InstanceParseResult doCompile(String expression, int eventPosition, Event event, JavaSettings settings) throws SyntaxException, EvaluationException, Parser.EventResultException, SemanticException {
		Parser<JavaSettings> parser = new Parser<>(expression, eventPosition, event);
		return parser.parse(FULL_EXPRESSION, null, settings);
	}
}
