package dd.kms.zenodot.settings;

import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.List;
import java.util.Set;

/**
 * Immutable settings for the parsing process. Can only be created with a {@link ParserSettingsBuilder}.<br/>
 * <br/>
 * You can either create a new builder or derive one from existing settings via {@link ParserSettings#builder()}.
 */
public class ParserSettings
{
	private final Imports			imports;
	private final VariablePool variablePool;
	private final AccessLevel		minimumAccessLevel;
	private final boolean			enableDynamicTyping;
	private final ObjectTreeNode customHierarchyRoot;

	private final ParserLogger logger;

	ParserSettings(Set<ClassInfo> importedClasses, Set<String> importedPackageNames, List<Variable> variables, AccessLevel minimumAccessLevel, boolean enableDynamicTyping, ObjectTreeNode customHierarchyRoot, ParserLogger logger) {
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

	public List<Variable> getVariables() {
		return variablePool.getVariables();
	}

	public AccessLevel getMinimumAccessLevel() {
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
		return new ParserSettingsBuilder(this);
	}
}
