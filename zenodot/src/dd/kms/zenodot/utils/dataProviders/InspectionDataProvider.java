package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InspectionDataProvider
{
	private static final Predicate<Integer>	STATIC_FILTER = modifiers -> Modifier.isStatic(modifiers);

	private final ParserToolbox parserToolbox;
	private final Predicate<Integer>	accessLevelFilter;

	public InspectionDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
		AccessLevel accessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		this.accessLevelFilter = createAccessLevelFilter(accessLevel);
	}

	private static Predicate<Integer> createAccessLevelFilter(final AccessLevel minimumAccessLevel) {
		switch (minimumAccessLevel) {
			case PUBLIC:
				return modifiers -> Modifier.isPublic(modifiers);
			case PROTECTED:
				return modifiers -> Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
			case PACKAGE_PRIVATE:
				return modifiers -> !Modifier.isPrivate(modifiers);
			case PRIVATE:
				return modifiers -> true;
			default:
				throw new IllegalArgumentException("Unsupported access level " + minimumAccessLevel);
		}
	}

	public List<FieldInfo> getFieldInfos(TypeInfo contextType, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		List<Field> fields = ReflectionUtils.getFields(contextType.getRawType(), true, modifierFilter);
		return fields.stream()
				.map(field -> new FieldInfo(field, contextType))
				.collect(Collectors.toList());
	}

	public List<ExecutableInfo> getMethodInfos(TypeInfo contextType, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		List<Method> methods = ReflectionUtils.getMethods(contextType.getRawType(), modifierFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(ExecutableInfo.getAvailableExecutableInfos(method, contextType));
		}
		return executableInfos;
	}

	public List<ExecutableInfo> getConstructorInfos(TypeInfo contextType) {
		List<Constructor<?>> constructors = ReflectionUtils.getConstructors(contextType.getRawType(), accessLevelFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(ExecutableInfo.getAvailableExecutableInfos(constructor, contextType));
		}
		return executableInfos;
	}
}