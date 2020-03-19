package dd.kms.zenodot.settings;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.List;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final Imports			imports;
	private final VariablePool 		variablePool;
	private final AccessModifier	minimumAccessLevel;
	private final boolean			enableDynamicTyping;
	private final boolean			considerAllClassesForClassSuggestions;
	private final ObjectTreeNode	customHierarchyRoot;

	private final ParserLogger logger;

	ParserSettingsImpl(Set<ClassInfo> importedClasses, Set<PackageInfo> importedPackages, List<Variable> variables, AccessModifier minimumAccessLevel, boolean enableDynamicTyping, boolean considerAllClassesForClassSuggestions, ObjectTreeNode customHierarchyRoot, ParserLogger logger) {
		this.imports = new ImportsImpl(importedClasses, importedPackages);
		this.variablePool = new VariablePool(variables);
		this.minimumAccessLevel = minimumAccessLevel;
		this.enableDynamicTyping = enableDynamicTyping;
		this.considerAllClassesForClassSuggestions = considerAllClassesForClassSuggestions;
		this.customHierarchyRoot = customHierarchyRoot;
		this.logger = logger;
	}

	@Override
	public Imports getImports() {
		return imports;
	}

	@Override
	public List<Variable> getVariables() {
		return variablePool.getVariables();
	}

	@Override
	public AccessModifier getMinimumAccessLevel() {
		return minimumAccessLevel;
	}

	@Override
	public boolean isEnableDynamicTyping() {
		return enableDynamicTyping;
	}

	@Override
	public boolean isConsiderAllClassesForClassSuggestions() {
		return considerAllClassesForClassSuggestions;
	}

	@Override
	public ObjectTreeNode getCustomHierarchyRoot() {
		return customHierarchyRoot;
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
