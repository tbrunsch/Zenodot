package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class ClassCastRule extends AbstractRule<Class<?>, InstanceParseResult, JavaSettings> implements CompoundRule<Class<?>, InstanceParseResult, JavaSettings>
{
	private final Rule<Void, InstanceParseResult, JavaSettings> simpleExpressionRule;

	public ClassCastRule(Rule<Void, InstanceParseResult, JavaSettings> simpleExpressionRule) {
		this.simpleExpressionRule = simpleExpressionRule;
	}

	@Override
	public InstanceParseResult parse(Class<?> targetClass, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		InstanceParseResult expression = parser.parse(simpleExpressionRule, null, settings);
		return new CastParseResult(targetClass, expression, settings.getEvaluationMode());
	}

	private static class CastParseResult implements InstanceParseResult
	{
		private final Class<?>				targetClass;
		private final InstanceParseResult	expression;
		private final ObjectInfo			evaluatedObject;

		private CastParseResult(Class<?> targetClass, InstanceParseResult expression, EvaluationMode evaluationMode) throws EvaluationException {
			this.targetClass = targetClass;
			this.expression = expression;
			this.evaluatedObject = evaluate(expression.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedObject;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo expressionInfo = this.expression.evaluate(thisInfo, variables, evaluationMode);
			return evaluate(expressionInfo, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo expressionInfo, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			try {
				return objectInfoProvider.getCastInfo(expressionInfo, targetClass);
			} catch (ClassCastException e) {
				throw new EvaluationException("Cannot cast expression to '" + targetClass + "'", e);
			}
		}
	}
}
