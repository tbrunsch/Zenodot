package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.impl.utils.ClassUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParserSettingsBuilderImpl implements ParserSettingsBuilder
{
	private CompletionMode								completionMode;
	private Set<Class<?>>								importedClasses;
	private Set<String>									importedPackages;
	private AccessModifier								minimumFieldAccessModifier;
	private AccessModifier								minimumMethodAccessModifier;
	private EvaluationMode								evaluationMode;
	private boolean										considerAllClassesForClassCompletions;
	private final Map<String, ParserExtension>			parserExtensions;
	private ParserLogger								logger;

	public ParserSettingsBuilderImpl() {
		completionMode = CompletionMode.COMPLETE_AND_REPLACE_WHOLE_WORDS;
		importedClasses = ImmutableSet.of();
		importedPackages = ImmutableSet.of();
		minimumFieldAccessModifier = AccessModifier.PUBLIC;
		minimumMethodAccessModifier = AccessModifier.PUBLIC;
		evaluationMode = EvaluationMode.MIXED;
		considerAllClassesForClassCompletions = false;
		parserExtensions = new HashMap<>();
		logger = ParserLoggers.createNullLogger();
	}

	ParserSettingsBuilderImpl(ParserSettings settings) {
		completionMode = settings.getCompletionMode();
		importedClasses = ImmutableSet.copyOf(settings.getImports().getImportedClasses());
		importedPackages = ImmutableSet.copyOf(settings.getImports().getImportedPackages());
		minimumFieldAccessModifier = settings.getMinimumFieldAccessModifier();
		minimumMethodAccessModifier = settings.getMinimumMethodAccessModifier();
		evaluationMode = settings.getEvaluationMode();
		considerAllClassesForClassCompletions = settings.isConsiderAllClassesForClassCompletions();
		parserExtensions = settings.getParserExtensionNames().stream()
			.collect(Collectors.toMap(
				Function.identity(),
				settings::getParserExtension)
			);
		logger = settings.getLogger();
	}

	@Override
	public ParserSettingsBuilder completionMode(CompletionMode completionMode) {
		this.completionMode = completionMode;
		return this;
	}

	@Override
	public ParserSettingsBuilder importClasses(Iterable<Class<?>> classes) {
		importedClasses = ImmutableSet.copyOf(classes);
		return this;
	}

	@Override
	public ParserSettingsBuilder importClassesByName(Iterable<String> classNames) throws ClassNotFoundException {
		ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
		for (String className : classNames) {
			String normalizedClassName = ClassUtils.normalizeClassName(className);
			Class<?> clazz = Class.forName(normalizedClassName);
			builder.add(clazz);
		}
		return importClasses(builder.build());
	}

	@Override
	public ParserSettingsBuilder importPackages(Iterable<String> packages) {
		importedPackages = ImmutableSet.copyOf(packages);
		return this;
	}

	@Override
	public ParserSettingsBuilder minimumFieldAccessModifier(AccessModifier minimumFieldAccessModifier) {
		this.minimumFieldAccessModifier = minimumFieldAccessModifier;
		return this;
	}

	@Override
	public ParserSettingsBuilder minimumMethodAccessModifier(AccessModifier minimumMethodAccessModifier) {
		this.minimumMethodAccessModifier = minimumMethodAccessModifier;
		return this;
	}

	@Override
	public ParserSettingsBuilder evaluationMode(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
		return this;
	}

	@Override
	public ParserSettingsBuilder considerAllClassesForClassCompletions(boolean considerAllClassesForClassCompletions) {
		this.considerAllClassesForClassCompletions = considerAllClassesForClassCompletions;
		return this;
	}

	@Override
	public ParserSettingsBuilder setParserExtension(String extensionName, @Nullable ParserExtension parserExtension) {
		if (parserExtension == null) {
			parserExtensions.remove(extensionName);
		} else {
			parserExtensions.put(extensionName, parserExtension);
		}
		return this;
	}

	@Override
	public ParserSettingsBuilder logger(ParserLogger logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		return new ParserSettingsImpl(completionMode, importedClasses, importedPackages, minimumFieldAccessModifier, minimumMethodAccessModifier, evaluationMode, considerAllClassesForClassCompletions, parserExtensions, logger);
	}
}
