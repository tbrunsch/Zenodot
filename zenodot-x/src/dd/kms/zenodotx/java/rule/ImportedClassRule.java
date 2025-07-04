package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.Imports;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.Collections;
import java.util.Set;

public class ImportedClassRule extends AbstractRule<Void, Class<?>, JavaSettings> implements SimpleRule<Void, Class<?>, JavaSettings>
{
	public ImportedClassRule() {
		name("Imported or primitive class");
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<Void, Class<?>, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, Class<?>>() {
			@Override
			void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			void doSuggestMethodParameters(Void input, JavaSettings settings) {
				// TODO
			}

			@Override
			public Class<?> evaluate(Void input, String className, JavaSettings settings) throws SemanticException, EvaluationException {
				ClassDataProvider classDataProvider = getClassDataProvider(settings);
				Class<?> importedClass = classDataProvider.getImportedClass(className);
				if (importedClass == null) {
					throw new SemanticException("\"" + className + "\" is no imported class");
				}
				return importedClass;
			}

			private ClassDataProvider getClassDataProvider(JavaSettings settings) {
				// TODO: Consider imports
				Imports imports = new Imports() {
					@Override
					public Set<Class<?>> getImportedClasses() {
						return Collections.emptySet();
					}

					@Override
					public Set<String> getImportedPackages() {
						return Collections.emptySet();
					}
				};

				// TODO: Can't we simply write thisInfo.getDeclaredType()?
				EvaluationMode evaluationMode = settings.getEvaluationMode();
				ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
				ObjectInfo thisInfo = settings.getThisInfo();
				Class<?> thisClass = objectInfoProvider.getType(thisInfo);

				return new ClassDataProvider(imports, thisClass);
			}
		};
	}
}
