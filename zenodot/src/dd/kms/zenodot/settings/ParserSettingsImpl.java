package dd.kms.zenodot.settings;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.List;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final Imports			imports;
	private final VariablePool 		variablePool;
	private final AccessModifier minimumAccessLevel;
	private final boolean			enableDynamicTyping;
	private final ObjectTreeNode	customHierarchyRoot;

	private final ParserLogger logger;

	ParserSettingsImpl(Set<ClassInfo> importedClasses, Set<String> importedPackageNames, List<Variable> variables, AccessModifier minimumAccessLevel, boolean enableDynamicTyping, ObjectTreeNode customHierarchyRoot, ParserLogger logger) {
		this.imports = new ImportsImpl(importedClasses, importedPackageNames);
		this.variablePool = new VariablePool(variables);
		this.minimumAccessLevel = minimumAccessLevel;
		this.enableDynamicTyping = enableDynamicTyping;
		this.customHierarchyRoot = customHierarchyRoot;
		this.logger = logger;
	}

	public Imports getImports() {
		return imports;
	}

	public List<Variable> getVariables() {
		return variablePool.getVariables();
	}

	public AccessModifier getMinimumAccessLevel() {
		return minimumAccessLevel;
	}

	public boolean isEnableDynamicTyping() {
		return enableDynamicTyping;
	}

	public ObjectTreeNode getCustomHierarchyRoot() {
		return customHierarchyRoot;
	}

	public ParserLogger getLogger() {
		return logger;
	}

	public ParserSettingsBuilder builder() {
		return new ParserSettingsBuilderImpl(this);
	}
}
