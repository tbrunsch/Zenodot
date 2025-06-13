package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ExecutableInfo;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InvokeMethodRule extends AbstractRule<ExecutableParseInfo, InstanceParseResult, JavaSettings> implements SimpleRule<ExecutableParseInfo, InstanceParseResult, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<ExecutableParseInfo, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<ExecutableParseInfo, InstanceParseResult>() {
			@Override
			void doSuggestCodeCompletions(ExecutableParseInfo input, String parsedString, JavaSettings settings) {
				/* nothing to do */
				return;
			}

			@Override
			void doSuggestMethodParameters(ExecutableParseInfo input, JavaSettings settings) {
				/* nothing to do */
				return;
			}

			@Override
			public InstanceParseResult evaluate(ExecutableParseInfo methodParseInfo, String parsedString, JavaSettings settings) throws SemanticException, EvaluationException {
				List<ExecutableInfo> methodInfos = methodParseInfo.getExecutableInfos();
				List<InstanceParseResult> parameters = methodParseInfo.getParameters();
				List<ObjectInfo> parameterInfos = parameters.stream().map(InstanceParseResult::getEvaluatedResult).collect(Collectors.toList());
				ExecutableDataProvider executableDataProvider = new ExecutableDataProvider(settings);
				ExecutableInfo executable = executableDataProvider.getExecutableToInvoke(methodInfos, parameterInfos);
				return new MethodParseResult(methodParseInfo.getContext(), executable, parameters, settings.getEvaluationMode());
			}
		};
	}

	@Override
	protected String getGenericName() {
		return "invoke method";
	}

	private static class MethodParseResult implements InstanceParseResult
	{
		private final InstanceParseResult		contextParseResult;
		private final ExecutableInfo			executableInfo;
		private final List<InstanceParseResult>	parameterParseResults;
		private final ObjectInfo				evaluatedResult;

		MethodParseResult(InstanceParseResult contextParseResult, ExecutableInfo executableInfo, List<InstanceParseResult> parameterParseResults, EvaluationMode evaluationMode) throws EvaluationException {
			this.contextParseResult = contextParseResult;
			this.executableInfo = executableInfo;
			this.parameterParseResults = parameterParseResults;
			this.evaluatedResult = evaluate(
				contextParseResult.getEvaluatedResult(),
				parameterParseResults.stream()
					.map(InstanceParseResult::getEvaluatedResult)
					.collect(Collectors.toList()),
				evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo context = contextParseResult.evaluate(thisInfo, variables, evaluationMode);
			List<ObjectInfo> parameters = new ArrayList<>();
			for (InstanceParseResult parameterParseResult : parameterParseResults) {
				ObjectInfo parameter = parameterParseResult.evaluate(thisInfo, variables, evaluationMode);
				parameters.add(parameter);
			}
			return evaluate(context, parameters, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo context, List<ObjectInfo> parameters, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			try {
				return objectInfoProvider.getExecutableReturnInfo(context.getObject(), executableInfo, parameters);
			} catch (ReflectiveOperationException e) {
				throw new EvaluationException("An exception occurred when executing method '" + executableInfo.getName() + "()': " + e.getMessage(), e);
			}
		}
	}
}
