package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Set;

/**
 * Contains information about the imported classes and packages.
 */
public class Imports
{
	private final ImmutableSet<ClassInfo>	importClasses;
	private final ImmutableSet<String>		importPackageNames;

	Imports(Set<ClassInfo> importClasses, Set<String> importPackageNames) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackageNames = ImmutableSet.copyOf(importPackageNames);
	}

	/**
	 * When you import a class, then you can directly reference that class by its simple name.
	 */
	public Set<ClassInfo> getImportedClasses() {
		return importClasses;
	}

	/**
	 * When you import a package, then you can directly reference any of its classes by their simple names.
	 */
	public ImmutableSet<String> getImportedPackageNames() {
		return importPackageNames;
	}
}
