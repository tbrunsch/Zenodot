package dd.kms.zenodot.api.settings;

import java.util.Set;

/**
 * Contains information about the imported classes and packages.<br>
 * <br>
 * An existing instance can be obtained from {@link ParserSettings#getImports()}.
 * A new instance can be created via the {@link ParserSettingsBuilder}.
 */
public interface Imports
{
	/**
	 * When you import a class, then you can directly reference that class by its simple name.
	 */
	Set<Class<?>> getImportedClasses();

	/**
	 * When you import a package, then you can directly reference any of its classes by their simple names.
	 */
	Set<String> getImportedPackages();
}
