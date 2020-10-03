package dd.kms.zenodot.impl.common;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
public class MethodScannerBuilderImpl implements MethodScannerBuilder
{
	private AccessModifier		minimumAccessModifier	= AccessModifier.PRIVATE;
	private StaticMode			staticMode				= StaticMode.BOTH;
	private Optional<String>	name					= Optional.empty();

	@Override
	public MethodScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
		return this;
	}

	@Override
	public MethodScannerBuilder staticMode(StaticMode staticMode) {
		this.staticMode = staticMode;
		return this;
	}

	@Override
	public MethodScannerBuilder name(String name) {
		this.name = Optional.of(name);
		return this;
	}

	@Override
	public MethodScanner build() {
		Predicate<Method> filter = getFilter();
		return new MethodScannerImpl(filter);
	}

	private Predicate<Method> getFilter() {
		List<Predicate<Method>> filters = new ArrayList<>();

		if (minimumAccessModifier != AccessModifier.PRIVATE) {
			IntPredicate modifierFilter = ModifierFilters.createMinimumAccessModifierFilter(minimumAccessModifier);
			Predicate<Method> minimumAccessModifierFilter = method -> modifierFilter.test(method.getModifiers());
			filters.add(minimumAccessModifierFilter);
		}

		if (staticMode == StaticMode.STATIC) {
			Predicate<Method> staticModeFilter = method -> Modifier.isStatic(method.getModifiers());
			filters.add(staticModeFilter);
		} else if (staticMode == StaticMode.NON_STATIC) {
			Predicate<Method> staticModeFilter = method -> !Modifier.isStatic(method.getModifiers());
			filters.add(staticModeFilter);
		}

		if (name.isPresent()) {
			String methodName = name.get();
			Predicate<Method> nameFilter = method -> method.getName().equals(methodName);
			filters.add(nameFilter);
		}

		return Predicates.and(filters);
	}
}
