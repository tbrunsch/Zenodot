package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;

import java.util.Collection;

/**
 * Immutable settings for the parsing process. Can only be created with a {@link ParserSettingsBuilder}.<br>
 * <br>
 * You can either create a new builder or derive one from existing settings via {@link ParserSettings#builder()}.
 */
public interface ParserSettings
{
	CompletionMode getCompletionMode();
	Imports getImports();
	AccessModifier getMinimumAccessModifier();
	EvaluationMode getEvaluationMode();
	boolean isConsiderAllClassesForClassCompletions();
	Collection<String> getParserExtensionNames();
	/**
	 * Returns the parser extension registered for the {@code extensionName}.
	 * @throws IllegalArgumentException if no extension has been registered for the {@code extensionName}.
	 *                                  This can be tested by checking {@link #getParserExtensionNames()}
	 *                                  first.
	 */
	ParserExtension getParserExtension(String extensionName);
	ParserLogger getLogger();
	ParserSettingsBuilder builder();
}
