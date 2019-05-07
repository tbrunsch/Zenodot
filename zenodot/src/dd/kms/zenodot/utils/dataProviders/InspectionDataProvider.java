package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

/**
 * Abstraction layer of class {@link ReflectionUtils} that takes the access level configured in
 * {@link ParserSettings} into account and wraps plain fields and executables in wrapper classes
 * {@link FieldInfo} and {@link ExecutableInfo}.
 */
public class InspectionDataProvider
{
	private final IntPredicate	accessLevelFilter;

	public InspectionDataProvider(ParserToolbox parserToolbox) {
		AccessLevel accessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		this.accessLevelFilter = createAccessLevelFilter(accessLevel);
	}

	private static IntPredicate createAccessLevelFilter(AccessLevel minimumAccessLevel) {
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
		IntPredicate modifierFilter = staticOnly ? accessLevelFilter.and(Modifier::isStatic) : accessLevelFilter;
		List<Field> fields = ReflectionUtils.getFields(contextType.getRawType(), true, modifierFilter);
		return fields.stream()
				.map(field -> InfoProvider.createFieldInfo(field, contextType))
				.collect(Collectors.toList());
	}

	public List<ExecutableInfo> getMethodInfos(TypeInfo contextType, boolean staticOnly) {
		IntPredicate modifierFilter = staticOnly ? accessLevelFilter.and(Modifier::isStatic) : accessLevelFilter;
		List<Method> methods = ReflectionUtils.getMethods(contextType.getRawType(), modifierFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(InfoProvider.getAvailableExecutableInfos(method, contextType));
		}
		return executableInfos;
	}

	public List<ExecutableInfo> getConstructorInfos(TypeInfo contextType) {
		List<Constructor<?>> constructors = ReflectionUtils.getConstructors(contextType.getRawType(), accessLevelFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(InfoProvider.getAvailableExecutableInfos(constructor, contextType));
		}
		return executableInfos;
	}
}
