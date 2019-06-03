package dd.kms.zenodot.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.Set;

class ImportsImpl implements Imports
{
	private final ImmutableSet<ClassInfo>	importClasses;
	private final ImmutableSet<PackageInfo> importPackages;

	ImportsImpl(Set<ClassInfo> importClasses, Set<PackageInfo> importPackages) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackages = ImmutableSet.copyOf(importPackages);
	}

	@Override
	public Set<ClassInfo> getImportedClasses() {
		return importClasses;
	}

	@Override
	public Set<PackageInfo> getImportedPackages() {
		return importPackages;
	}
}
