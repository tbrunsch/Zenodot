package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Set;

class ImportsImpl implements Imports
{
	private final ImmutableSet<ClassInfo>	importClasses;
	private final ImmutableSet<String>		importPackageNames;

	ImportsImpl(Set<ClassInfo> importClasses, Set<String> importPackageNames) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackageNames = ImmutableSet.copyOf(importPackageNames);
	}

	@Override
	public Set<ClassInfo> getImportedClasses() {
		return importClasses;
	}

	@Override
	public ImmutableSet<String> getImportedPackageNames() {
		return importPackageNames;
	}
}
