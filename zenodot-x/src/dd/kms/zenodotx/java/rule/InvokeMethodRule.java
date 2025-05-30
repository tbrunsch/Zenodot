package dd.kms.zenodotx.java.rule;

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

				// TODO

				// ExecutableDataProvider executableDataProvider = new ExecutableDataProvider(settings);
				// executableDataProvider.getExecutableToInvoke(methodInfos, parameterInfos);
				return null;
			}
		};
	}
}
