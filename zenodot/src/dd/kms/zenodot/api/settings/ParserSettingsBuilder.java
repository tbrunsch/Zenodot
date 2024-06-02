package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;
import dd.kms.zenodot.api.settings.extensions.ParserExtensionBuilder;

import javax.annotation.Nullable;

/**
 * Builder for {@link ParserSettings}<br>
 * <br>
 * You can either create a new builder via {@link #create()} or derive
 * one from existing settings via {@link ParserSettings#builder()}.
 */
public interface ParserSettingsBuilder
{
	static ParserSettingsBuilder create() {
		return new dd.kms.zenodot.impl.settings.ParserSettingsBuilderImpl();
	}

	/**
	 * Configure which text should be completed and which text should be overwritten by
	 * the selected completion.
	 */
	ParserSettingsBuilder completionMode(CompletionMode completionMode);

	/**
	 * When you import a class, then you can directly reference that class by its simple name.
	 */
	ParserSettingsBuilder importClasses(Iterable<Class<?>> classes);

	/**
	 * see {@link #importClasses(Iterable)}
	 */
	ParserSettingsBuilder importClassesByName(Iterable<String> classNames) throws ClassNotFoundException;

	/**
	 * When you import a package, then you can directly reference any of its classes by their simple names.
	 */
	ParserSettingsBuilder importPackages(Iterable<String> packageNames);

	/**
	 * The minimum access modifier affects which fields are suggested for code completion and are accepted
	 * when evaluating expressions. When setting this to {@link AccessModifier#PRIVATE}, then all fields
	 * will be considered.
	 */
	ParserSettingsBuilder minimumFieldAccessModifier(AccessModifier minimumAccessModifier);

	/**
	 * The minimum method access modifier affects which methods and constructors are suggested for
	 * code completion and are accepted when evaluating expressions. When setting this to
	 * {@link AccessModifier#PRIVATE}, then all methods will be considered.
	 */
	ParserSettingsBuilder minimumMethodAccessModifier(AccessModifier minimumAccessModifier);

	/**
	 * Specify how expressions are evaluated, also during code completion. See the different values of
	 * {@link EvaluationMode} for more details.
	 */
	ParserSettingsBuilder evaluationMode(EvaluationMode evaluationMode);

	/**
	 * By default, when typing an unqualified class name, only classes you have imported or whose package is
	 * imported will be considered as code completions. With enabling this feature, also classes from other
	 * packages will be suggested, but fully qualified.<br>
	 * <br>
	 * Consider the partial class name "Lis". If you have not imported "java.util", then you will get the
	 * code completion "java.util.List" if this feature is enabled. Otherwise, that class will not be suggested.
	 */
	ParserSettingsBuilder considerAllClassesForClassCompletions(boolean considerAllClassesForClassCompletions);

	/**
	 * Updates the extension with the specified {@code extensionName}. If the specified {@code parserExtension} is
	 * {@code null}, then the extension is removed. Extensions are created via the {@link ParserExtensionBuilder}.
	 */
	ParserSettingsBuilder setParserExtension(String extensionName, @Nullable ParserExtension parserExtension);

	/**
	 * Specify a logger that receives messages during the parsing process. This is primarily meant for
	 * debugging purposes.
	 */
	ParserSettingsBuilder logger(ParserLogger logger);

	ParserSettings build();
}
