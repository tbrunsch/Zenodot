package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

public class AccessArrayElementRule extends AbstractRule<ArrayAccessInfo, InstanceParseResult, JavaSettings> implements SimpleRule<ArrayAccessInfo, InstanceParseResult, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<ArrayAccessInfo, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<ArrayAccessInfo, InstanceParseResult>() {
			@Override
			void doSuggestCodeCompletions(ArrayAccessInfo input, String parsedString, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			void doSuggestMethodParameters(ArrayAccessInfo input, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			public InstanceParseResult evaluate(ArrayAccessInfo arrayAccessInfo, String parsedString, JavaSettings settings) {
				InstanceParseResult array = arrayAccessInfo.getArray();
				InstanceParseResult index = arrayAccessInfo.getIndex();
				return new ArrayElementAccessParseResult(array, index, settings.getEvaluationMode());
			}
		};
	}

	@Override
	protected String getGenericName() {
		return "evaluate array[index]";
	}

	private static class ArrayElementAccessParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	arrayParseResult;
		private final InstanceParseResult	indexParseResult;
		private final ObjectInfo			evaluatedResult;

		ArrayElementAccessParseResult(InstanceParseResult arrayParseResult, InstanceParseResult indexParseResult, EvaluationMode evaluationMode) {
			this.arrayParseResult = arrayParseResult;
			this.indexParseResult = indexParseResult;
			this.evaluatedResult = evaluate(arrayParseResult.getEvaluatedResult(), indexParseResult.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo arrayInfo = arrayParseResult.evaluate(thisInfo, variables, evaluationMode);
			ObjectInfo indexInfo = indexParseResult.evaluate(thisInfo, variables, evaluationMode);
			return evaluate(arrayInfo, indexInfo, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo arrayInfo, ObjectInfo indexInfo, EvaluationMode evaluationMode) {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			return objectInfoProvider.getArrayElementInfo(arrayInfo, indexInfo);
		}
	}
}
