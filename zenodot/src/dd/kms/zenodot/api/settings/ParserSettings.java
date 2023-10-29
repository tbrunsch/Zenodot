package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;

import java.lang.reflect.Executable;
import java.util.List;

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
	List<AdditionalParserSettings> getAdditionalParserSettings();
	List<CompletionProvider> getStringLiteralCompletionProviders(Executable executable, int parameterIndex);
	ParserLogger getLogger();
	ParserSettingsBuilder builder();
}
