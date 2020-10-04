package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.ConstructorScanner;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the constructors of a class optionally considering a minimum access modifier.
 */
class ConstructorScannerImpl implements ConstructorScanner
{
	private final Predicate<Constructor<?>>	filter;

	ConstructorScannerImpl(Predicate<Constructor<?>> filter) {
		this.filter = filter;
	}

	@Override
	public List<Constructor<?>> getConstructors(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredConstructors())
			.filter(filter)
			.collect(Collectors.toList());
	}
}
