package dd.kms.zenodot.settings;

import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.utils.VariablePool;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.List;
import java.util.Set;

public class ParserSettings
{
	private final Imports			imports;
	private final VariablePool variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final boolean			enableDynamicTyping;
	private final ObjectTreeNodeIF	customHierarchyRoot;

	private final ParserLoggerIF	logger;

	ParserSettings(Set<ClassInfo> importedClasses, Set<String> importedPackageNames, List<Variable> variables, AccessLevel minimumAccessLevel, boolean enableDynamicTyping, ObjectTreeNodeIF customHierarchyRoot, ParserLoggerIF logger) {
		this.imports = new Imports(importedClasses, importedPackageNames);
		this.variablePool = new VariablePool(variables);
		this.minimumAccessLevel = minimumAccessLevel;
		this.enableDynamicTyping = enableDynamicTyping;
		this.customHierarchyRoot = customHierarchyRoot;
		this.logger = logger;
	}

	public Imports getImports() {
		return imports;
	}

	public VariablePool getVariablePool() {
		return variablePool;
	}

	public AccessLevel getMinimumAccessLevel() {
		return minimumAccessLevel;
	}

	public boolean isEnableDynamicTyping() {
		return enableDynamicTyping;
	}

	public ObjectTreeNodeIF getCustomHierarchyRoot() {
		return customHierarchyRoot;
	}

	public ParserLoggerIF getLogger() {
		return logger;
	}

	public ParserSettingsBuilder builder() {
		return new ParserSettingsBuilder(this);
	}
}
