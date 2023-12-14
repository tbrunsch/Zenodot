package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;

import javax.annotation.Nullable;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final CompletionMode						completionMode;
	private final Imports								imports;
	private final AccessModifier						minimumAccessModifier;
	private final EvaluationMode						evaluationMode;
	private final boolean 								considerAllClassesForClassCompletions;
	private final List<AdditionalParserSettings>		additionalParserSettings;
	private final List<StringLiteralCompletionEntry>	stringLiteralCompletionProviders;
	private final ParserLogger 							logger;

	ParserSettingsImpl(CompletionMode completionMode, Set<Class<?>> importedClasses, Set<String> importedPackages, AccessModifier minimumAccessModifier, EvaluationMode evaluationMode, boolean considerAllClassesForClassCompletions, List<AdditionalParserSettings> additionalParserSettings, List<StringLiteralCompletionEntry> stringLiteralCompletionProviders, ParserLogger logger) {
		this.completionMode = completionMode;
		this.imports = new ImportsImpl(importedClasses, importedPackages);
		this.minimumAccessModifier = minimumAccessModifier;
		this.evaluationMode = evaluationMode;
		this.considerAllClassesForClassCompletions = considerAllClassesForClassCompletions;
		this.additionalParserSettings = ImmutableList.copyOf(additionalParserSettings);
		this.stringLiteralCompletionProviders = ImmutableList.copyOf(stringLiteralCompletionProviders);
		this.logger = logger;
	}

	@Override
	public CompletionMode getCompletionMode() {
		return completionMode;
	}

	@Override
	public Imports getImports() {
		return imports;
	}

	@Override
	public AccessModifier getMinimumAccessModifier() {
		return minimumAccessModifier;
	}

	@Override
	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	@Override
	public boolean isConsiderAllClassesForClassCompletions() {
		return considerAllClassesForClassCompletions;
	}

	@Override
	@Nullable
	public List<AdditionalParserSettings> getAdditionalParserSettings() {
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

	List<StringLiteralCompletionEntry> getStringLiteralCompletionProviders() {
		return stringLiteralCompletionProviders;
	}

	@Override
	public ParserLogger getLogger() {
		return logger;
	}

	@Override
	public ParserSettingsBuilder builder() {
		return new ParserSettingsBuilderImpl(this);
	}
}
