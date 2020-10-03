package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.ConstructorScanner;
import dd.kms.zenodot.api.common.ConstructorScannerBuilder;

import java.lang.reflect.Constructor;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Scans the constructors of a class optionally considering a minimum access modifier.
 */
public class ConstructorScannerBuilderImpl implements ConstructorScannerBuilder
{
	private AccessModifier minimumAccessModifier	= AccessModifier.PRIVATE;

	@Override
	public ConstructorScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
		return this;
	}

	@Override
	public ConstructorScanner build() {
		IntPredicate modifierFilter = ModifierFilters.createMinimumAccessModifierFilter(minimumAccessModifier);
		Predicate<Constructor<?>> filter = constructor -> modifierFilter.test(constructor.getModifiers());
		return new ConstructorScannerImpl(filter);
	}
}
