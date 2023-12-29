package dd.kms.zenodot.impl.settings.extensions;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.settings.extensions.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.extensions.CompletionProvider;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ParserExtensionImpl implements ParserExtension
{
	private final List<AdditionalParserSettings>		additionalParserSettings;
	private final List<StringLiteralCompletionEntry>	stringLiteralCompletionProviders;

	ParserExtensionImpl(List<AdditionalParserSettings> additionalParserSettings, List<StringLiteralCompletionEntry> stringLiteralCompletionProviders) {
		this.additionalParserSettings = ImmutableList.copyOf(additionalParserSettings);
		this.stringLiteralCompletionProviders = ImmutableList.copyOf(stringLiteralCompletionProviders);
	}

	@Override
	public List<AdditionalParserSettings> getParsers() {
		return additionalParserSettings;
	}

	@Override
	public List<CompletionProvider> getStringLiteralCompletionProviders(Executable executable, int parameterIndex) {
		List<CompletionProvider> completionProviders = new ArrayList<>();
		for (StringLiteralCompletionEntry entry : stringLiteralCompletionProviders) {
			if (parameterIndex != entry.getParameterIndex()) {
				continue;
			}
			Executable supportedExecutable = entry.getExecutable();
			if (!Objects.equals(executable, supportedExecutable) && !ReflectionUtils.isOverriddenBy(supportedExecutable, executable)) {
				continue;
			}
			completionProviders.add(entry.getCompletionProvider());
		}
		return completionProviders;
	}
}
