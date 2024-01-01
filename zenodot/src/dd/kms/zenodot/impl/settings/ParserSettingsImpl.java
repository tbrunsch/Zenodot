package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;
import dd.kms.zenodot.api.settings.extensions.ExtensionMethodDescription;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final CompletionMode						completionMode;
	private final Imports								imports;
	private final AccessModifier						minimumAccessModifier;
	private final EvaluationMode						evaluationMode;
	private final boolean 								considerAllClassesForClassCompletions;
	private final Map<String, ParserExtension>			parserExtensions;
	private final List<ExtensionMethodDescription>		extensionMethodDescriptions;
	private final ParserLogger 							logger;

	ParserSettingsImpl(CompletionMode completionMode, Set<Class<?>> importedClasses, Set<String> importedPackages, AccessModifier minimumAccessModifier, EvaluationMode evaluationMode, boolean considerAllClassesForClassCompletions, Map<String, ParserExtension> parserExtensions, List<ExtensionMethodDescription> extensionMethodDescriptions, ParserLogger logger) {
		this.completionMode = completionMode;
		this.imports = new ImportsImpl(importedClasses, importedPackages);
		this.minimumAccessModifier = minimumAccessModifier;
		this.evaluationMode = evaluationMode;
		this.considerAllClassesForClassCompletions = considerAllClassesForClassCompletions;
		this.parserExtensions = ImmutableMap.copyOf(parserExtensions);
		this.extensionMethodDescriptions = extensionMethodDescriptions;
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
	public Collection<String> getParserExtensionNames() {
		return parserExtensions.keySet();
	}

	@Nullable
	@Override
	public ParserExtension getParserExtension(String extensionName) {
		return parserExtensions.get(extensionName);
	}

	@Override
	public List<ExtensionMethodDescription> getExtensionMethodDescriptions() {
		return extensionMethodDescriptions;
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
