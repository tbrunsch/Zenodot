package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;

import java.util.List;
import java.util.Set;

class ParserSettingsBuilderImpl implements ParserSettingsBuilder
{
	private Set<ClassInfo>	importClasses;
	private Set<String>		importPackages;
	private List<Variable>	variables;
	private AccessLevel		minimumAccessLevel;
	private boolean			enableDynamicTyping;
	private ObjectTreeNode	customHierarchyRoot;
	private ParserLogger	logger;

	ParserSettingsBuilderImpl() {
		importClasses = ImmutableSet.of();
		importPackages = ImmutableSet.of();
		variables = ImmutableList.of();
		minimumAccessLevel = AccessLevel.PUBLIC;
		enableDynamicTyping = false;
		customHierarchyRoot = ParserSettingsUtils.createEmptyLeafNode();
		logger = ParserLoggers.createNullLogger();
	}

	ParserSettingsBuilderImpl(ParserSettings settings) {
		importClasses = ImmutableSet.copyOf(settings.getImports().getImportedClasses());
		importPackages = ImmutableSet.copyOf(settings.getImports().getImportedPackageNames());
		variables = ImmutableList.copyOf(settings.getVariables());
		minimumAccessLevel = settings.getMinimumAccessLevel();
		enableDynamicTyping = settings.isEnableDynamicTyping();
		customHierarchyRoot = settings.getCustomHierarchyRoot();
		logger = settings.getLogger();
	}

	public ParserSettingsBuilderImpl importClasses(Set<String> qualifiedClassNames) {
		ImmutableSet.Builder<ClassInfo> importClassesBuilder = ImmutableSet.builder();
		for (String qualifiedClassName : qualifiedClassNames) {
			try {
				importClassesBuilder.add(InfoProvider.createClassInfo(qualifiedClassName));
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}
		importClasses = importClassesBuilder.build();
		return this;
	}

	public ParserSettingsBuilderImpl importPackages(Set<String> packageNames) {
		importPackages = ImmutableSet.copyOf(packageNames);
		return this;
	}

	public ParserSettingsBuilderImpl variables(List<Variable> variables) {
		this.variables = ImmutableList.copyOf(variables);
		return this;
	}

	public ParserSettingsBuilderImpl minimumAccessLevel(AccessLevel minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		return this;
	}

	public ParserSettingsBuilderImpl enableDynamicTyping(boolean enableDynamicTyping) {
		this.enableDynamicTyping = enableDynamicTyping;
		return this;
	}

	public ParserSettingsBuilderImpl customHierarchyRoot(ObjectTreeNode customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	public ParserSettingsBuilderImpl logger(ParserLogger logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		return new ParserSettingsImpl(importClasses, importPackages, variables, minimumAccessLevel, enableDynamicTyping, customHierarchyRoot, logger);
	}
}
