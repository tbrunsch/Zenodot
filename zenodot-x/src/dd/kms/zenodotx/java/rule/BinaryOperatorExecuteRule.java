package dd.kms.zenodotx.java.rule;

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

public class BinaryOperatorExecuteRule extends AbstractRule<InstanceParseResult, InstanceParseResult, JavaSettings> implements CompoundRule<InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private final Rule<Void, String, JavaSettings>				operatorRule;
	private final Rule<Void, InstanceParseResult, JavaSettings>	rightHandSideRule;

	public BinaryOperatorExecuteRule(Rule<Void, String, JavaSettings> operatorRule, Rule<Void, InstanceParseResult, JavaSettings> rightHandSideRule) {
		this.operatorRule = operatorRule;
		this.rightHandSideRule = rightHandSideRule;
	}


	@Override
	public InstanceParseResult parse(InstanceParseResult lhs, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		String operator = parser.parse(operatorRule, null, settings);
		boolean shortCircuitEvaluation = isApplyShortCircuitEvaluation(lhs.getEvaluatedResult(), operator);
		JavaSettings rhsSettings = getRightHandSideSettings(settings, shortCircuitEvaluation);
		InstanceParseResult rhs = parser.parse(rightHandSideRule, null, rhsSettings);
		return new BinaryOperatorInstanceParseResult(lhs, rhs, operator, settings);
	}

	private static boolean isApplyShortCircuitEvaluation(ObjectInfo lhsInfo, String operator) {
		// TODO
		return false;
	}

	private static JavaSettings getRightHandSideSettings(JavaSettings settings, boolean shortCircuitEvaluation) {
		return shortCircuitEvaluation
				? settings.withoutEvaluation()
				: settings;
	}

	private static class BinaryOperatorInstanceParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	lhs;
		private final InstanceParseResult	rhs;
		private final String				operator;
		private final ObjectInfo			evaluatedResult;

		private BinaryOperatorInstanceParseResult(InstanceParseResult lhs, InstanceParseResult rhs, String operator, JavaSettings rhsSettings) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.operator = operator;
			this.evaluatedResult = evaluate(lhs.getEvaluatedResult(), rhs.getEvaluatedResult(), rhsSettings);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(JavaSettings settings) throws EvaluationException {
			ObjectInfo lhsInfo = lhs.evaluate(settings);
			boolean shortCircuitEvaluation = isApplyShortCircuitEvaluation(lhsInfo, operator);
			JavaSettings rhsSettings = getRightHandSideSettings(settings, shortCircuitEvaluation);
			ObjectInfo rhsInfo = rhs.evaluate(rhsSettings);
			return evaluate(lhsInfo, rhsInfo, settings);
		}

		private ObjectInfo evaluate(ObjectInfo lhsInfo, ObjectInfo rhsInfo, JavaSettings settings) {
			// TODO: Evaluate operator (which should not be a String anymore, but some functor)
			return null;
		}
	}
}
