package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;

import java.lang.reflect.Executable;

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
	 * The minimum access modifier affects which fields and methods are suggested for code completion and
	 * are accepted when evaluating expressions. When setting this to {@link AccessModifier#PRIVATE}, then
	 * all fields and methods will be considered.
	 */
	ParserSettingsBuilder minimumAccessModifier(AccessModifier minimumAccessModifier);

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
	 * Call this method if you want to add custom parsers to the Zenodot parser framework.
	 */
	ParserSettingsBuilder additionalParserSettings(AdditionalParserSettings additionalParserSettings);

	/**
	 * Adds a completion provider for the String parameter {@code parameterIndex} of {@code executable}. The
	 * completion provider is associated with the specified {@code owner}. All completion providers of
	 * {@code owner} can later be removed by calling {@link #removeStringLiteralCompletionProviders(Object)}.
	 */
	ParserSettingsBuilder addStringLiteralCompletionProvider(Object owner, Executable executable, int parameterIndex, CompletionProvider completionProvider);

	/**
	 * Removes all completion providers registered via {@link #addStringLiteralCompletionProvider(Object, Executable, int, CompletionProvider)}
	 * for the specified {@code owner}.
	 */
	ParserSettingsBuilder removeStringLiteralCompletionProviders(Object owner);

	/**
	 * Specify a logger that receives messages during the parsing process. This is primarily meant for
	 * debugging purposes.
	 */
	ParserSettingsBuilder logger(ParserLogger logger);

	ParserSettings build();
}
