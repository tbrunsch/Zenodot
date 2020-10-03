package dd.kms.zenodot.api.wrappers;

import com.google.common.reflect.TypeToken;
import dd.kms.zenodot.api.common.ConstructorScanner;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.MethodScanner;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoProvider
{
	public static final Object 		INDETERMINATE_VALUE = new Object();

	public static final TypeInfo	NO_TYPE				= new dd.kms.zenodot.impl.wrappers.TypeInfoImpl(null);

	public static final ObjectInfo	NULL_LITERAL		= createObjectInfo(null, NO_TYPE);

	public static List<ExecutableInfo> getAvailableExecutableInfos(TypeInfo type, Executable executable) {
		Class<?> declaringClass = executable.getDeclaringClass();
		TypeInfo declaringType = type.resolveType(declaringClass);
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
		return createObjectInfo(object, object == null ? NO_TYPE : createTypeInfo(object.getClass()));
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

	public static List<FieldInfo> getFieldInfos(TypeInfo type, FieldScanner fieldScanner) {
		List<Field> fields = fieldScanner.getFields(type.getRawType());
		List<FieldInfo> fieldInfos = new ArrayList<>(fields.size());
		for (Field field : fields) {
			Class<?> declaringClass = field.getDeclaringClass();
			TypeInfo declaringType = type.resolveType(declaringClass);
			FieldInfo fieldInfo = createFieldInfo(declaringType, field);
			fieldInfos.add(fieldInfo);
		}
		return fieldInfos;
	}

	public static List<ExecutableInfo> getMethodInfos(TypeInfo type, MethodScanner methodScanner) {
		List<Method> methods = methodScanner.getMethods(type.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(getAvailableExecutableInfos(type, method));
		}
		return executableInfos;
	}

	public static List<ExecutableInfo> getConstructorInfos(TypeInfo type, ConstructorScanner constructorScanner) {
		List<Constructor<?>> constructors = constructorScanner.getConstructors(type.getRawType());
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(getAvailableExecutableInfos(type, constructor));
		}
		return executableInfos;
	}

	public static TypeInfo createTypeInfo(Type type) {
		return type == null ? NO_TYPE : new dd.kms.zenodot.impl.wrappers.TypeInfoImpl(TypeToken.of(type));
	}
}