package dd.kms.zenodot.utils.wrappers;

import com.google.common.reflect.TypeToken;
import dd.kms.zenodot.common.ConstructorScanner;
import dd.kms.zenodot.common.FieldScanner;
import dd.kms.zenodot.common.MethodScanner;
import dd.kms.zenodot.utils.ClassUtils;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InfoProvider
{
	public static final Object 		INDETERMINATE_VALUE = new Object();

	/**
	 * This field is only used for the {@link ObjectInfo} representing the null literal.
	 */
	public static final TypeInfo	NO_TYPE				= new TypeInfoImpl(null);

	/**
	 * Use this field for creating an {@link ObjectInfo} for an object whose
	 * declared type you do not know.
	 */
	public static final TypeInfo	UNKNOWN_TYPE		= new TypeInfoImpl(null);

	public static final ObjectInfo	NULL_LITERAL		= createObjectInfo(null, NO_TYPE);

	public static List<ExecutableInfo> getAvailableExecutableInfos(Executable executable, TypeInfo declaringType) {
		return executable.isVarArgs()
			? Arrays.asList(new RegularExecutableInfo(executable, declaringType), new VariadicExecutableInfo(executable, declaringType))
			: Arrays.asList(new RegularExecutableInfo(executable, declaringType));
	}

	public static ClassInfo createClassInfo(String qualifiedClassName) throws ClassNotFoundException {
		return new ClassInfoImpl(ClassUtils.normalizeClassName(qualifiedClassName));
	}

	public static ClassInfo createClassInfoUnchecked(String qualifiedClassName) {
		return new ClassInfoImpl(qualifiedClassName);
	}

	public static PackageInfo createPackageInfo(String packageName) {
		return new PackageInfoImpl(packageName);
	}

	public static FieldInfo createFieldInfo(Field field, TypeInfo declaringType) {
		return new FieldInfoImpl(field, declaringType);
	}

	public static ObjectInfo createObjectInfo(Object object) {
		return createObjectInfo(object, UNKNOWN_TYPE);
	}

	public static ObjectInfo createObjectInfo(Object object, TypeInfo declaredType) {
		return createObjectInfo(object, declaredType, null);
	}

	public static ObjectInfo createObjectInfo(Object object, TypeInfo declaredType, ObjectInfo.ValueSetter valueSetter) {
		return new ObjectInfoImpl(object, declaredType, valueSetter);
	}

	public static TypeInfo createTypeInfo(Type type) {
		return type == null ? NO_TYPE : new TypeInfoImpl(TypeToken.of(type));
	}

	public static List<FieldInfo> getFieldInfos(TypeInfo contextType, FieldScanner fieldScanner) {
		List<Field> fields = fieldScanner.getFields(contextType.getRawType(), true);
		return fields.stream()
			.map(field -> createFieldInfo(field, contextType))
			.collect(Collectors.toList());
	}

	public static List<ExecutableInfo> getMethodInfos(TypeInfo contextType, MethodScanner methodScanner) {
		List<Method> methods = methodScanner.getMethods(contextType.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(getAvailableExecutableInfos(method, contextType));
		}
		return executableInfos;
	}

	public static List<ExecutableInfo> getConstructorInfos(TypeInfo contextType, ConstructorScanner constructorScanner) {
		List<Constructor<?>> constructors = constructorScanner.getConstructors(contextType.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(getAvailableExecutableInfos(constructor, contextType));
		}
		return executableInfos;
	}
}