package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.settings.Imports;
import dd.kms.zenodot.api.wrappers.PackageInfo;

import java.util.Set;

class ImportsImpl implements Imports
{
	private final ImmutableSet<Class<?>>	importClasses;
	private final ImmutableSet<PackageInfo> importPackages;

	ImportsImpl(Set<Class<?>> importClasses, Set<PackageInfo> importPackages) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackages = ImmutableSet.copyOf(importPackages);
	}

	@Override
	public Set<Class<?>> getImportedClasses() {
		return importClasses;
	}

	@Override
	public Set<PackageInfo> getImportedPackages() {
		return importPackages;
	}
}
