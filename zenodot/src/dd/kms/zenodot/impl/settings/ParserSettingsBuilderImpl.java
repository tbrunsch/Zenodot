package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.impl.utils.ClassUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ParserSettingsBuilderImpl implements ParserSettingsBuilder
{
	private CompletionMode		completionMode;
	private List<String>		innerClassNames;
	private Set<Class<?>>		importedClasses;
	private Set<String>			importedPackages;
	private AccessModifier		minimumAccessModifier;
	private EvaluationMode		evaluationMode;
	private boolean				considerAllClassesForClassCompletions;
	private ObjectTreeNode		customHierarchyRoot;
	private ParserLogger		logger;

	public ParserSettingsBuilderImpl() {
		completionMode = CompletionMode.COMPLETE_AND_REPLACE_WHOLE_WORDS;
		innerClassNames = ImmutableList.of();
		importedClasses = ImmutableSet.of();
		importedPackages = ImmutableSet.of();
		minimumAccessModifier = AccessModifier.PUBLIC;
		evaluationMode = EvaluationMode.MIXED;
		considerAllClassesForClassCompletions = false;
		customHierarchyRoot = ParserSettingsUtils.createEmptyLeafNode();
		logger = ParserLoggers.createNullLogger();
	}

	ParserSettingsBuilderImpl(ParserSettings settings) {
		completionMode = settings.getCompletionMode();
		innerClassNames = ImmutableList.copyOf(settings.getInnerClassNames());
		importedClasses = ImmutableSet.copyOf(settings.getImports().getImportedClasses());
		importedPackages = ImmutableSet.copyOf(settings.getImports().getImportedPackages());
		minimumAccessModifier = settings.getMinimumAccessModifier();
		evaluationMode = settings.getEvaluationMode();
		considerAllClassesForClassCompletions = settings.isConsiderAllClassesForClassCompletions();
		customHierarchyRoot = settings.getCustomHierarchyRoot();
		logger = settings.getLogger();
	}

	@Override
	public ParserSettingsBuilder completionMode(CompletionMode completionMode) {
		this.completionMode = completionMode;
		return this;
	}

	@Override
	public ParserSettingsBuilder innerClassNames(Collection<String> innerClassNames) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (String innerClassName : innerClassNames) {
			if (innerClassName.contains(" ")) {
				continue;
			}
			String normalizedName;
			try {
				normalizedName = ClassUtils.normalizeClassName(innerClassName, false);
			} catch (Throwable ignored) {
				/* ignore this class */
				continue;
			}
			if (normalizedName.contains("$")) {
				// only consider inner classes
				builder.add(normalizedName);
			}
		}
		this.innerClassNames = builder.build();
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
			String normalizedClassName = ClassUtils.normalizeClassName(className, false);
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
	public ParserSettingsBuilder minimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
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
	public ParserSettingsBuilder customHierarchyRoot(ObjectTreeNode customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	@Override
	public ParserSettingsBuilder logger(ParserLogger logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		return new ParserSettingsImpl(completionMode, innerClassNames, importedClasses, importedPackages, minimumAccessModifier, evaluationMode, considerAllClassesForClassCompletions, customHierarchyRoot, logger);
	}
}
