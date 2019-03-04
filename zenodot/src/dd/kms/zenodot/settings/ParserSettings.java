package dd.kms.zenodot.settings;

import dd.kms.zenodot.debug.ParserLoggerIF;

public class ParserSettings
{
	private final Imports			imports;
	private final VariablePool		variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final boolean			enableDynamicTyping;
	private final ObjectTreeNodeIF	customHierarchyRoot;

	private final ParserLoggerIF	logger;

	ParserSettings(Imports imports, VariablePool variablePool, AccessLevel minimumAccessLevel, boolean enableDynamicTyping, ObjectTreeNodeIF customHierarchyRoot, ParserLoggerIF logger) {
		this.imports = imports;
		this.variablePool = variablePool;
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
