package dd.kms.zenodotx.java.rule;

import com.google.common.primitives.Primitives;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class ConditionalOperatorExecuteRule extends AbstractRule<InstanceParseResult, InstanceParseResult, JavaSettings> implements CompoundRule<InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private static final Rule<Void, Void, JavaSettings>	OPERATOR_PART_1 = Rules.character('?');
	private static final Rule<Void, Void, JavaSettings>	OPERATOR_PART_2 = Rules.character(':');

	private final Rule<Void, InstanceParseResult, JavaSettings> partExpressionRule;

	public ConditionalOperatorExecuteRule(Rule<Void, InstanceParseResult, JavaSettings> partExpressionRule) {
		this.partExpressionRule = partExpressionRule;
	}

	@Override
	public InstanceParseResult parse(InstanceParseResult condition, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		ObjectInfo conditionInfo = condition.getEvaluatedResult();
		Class<?> type = objectInfoProvider.getType(conditionInfo);
		TypeMatch typeMatch = MatchRatings.rateTypeMatch(boolean.class, type);
		if (typeMatch == TypeMatch.NONE) {
			throw new SemanticException("Expected Boolean expression for the condition of the conditional operator");
		}
		Object conditionValue = conditionInfo.getObject();
		PartToEvaluate partToEvaluate = getPartToEvaluate(conditionValue);
		JavaSettings settingsWithoutEvaluation = settings.withoutEvaluation();

		parser.parse(OPERATOR_PART_1, null, settings);

		InstanceParseResult part1 = parser.parse(partExpressionRule, null, partToEvaluate == PartToEvaluate.FIRST ? settings : settingsWithoutEvaluation);

		parser.parse(OPERATOR_PART_2, null, settings);

		InstanceParseResult part2 = parser.parse(partExpressionRule, null, partToEvaluate == PartToEvaluate.SECOND ? settings : settingsWithoutEvaluation);

		return new ConditionalOperatorParseResult(condition, part1, part2, partToEvaluate, settings.getEvaluationMode());
	}

	private static PartToEvaluate getPartToEvaluate(Object conditionValue) {
		return	Boolean.TRUE.equals(conditionValue)		? PartToEvaluate.FIRST :
				Boolean.FALSE.equals(conditionValue)	? PartToEvaluate.SECOND
														: PartToEvaluate.NONE;
	}

	private static class ConditionalOperatorParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	condition;
		private final InstanceParseResult	part1;
		private final InstanceParseResult	part2;
		private final ObjectInfo			evaluatedResult;

		private ConditionalOperatorParseResult(InstanceParseResult condition, InstanceParseResult part1, InstanceParseResult part2, PartToEvaluate partToEvaluate, EvaluationMode evaluationMode) {
			this.condition = condition;
			this.part1 = part1;
			this.part2 = part2;

			ObjectInfo part1Info = part1.getEvaluatedResult();
			ObjectInfo part2Info = part2.getEvaluatedResult();
			Object evaluatedResultValue =	partToEvaluate == PartToEvaluate.FIRST	? part1Info.getObject() :
											partToEvaluate == PartToEvaluate.SECOND	? part2Info.getObject()
																					: InfoProvider.INDETERMINATE_VALUE;
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			Class<?> evaluatedType = getCommonClass(objectInfoProvider.getType(part1Info), objectInfoProvider.getType(part2Info));
			this.evaluatedResult = InfoProvider.createObjectInfo(evaluatedResultValue, evaluatedType);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			ObjectInfo conditionInfo = condition.evaluate(thisInfo, variables, evaluationMode);
			Class<?> type = objectInfoProvider.getType(conditionInfo);
			TypeMatch typeMatch = MatchRatings.rateTypeMatch(boolean.class, type);
			if (typeMatch == TypeMatch.NONE) {
				throw new EvaluationException("The result of the condition of the conditional expression is not of type boolean");
			}
			Object conditionValue = conditionInfo.getObject();
			PartToEvaluate partToEvaluate = getPartToEvaluate(conditionValue);
			switch (partToEvaluate) {
				case FIRST:
					return part1.evaluate(thisInfo, variables, evaluationMode);
				case SECOND:
					return part2.evaluate(thisInfo, variables, evaluationMode);
				case NONE:
					throw new IllegalStateException("Internal error: The result of the condition of the conditional expression is neither \"true\" nor \"false\"");
				default:
					throw new IllegalStateException("Internal error: Unexpected part to evaluate: " + partToEvaluate);
			}
		}

		private static Class<?> getCommonClass(Class<?> class1, Class<?> class2) {
			try {
				return ReflectionUtils.getCommonPrimitiveClass(class1, class2);
			} catch (IllegalArgumentException e) {
				/* fallthrough */
			}
			return ReflectionUtils.getCommonSuperClass(Primitives.wrap(class1), Primitives.wrap(class2));
		}
	}

	private enum PartToEvaluate {FIRST, SECOND, NONE}
}
