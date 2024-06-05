package dd.kms.zenodot.impl.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.GeneralizedField;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
class FieldScannerImpl implements FieldScanner
{
	private final Predicate<GeneralizedField>	filter;
	private final boolean						ignoreShadowedFields;

	FieldScannerImpl(Predicate<GeneralizedField> filter, boolean ignoreShadowedFields) {
		this.filter = filter;
		this.ignoreShadowedFields = ignoreShadowedFields;
	}

	@Override
	public List<GeneralizedField> getFields(Class<?> clazz) {
		Multimap<String, GeneralizedField> fieldsByName = ArrayListMultimap.create();
		addFields(clazz, fieldsByName);
		return fieldsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz))
			.collect(Collectors.toList());
	}

	private void addFields(Class<?> clazz, Multimap<String, GeneralizedField> fieldsByName) {
		if (clazz == null) {
			return;
		}
		if (clazz.isArray()) {
			ArrayLengthField arrayLengthField = new ArrayLengthField(clazz);
			if (filter.test(arrayLengthField)) {
				fieldsByName.put(arrayLengthField.getName(), arrayLengthField);
			}
		}

		for (Field field : clazz.getDeclaredFields()) {
			DefaultGeneralizedField generalizedField = new DefaultGeneralizedField(field);
			if (!filter.test(generalizedField)) {
				continue;
			}
			String fieldName = field.getName();
			Collection<GeneralizedField> fields = fieldsByName.get(fieldName);
			Iterator<GeneralizedField> fieldIterator = fields.iterator();
			boolean addField = true;
			while (fieldIterator.hasNext()) {
				GeneralizedField otherField = fieldIterator.next();
				Class<?> otherDeclaringClass = otherField.getDeclaringClass();
				if (otherDeclaringClass == clazz) {
					addField = false;
				} else if (clazz.isAssignableFrom(otherDeclaringClass)) {
					if (ignoreShadowedFields) {
						addField = false;
					}
				} else if (otherDeclaringClass.isAssignableFrom(clazz)) {
					if (ignoreShadowedFields) {
						fieldIterator.remove();
					}
				}
			}
			if (addField) {
				fields.add(generalizedField);
			}
		}

		Class<?> superclass = clazz.getSuperclass();
		addFields(superclass, fieldsByName);
		for (Class<?> implementedInterfaces : clazz.getInterfaces()){
			addFields(implementedInterfaces, fieldsByName);
		}
	}
}
