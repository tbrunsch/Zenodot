package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.*;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.FieldInfo;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.List;

public abstract class AbstractFieldRule<C> extends AbstractRule<C, InstanceParseResult, JavaSettings> implements SimpleRule<C, InstanceParseResult, JavaSettings>
{
	protected abstract Class<?> getContextType(C context, JavaSettings settings) throws EvaluationException;
	protected abstract InstanceParseResult getContextInstanceEvaluation(C context, JavaSettings settings);
	protected abstract boolean isContextStatic();

	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<C, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<C, InstanceParseResult>() {
			@Override
			protected void doSuggestCodeCompletions(C context, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(C context, JavaSettings settings) {
				// TODO
			}

			@Override
			public InstanceParseResult evaluate(C context, String fieldName, JavaSettings settings) throws SemanticException, EvaluationException {
				Class<?> contextType = getContextType(context, settings);
				InstanceParseResult contextParseResult = getContextInstanceEvaluation(context, settings);
				AccessModifier minimumFieldAccessModifier = settings.getMinimumFieldAccessModifier();
				FieldScanner fieldScanner = getFieldScanner(fieldName, minimumFieldAccessModifier);
				List<FieldInfo> fieldInfos = InfoProvider.getFieldInfos(contextType, fieldScanner);
				if (fieldInfos.isEmpty()) {
					fieldScanner = getFieldScanner(fieldName, AccessModifier.PRIVATE);
					fieldInfos = InfoProvider.getFieldInfos(contextType, fieldScanner);
					throw fieldInfos.isEmpty()
						? new SemanticException("Unknown field '" + fieldName + "'")
						: new SemanticException("Field '" + fieldName + "' is not visible");
				} else if (fieldInfos.size() > 1) {
					throw new SemanticException("Ambiguous field name '" + fieldName + "'");
				}

				FieldInfo fieldInfo = fieldInfos.get(0);
				return new FieldParseResult(contextParseResult, fieldInfo, settings.getEvaluationMode());
			}

			private FieldScanner getFieldScanner(String name, AccessModifier minimumAccessModifier) {
				FieldScannerBuilder builder = getFieldScannerBuilder(minimumAccessModifier).name(name);
				return builder.build();
			}

			private FieldScannerBuilder getFieldScannerBuilder(AccessModifier minimumAccessModifier) {
				StaticMode staticMode = isContextStatic() ? StaticMode.STATIC : StaticMode.BOTH;
				return FieldScannerBuilder.create()
					.staticMode(staticMode)
					.minimumAccessModifier(minimumAccessModifier);
			}
		};
	}

	private static class FieldParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	contextParseResult;
		private final FieldInfo				fieldInfo;
		private final ObjectInfo			evaluatedResult;

		FieldParseResult(InstanceParseResult contextParseResult, FieldInfo fieldInfo, EvaluationMode evaluationMode) throws EvaluationException {
			this.contextParseResult = contextParseResult;
			this.fieldInfo = fieldInfo;
			this.evaluatedResult = evaluate(contextParseResult.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo context = contextParseResult.evaluate(thisInfo, variables, evaluationMode);
			return evaluate(context, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo context, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			try {
				return objectInfoProvider.getFieldValueInfo(context.getObject(), fieldInfo);
			} catch (AccessDeniedException e) {
				throw new EvaluationException("Cannot access field '" + fieldInfo.getName() + "': " + e.getMessage());
			}
		}
	}
}
