package dd.kms.zenodot.impl.common;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.FieldScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
public class FieldScannerBuilderImpl implements FieldScannerBuilder
{
	private AccessModifier		minimumAccessModifier	= AccessModifier.PRIVATE;
	private StaticMode			staticMode				= StaticMode.BOTH;
	private boolean				ignoreShadowedFields	= true;
	private Optional<String>	name					= Optional.empty();

	@Override
	public FieldScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier) {
		this.minimumAccessModifier = minimumAccessModifier;
		return this;
	}

	@Override
	public FieldScannerBuilder staticMode(StaticMode staticMode) {
		this.staticMode = staticMode;
		return this;
	}

	@Override
	public FieldScannerBuilder ignoreShadowedFields(boolean ignoreShadowedFields) {
		this.ignoreShadowedFields = ignoreShadowedFields;
		return this;
	}

	@Override
	public FieldScannerBuilder name(String name) {
		this.name = Optional.of(name);
		return this;
	}

	@Override
	public FieldScanner build() {
		Predicate<Field> filter = getFilter();
		return new FieldScannerImpl(filter, ignoreShadowedFields);
	}

	private Predicate<Field> getFilter() {
		List<Predicate<Field>> filters = new ArrayList<>();

		if (minimumAccessModifier != AccessModifier.PRIVATE) {
			IntPredicate modifierFilter = ModifierFilters.createMinimumAccessModifierFilter(minimumAccessModifier);
			Predicate<Field> minimumAccessModifierFilter = field -> modifierFilter.test(field.getModifiers());
			filters.add(minimumAccessModifierFilter);
		}

		if (staticMode == StaticMode.STATIC) {
			Predicate<Field> staticModeFilter = field -> Modifier.isStatic(field.getModifiers());
			filters.add(staticModeFilter);
		} else if (staticMode == StaticMode.NON_STATIC) {
			Predicate<Field> staticModeFilter = field -> !Modifier.isStatic(field.getModifiers());
			filters.add(staticModeFilter);
		}

		if (name.isPresent()) {
			String fieldName = name.get();
			Predicate<Field> nameFilter = field -> field.getName().equals(fieldName);
			filters.add(nameFilter);
		}

		Predicate<Field> specialClassFilter = field -> !field.getName().startsWith("this$");
		filters.add(specialClassFilter);

		return Predicates.and(filters);
	}
}
