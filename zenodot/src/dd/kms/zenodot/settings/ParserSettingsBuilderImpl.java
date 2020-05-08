package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.List;
import java.util.Set;

class ParserSettingsBuilderImpl implements ParserSettingsBuilder
{
	private Set<ClassInfo>		importedClasses;
	private Set<PackageInfo>	importedPackages;
	private List<Variable>		variables;
	private AccessModifier		minimumAccessLevel;
	private boolean				enableDynamicTyping;
	private boolean				considerAllClassesForClassSuggestions;
	private ObjectTreeNode		customHierarchyRoot;
	private ParserLogger		logger;

	ParserSettingsBuilderImpl() {
		importedClasses = ImmutableSet.of();
		importedPackages = ImmutableSet.of();
		variables = ImmutableList.of();
		minimumAccessLevel = AccessModifier.PUBLIC;
		enableDynamicTyping = false;
		considerAllClassesForClassSuggestions = false;
		customHierarchyRoot = ParserSettingsUtils.createEmptyLeafNode();
		logger = ParserLoggers.createNullLogger();
	}

	ParserSettingsBuilderImpl(ParserSettings settings) {
		importedClasses = ImmutableSet.copyOf(settings.getImports().getImportedClasses());
		importedPackages = ImmutableSet.copyOf(settings.getImports().getImportedPackages());
		variables = ImmutableList.copyOf(settings.getVariables());
		minimumAccessLevel = settings.getMinimumAccessLevel();
		enableDynamicTyping = settings.isEnableDynamicTyping();
		considerAllClassesForClassSuggestions = settings.isConsiderAllClassesForClassSuggestions();
		customHierarchyRoot = settings.getCustomHierarchyRoot();
		logger = settings.getLogger();
	}

	@Override
	public ParserSettingsBuilderImpl importClasses(Iterable<ClassInfo> classes) {
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
	public ParserSettingsBuilderImpl importPackages(Iterable<PackageInfo> packages) {
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
	public ParserSettingsBuilderImpl variables(List<Variable> variables) {
		this.variables = ImmutableList.copyOf(variables);
		return this;
	}

	@Override
	public ParserSettingsBuilderImpl minimumAccessLevel(AccessModifier minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		return this;
	}

	@Override
	public ParserSettingsBuilderImpl enableDynamicTyping(boolean enableDynamicTyping) {
		this.enableDynamicTyping = enableDynamicTyping;
		return this;
	}

	@Override
	public ParserSettingsBuilder considerAllClassesForClassSuggestions(boolean considerAllClassesForClassSuggestions) {
		this.considerAllClassesForClassSuggestions = considerAllClassesForClassSuggestions;
		return this;
	}

	@Override
	public ParserSettingsBuilderImpl customHierarchyRoot(ObjectTreeNode customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	@Override
	public ParserSettingsBuilderImpl logger(ParserLogger logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		return new ParserSettingsImpl(importedClasses, importedPackages, variables, minimumAccessLevel, enableDynamicTyping, considerAllClassesForClassSuggestions, customHierarchyRoot, logger);
	}
}
