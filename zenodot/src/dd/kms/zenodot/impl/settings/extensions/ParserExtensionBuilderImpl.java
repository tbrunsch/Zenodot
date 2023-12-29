package dd.kms.zenodot.impl.settings.extensions;

import dd.kms.zenodot.api.settings.extensions.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.extensions.CompletionProvider;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;
import dd.kms.zenodot.api.settings.extensions.ParserExtensionBuilder;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

public class ParserExtensionBuilderImpl implements ParserExtensionBuilder
{
	private final List<AdditionalParserSettings>		additionalParserSettings			= new ArrayList<>();
	private final List<StringLiteralCompletionEntry>	stringLiteralCompletionProviders	= new ArrayList<>();

	@Override
	public ParserExtensionBuilder addParser(AdditionalParserSettings additionalParserSettings) {
		this.additionalParserSettings.add(additionalParserSettings);
		return this;
	}

	@Override
	public ParserExtensionBuilder addStringLiteralCompletionProvider(Executable executable, int parameterIndex, CompletionProvider completionProvider) {
		StringLiteralCompletionEntry stringLiteralCompletionProvider = new StringLiteralCompletionEntry(executable, parameterIndex, completionProvider);
		this.stringLiteralCompletionProviders.add(stringLiteralCompletionProvider);
		return this;
	}

	@Override
	public ParserExtension build() {
		return new ParserExtensionImpl(additionalParserSettings, stringLiteralCompletionProviders);
	}
}
