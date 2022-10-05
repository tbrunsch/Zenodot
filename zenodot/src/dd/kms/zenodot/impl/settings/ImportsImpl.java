package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.settings.Imports;

import java.util.Set;

class ImportsImpl implements Imports
{
	private final ImmutableSet<Class<?>>	importClasses;
	private final ImmutableSet<String> importPackages;

	ImportsImpl(Set<Class<?>> importClasses, Set<String> importPackages) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackages = ImmutableSet.copyOf(importPackages);
	}

	@Override
	public Set<Class<?>> getImportedClasses() {
		return importClasses;
	}

	@Override
	public Set<String> getImportedPackages() {
		return importPackages;
	}
}
