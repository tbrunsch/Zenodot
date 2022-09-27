package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;
import dd.kms.zenodot.api.wrappers.ClassInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.PackageInfo;
import dd.kms.zenodot.impl.debug.ParserLoggers;

import java.util.List;
import java.util.Set;

public class ParserSettingsBuilderImpl implements ParserSettingsBuilder
{
	private CompletionMode		completionMode;
	private Set<ClassInfo>		importedClasses;
	private Set<PackageInfo>	importedPackages;
	private List<Variable>		variables;
	private AccessModifier		minimumAccessModifier;
	private EvaluationMode		evaluationMode;
	private boolean				considerAllClassesForClassCompletions;
	private ObjectTreeNode		customHierarchyRoot;
	private ParserLogger		logger;

	public ParserSettingsBuilderImpl() {
		completionMode = CompletionMode.COMPLETE_AND_REPLACE_WHOLE_WORDS;
		importedClasses = ImmutableSet.of();
		importedPackages = ImmutableSet.of();
		variables = ImmutableList.of();
		minimumAccessModifier = AccessModifier.PUBLIC;
		evaluationMode = EvaluationMode.MIXED;
		considerAllClassesForClassCompletions = false;
		customHierarchyRoot = ParserSettingsUtils.createEmptyLeafNode();
		logger = ParserLoggers.createNullLogger();
	}

	ParserSettingsBuilderImpl(ParserSettings settings) {
		completionMode = settings.getCompletionMode();
		importedClasses = ImmutableSet.copyOf(settings.getImports().getImportedClasses());
		importedPackages = ImmutableSet.copyOf(settings.getImports().getImportedPackages());
		variables = ImmutableList.copyOf(settings.getVariables());
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
	public ParserSettingsBuilder importClasses(Iterable<ClassInfo> classes) {
		importedClasses = ImmutableSet.copyOf(classes);
		return this;
	}

	@Override
	public ParserSettingsBuilder importClassesByName(Iterable<String> classNames) throws ClassNotFoundException {
		ImmutableSet.Builder<ClassInfo> builder = ImmutableSet.builder();
		for (String className : classNames) {
			ClassInfo classInfo = InfoProvider.createClassInfo(className);
			builder.add(classInfo);
		}
		return importClasses(builder.build());
	}

	@Override
	public ParserSettingsBuilder importPackages(Iterable<PackageInfo> packages) {
		importedPackages = ImmutableSet.copyOf(packages);
		return this;
	}

	@Override
	public ParserSettingsBuilder importPackagesByName(Iterable<String> packageNames) {
		ImmutableSet.Builder<PackageInfo> builder = ImmutableSet.builder();
		for (String packageName : packageNames) {
			PackageInfo packageInfo = InfoProvider.createPackageInfo(packageName);
			builder.add(packageInfo);
		}
		return importPackages(builder.build());
	}

	@Override
	public ParserSettingsBuilder variables(List<Variable> variables) {
		this.variables = ImmutableList.copyOf(variables);
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
		return new ParserSettingsImpl(completionMode, importedClasses, importedPackages, variables, minimumAccessModifier, evaluationMode, considerAllClassesForClassCompletions, customHierarchyRoot, logger);
	}
}
