package dd.kms.zenodot.api.wrappers;

import com.google.common.reflect.TypeToken;
import dd.kms.zenodot.api.common.ConstructorScanner;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.MethodScanner;

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
	public static final TypeInfo	NO_TYPE				= new dd.kms.zenodot.impl.wrappers.TypeInfoImpl(null);

	/**
	 * Use this field for creating an {@link ObjectInfo} for an object whose
	 * declared type you do not know.
	 */
	public static final TypeInfo	UNKNOWN_TYPE		= new dd.kms.zenodot.impl.wrappers.TypeInfoImpl(null);

	public static final ObjectInfo	NULL_LITERAL		= createObjectInfo(null, NO_TYPE);

	public static List<ExecutableInfo> getAvailableExecutableInfos(TypeInfo declaringType, Executable executable) {
		return executable.isVarArgs()
			? Arrays.asList(new dd.kms.zenodot.impl.wrappers.RegularExecutableInfo(declaringType, executable), new dd.kms.zenodot.impl.wrappers.VariadicExecutableInfo(declaringType, executable))
			: Arrays.asList(new dd.kms.zenodot.impl.wrappers.RegularExecutableInfo(declaringType, executable));
	}

	public static ClassInfo createClassInfo(String qualifiedClassName) throws ClassNotFoundException {
		return new dd.kms.zenodot.impl.wrappers.ClassInfoImpl(dd.kms.zenodot.impl.utils.ClassUtils.normalizeClassName(qualifiedClassName));
	}

	public static ClassInfo createClassInfoUnchecked(String qualifiedClassName) {
		return new dd.kms.zenodot.impl.wrappers.ClassInfoImpl(qualifiedClassName);
	}

	public static PackageInfo createPackageInfo(String packageName) {
		return new dd.kms.zenodot.impl.wrappers.PackageInfoImpl(packageName);
	}

	public static ObjectInfo createObjectInfo(Object object) {
		return createObjectInfo(object, UNKNOWN_TYPE);
	}

	public static ObjectInfo createObjectInfo(Object object, TypeInfo declaredType) {
		return createObjectInfo(object, declaredType, null);
	}

	public static ObjectInfo createObjectInfo(Object object, TypeInfo declaredType, ObjectInfo.ValueSetter valueSetter) {
		return new dd.kms.zenodot.impl.wrappers.ObjectInfoImpl(object, declaredType, valueSetter);
	}

	public static FieldInfo createFieldInfo(TypeInfo declaringType, Field field) {
		return new dd.kms.zenodot.impl.wrappers.FieldInfoImpl(declaringType, field);
	}

	public static List<FieldInfo> getFieldInfos(TypeInfo declaringType, FieldScanner fieldScanner) {
		List<Field> fields = fieldScanner.getFields(declaringType.getRawType(), true);
		return fields.stream()
			.map(field -> createFieldInfo(declaringType, field))
			.collect(Collectors.toList());
	}

	public static List<ExecutableInfo> getMethodInfos(TypeInfo declaringType, MethodScanner methodScanner) {
		List<Method> methods = methodScanner.getMethods(declaringType.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(getAvailableExecutableInfos(declaringType, method));
		}
		return executableInfos;
	}

	public static List<ExecutableInfo> getConstructorInfos(TypeInfo declaringType, ConstructorScanner constructorScanner) {
		List<Constructor<?>> constructors = constructorScanner.getConstructors(declaringType.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(getAvailableExecutableInfos(declaringType, constructor));
		}
		return executableInfos;
	}

	public static TypeInfo createTypeInfo(Type type) {
		return type == null ? NO_TYPE : new dd.kms.zenodot.impl.wrappers.TypeInfoImpl(TypeToken.of(type));
	}
}