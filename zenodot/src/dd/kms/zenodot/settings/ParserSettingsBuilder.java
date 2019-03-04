package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.debug.ParserNullLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

public class ParserSettingsBuilder
{
	private final ImmutableSet.Builder<ClassInfo> 	importClassesBuilder		= ImmutableSet.builder();
	private final ImmutableSet.Builder<String>	 	importPackageNamesBuilder	= ImmutableSet.builder();
	private final ImmutableList.Builder<Variable>	variablesBuilder 			= ImmutableList.builder();
	private AccessLevel								minimumAccessLevel			= AccessLevel.PUBLIC;
	private boolean									enableDynamicTyping			= false;
	private ObjectTreeNodeIF						customHierarchyRoot			= LeafObjectTreeNode.EMPTY;
	private ParserLoggerIF							logger						= new ParserNullLogger();

	public ParserSettingsBuilder() {}

	public ParserSettingsBuilder(ParserSettings settings) {
		importClassesBuilder.addAll(settings.getImports().getImportedClasses());
		importPackageNamesBuilder.addAll(settings.getImports().getImportedPackageNames());
		variablesBuilder.addAll(settings.getVariablePool().getVariables());
		minimumAccessLevel = settings.getMinimumAccessLevel();
		enableDynamicTyping = settings.isEnableDynamicTyping();
		customHierarchyRoot = settings.getCustomHierarchyRoot();
		logger = settings.getLogger();
	}

	public ParserSettingsBuilder importClass(String qualifiedClassName) {
		try {
			importClassesBuilder.add(ClassInfo.forName(qualifiedClassName));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return this;
	}

	public ParserSettingsBuilder importPackage(String packageName) {
		importPackageNamesBuilder.add(packageName);
		return this;
	}

	public ParserSettingsBuilder addVariable(Variable variable) {
		variablesBuilder.add(variable);
		return this;
	}

	public ParserSettingsBuilder minimumAccessLevel(AccessLevel minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		return this;
	}

	public ParserSettingsBuilder enableDynamicTyping(boolean enableDynamicTyping) {
		this.enableDynamicTyping = enableDynamicTyping;
		return this;
	}

	public ParserSettingsBuilder customHierarchyRoot(ObjectTreeNodeIF customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	public ParserSettingsBuilder logger(ParserLoggerIF logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		Imports imports = new Imports(importClassesBuilder.build(), importPackageNamesBuilder.build());
		VariablePool variablePool = new VariablePool(variablesBuilder.build());
		return new ParserSettings(imports, variablePool, minimumAccessLevel, enableDynamicTyping, customHierarchyRoot, logger);
	}
}
