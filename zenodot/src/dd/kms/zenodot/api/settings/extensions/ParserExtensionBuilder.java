package dd.kms.zenodot.api.settings.extensions;

import java.lang.reflect.Executable;

public interface ParserExtensionBuilder
{
	static ParserExtensionBuilder create() {
		return new dd.kms.zenodot.impl.settings.extensions.ParserExtensionBuilderImpl();
	}

	/**
	 * Adds a new parser that will be considered by Zenodot when parsing expressions.
	 */
	ParserExtensionBuilder addParser(AdditionalParserSettings additionalParserSettings);

	/**
	 * Adds a {@link CompletionProvider} for the parameter of {@code executable} with index {@code parameterIndex}.
	 * The behavior of this method is currently undefined if that parameter is not of type {@code String}.
	 */
	ParserExtensionBuilder addStringLiteralCompletionProvider(Executable executable, int parameterIndex, CompletionProvider completionProvider);
	ParserExtension build();
}
