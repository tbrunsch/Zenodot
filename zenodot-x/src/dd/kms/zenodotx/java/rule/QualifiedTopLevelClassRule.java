package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.impl.utils.ClassUtils;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

public class QualifiedTopLevelClassRule extends AbstractRule<String, Class<?>, JavaSettings> implements SimpleRule<String, Class<?>, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<String, Class<?>, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<String, Class<?>>() {
			@Override
			protected void doSuggestCodeCompletions(String input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(String input, JavaSettings settings) {
				// TODO
			}

			@Override
			public Class<?> evaluate(String packageName, String className, JavaSettings settings) throws SemanticException {
				String qualifiedClassName = packageName + "." + className;
				Class<?> clazz = ClassUtils.getClassUnchecked(qualifiedClassName);
				if (clazz == null) {
					throw new SemanticException("Unknown class '" + qualifiedClassName + "'");
				}
				return clazz;
			}
		};
	}
}
