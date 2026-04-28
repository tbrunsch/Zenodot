package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

abstract class AbstractPackageRule<C> extends AbstractRule<C, String, JavaSettings> implements SimpleRule<C, String, JavaSettings>
{
	protected abstract String getParentPackageName(C input);

	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<C, String, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<C, String>() {
			@Override
			protected void doSuggestCodeCompletions(C input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(C input, JavaSettings settings) {
				// TODO
			}

			@Override
			public String evaluate(C input, String subpackageName, JavaSettings settings) throws SemanticException {
				String parentPackageName = getParentPackageName(input);
				String packageName = parentPackageName != null ? parentPackageName + "." + subpackageName : subpackageName;
				if (!ClassDataProvider.packageExists(packageName)) {
					throw new SemanticException("Package \"" + packageName + "\" does not exist");
				}
				return packageName;
			}
		};
	}
}
