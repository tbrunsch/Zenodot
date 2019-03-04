package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.debug.ParserNullLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

/**
 * Builder for {@link ParserSettings}
 */
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

	/**
	 * When you import a class, then you can directly reference that class by its simple name.
	 */
	public ParserSettingsBuilder importClass(String qualifiedClassName) {
		try {
			importClassesBuilder.add(ClassInfo.forName(qualifiedClassName));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return this;
	}

	/**
	 * When you import a package, then you can directly reference any of its classes by their simple names.
	 */
	public ParserSettingsBuilder importPackage(String packageName) {
		importPackageNamesBuilder.add(packageName);
		return this;
	}

	/**
	 * When you add a variable, then you can reference its value by the variable's name.
	 */
	public ParserSettingsBuilder addVariable(Variable variable) {
		variablesBuilder.add(variable);
		return this;
	}

	/**
	 * The minimum access level affects which fields and methods are suggested for code completion and
	 * are accepted when evaluating expressions. When setting this to {@link AccessLevel#PRIVATE}, then
	 * all fields and methods will be considered.
	 */
	public ParserSettingsBuilder minimumAccessLevel(AccessLevel minimumAccessLevel) {
		this.minimumAccessLevel = minimumAccessLevel;
		return this;
	}

	/**
	 * Enable dynamic typing to consider runtime types instead of declared types during code completion
	 * and expression evaluation.<br/>
	 * <br/>
	 * This can save cumbersome type casts at the risk of unintended side effects or method overload resolutions
	 * deviating from those based on declared type.
	 */
	public ParserSettingsBuilder enableDynamicTyping(boolean enableDynamicTyping) {
		this.enableDynamicTyping = enableDynamicTyping;
		return this;
	}

	/**
	 * Call this method if you want to inject a custom hierarchy into the parser that is not represented
	 * by field and method names of classes. Each node of the custom hierarchy must be wrapped into an
	 * {@link ObjectTreeNodeIF}. Zenodot then provides code completion and evaluation for this hierarchy.<br/>
	 * <br/>
	 * An access to a node in the custom hierarchy has to be embraced in curly braces {@code {...}}. The
	 * character {@code #} is used to separate a child node from its parent node.<br/>
	 * <br/>
	 * <b>Example:</b> Assume that for some reason you want to make your main menu commands accessible when
	 *          parsing expressions. Further assume that you can currently select
	 *          File -> Open Recent -> README.md in your menu. If you provide this information by specifying
	 *          a custom hierarchy, then you can access the command behind that menu item via
	 *          {@code {File#Open Recent#README.md}} in the expression.
	 *
	 */
	public ParserSettingsBuilder customHierarchyRoot(ObjectTreeNodeIF customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
		return this;
	}

	/**
	 * Specify a logger that receives messages during the parsing process. This is primarily meant for
	 * debugging purposes.
	 */
	public ParserSettingsBuilder logger(ParserLoggerIF logger) {
		this.logger = logger;
		return this;
	}

	public ParserSettings build() {
		return new ParserSettings(importClassesBuilder.build(), importPackageNamesBuilder.build(), variablesBuilder.build(), minimumAccessLevel, enableDynamicTyping, customHierarchyRoot, logger);
	}
}
