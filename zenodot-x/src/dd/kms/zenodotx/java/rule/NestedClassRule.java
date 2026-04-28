package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

public class NestedClassRule extends AbstractRule<Class<?>, Class<?>, JavaSettings> implements SimpleRule<Class<?>, Class<?>, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<Class<?>, Class<?>, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Class<?>, Class<?>>() {
			@Override
			protected void doSuggestCodeCompletions(Class<?> input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(Class<?> input, JavaSettings settings) {
				// TODO
			}

			@Override
			public Class<?> evaluate(Class<?> parentClass, String nestedClassName, JavaSettings settings) throws SemanticException, EvaluationException {
				Class<?>[] nestedClasses = parentClass.getDeclaredClasses();
				for (Class<?> nestedClass : nestedClasses) {
					if (nestedClassName.equals(nestedClass.getSimpleName())) {
						return nestedClass;
					}
				}
				throw new SemanticException("Unknown nested class \"" + nestedClassName + "\" of class \"" + parentClass.getName() + "\"");
			}
		};
	}
}
