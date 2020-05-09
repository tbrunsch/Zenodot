package dd.kms.zenodot.settings;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.List;

/**
 * Builder for {@link ParserSettings}<br/>
 * <br/>
 * You can either create a new builder via {@link ParserSettingsUtils#createBuilder()} or derive
 * one from existing settings via {@link ParserSettings#builder()}.
 */
public interface ParserSettingsBuilder
{
	/**
	 * When you import a class, then you can directly reference that class by its simple name.
	 */
	ParserSettingsBuilder importClasses(Iterable<ClassInfo> classes);

	/**
	 * see {@link #importClasses(Iterable)}
	 */
	ParserSettingsBuilder importClassesByName(Iterable<String> classNames) throws ClassNotFoundException;

	/**
	 * When you import a package, then you can directly reference any of its classes by their simple names.
	 */
	ParserSettingsBuilder importPackages(Iterable<PackageInfo> packages);

	/**
	 * see {@link #importPackages(Iterable)}
	 */
	ParserSettingsBuilder importPackagesByName(Iterable<String> packageNames);

	/**
	 * When you add a variable, then you can reference its value by the variable's name.
	 */
	ParserSettingsBuilder variables(List<Variable> variables);

	/**
	 * The minimum access level affects which fields and methods are suggested for code completion and
	 * are accepted when evaluating expressions. When setting this to {@link AccessModifier#PRIVATE}, then
	 * all fields and methods will be considered.
	 */
	ParserSettingsBuilder minimumAccessLevel(AccessModifier minimumAccessLevel);

	/**
	 * Enable dynamic typing to consider runtime types instead of declared types during code completion
	 * and expression evaluation.<br/>
	 * <br/>
	 * This can save cumbersome type casts at the risk of unintended side effects or method overload resolutions
	 * deviating from those based on declared type.
	 */
	ParserSettingsBuilder enableDynamicTyping(boolean enableDynamicTyping);

	/**
	 * By default, when typing an unqualified class name, only classes you have imported or whose package is
	 * imported will be considered as code completions. With enabling this feature, also classes from other
	 * packages will be suggested, but fully qualified.<br/>
	 * <br/>
	 * Consider the partial class name "Lis". If you have not imported "java.util", then you will get the
	 * code completion "java.util.List" if this feature is enabled. Otherwise, that class will not be suggested.
	 */
	ParserSettingsBuilder considerAllClassesForClassCompletions(boolean considerAllClassesForClassCompletions);

	/**
	 * Call this method if you want to inject a custom hierarchy into the parser that is not represented
	 * by field and method names of classes. Each node of the custom hierarchy must be wrapped into an
	 * {@link ObjectTreeNode}. Zenodot then provides code completion and evaluation for this hierarchy.<br/>
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
	ParserSettingsBuilder customHierarchyRoot(ObjectTreeNode customHierarchyRoot);

	/**
	 * Specify a logger that receives messages during the parsing process. This is primarily meant for
	 * debugging purposes.
	 */
	ParserSettingsBuilder logger(ParserLogger logger);

	ParserSettings build();
}
