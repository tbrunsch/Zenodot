package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;
import dd.kms.zenodot.framework.wrappers.ExecutableInfo;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.List;

public abstract class AbstractMethodNameRule<C> extends AbstractRule<C, ExecutableParseInfo, JavaSettings> implements SimpleRule<C, ExecutableParseInfo, JavaSettings>
{
	protected abstract Class<?> getContextType(C context, JavaSettings settings);
	protected abstract InstanceParseResult getContextInstanceEvaluation(C context, JavaSettings settings);
	protected abstract boolean isContextStatic();

	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<C, ExecutableParseInfo, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<C, ExecutableParseInfo>() {

			@Override
			protected void doSuggestCodeCompletions(C input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(C input, JavaSettings settings) {
				// TODO
			}

			@Override
			public ExecutableParseInfo evaluate(C context, String methodName, JavaSettings settings) throws SemanticException, EvaluationException {
				Class<?> contextType = getContextType(context, settings);
				InstanceParseResult contextParseResult = getContextInstanceEvaluation(context, settings);
				AccessModifier minimumMethodAccessModifier = settings.getMinimumMethodAccessModifier();
				MethodScanner methodScanner = getMethodScanner(methodName, minimumMethodAccessModifier);
				List<ExecutableInfo> methodInfos = InfoProvider.getMethodInfos(contextType, methodScanner);
				if (methodInfos.isEmpty()) {
					methodScanner = getMethodScanner(methodName, AccessModifier.PRIVATE);
					methodInfos = InfoProvider.getMethodInfos(contextType, methodScanner);
					throw methodInfos.isEmpty()
						? new SemanticException("Unknown method '" + methodName + "'")
						: new SemanticException("Method '" + methodName + "' is not visible");
				}
				return new ExecutableParseInfo(methodInfos, contextParseResult);
			}

			private MethodScanner getMethodScanner(String name, AccessModifier minimumAccessModifier) {
				MethodScannerBuilder methodScannerBuilder = getMethodScannerBuilder(minimumAccessModifier).name(name);
				return methodScannerBuilder.build();
			}

			private MethodScannerBuilder getMethodScannerBuilder(AccessModifier minimumAccessModifier) {
				StaticMode staticMode = isContextStatic() ? StaticMode.STATIC : StaticMode.BOTH;
				return MethodScannerBuilder.create()
					.staticMode(staticMode)
					.minimumAccessModifier(minimumAccessModifier);
			}
		};
	}
}
