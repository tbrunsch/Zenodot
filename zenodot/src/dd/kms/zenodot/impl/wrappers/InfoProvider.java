package dd.kms.zenodot.impl.wrappers;

import dd.kms.zenodot.api.common.ConstructorScanner;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.MethodScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoProvider
{
	public static final Object 		INDETERMINATE_VALUE = new Object();

	public static final Class<?>	NO_TYPE				= null;

	public static final ObjectInfo	NULL_LITERAL		= createObjectInfo(null, NO_TYPE);

	public static List<ExecutableInfo> getAvailableExecutableInfos(Executable executable) {
		return executable.isVarArgs()
			? Arrays.asList(new dd.kms.zenodot.impl.wrappers.RegularExecutableInfo(executable), new dd.kms.zenodot.impl.wrappers.VariadicExecutableInfo(executable))
			: Arrays.asList(new dd.kms.zenodot.impl.wrappers.RegularExecutableInfo(executable));
	}

	public static ClassInfo createClassInfo(Class<?> clazz) {
		return createClassInfoUnchecked(clazz.getName());
	}

	public static ClassInfo createClassInfo(String qualifiedClassName) throws ClassNotFoundException {
		return new dd.kms.zenodot.impl.wrappers.ClassInfoImpl(dd.kms.zenodot.impl.utils.ClassUtils.normalizeClassName(qualifiedClassName));
	}

	public static ClassInfo createClassInfoUnchecked(String qualifiedClassName) {
		return new dd.kms.zenodot.impl.wrappers.ClassInfoImpl(qualifiedClassName);
	}

	public static ObjectInfo createObjectInfo(Object object) {
		return createObjectInfo(object, object == null ? NO_TYPE : object.getClass());
	}

	public static ObjectInfo createObjectInfo(Object object, Class<?> declaredType) {
		return createObjectInfo(object, declaredType, null);
	}

	public static ObjectInfo createObjectInfo(Object object, Class<?> declaredType, ObjectInfo.ValueSetter valueSetter) {
		return new dd.kms.zenodot.impl.wrappers.ObjectInfoImpl(object, declaredType, valueSetter);
	}

	public static FieldInfo createFieldInfo(Field field) {
		return new dd.kms.zenodot.impl.wrappers.FieldInfoImpl(field);
	}

	public static List<FieldInfo> getFieldInfos(Class<?> type, FieldScanner fieldScanner) {
		List<Field> fields = fieldScanner.getFields(type);
		List<FieldInfo> fieldInfos = new ArrayList<>(fields.size());
		for (Field field : fields) {
			FieldInfo fieldInfo = createFieldInfo(field);
			fieldInfos.add(fieldInfo);
		}
		return fieldInfos;
	}

	public static List<ExecutableInfo> getMethodInfos(Class<?> type, MethodScanner methodScanner) {
		List<Method> methods = methodScanner.getMethods(type);
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(getAvailableExecutableInfos(method));
		}
		return executableInfos;
	}

	public static List<ExecutableInfo> getConstructorInfos(Class<?> type, ConstructorScanner constructorScanner) {
		List<Constructor<?>> constructors = constructorScanner.getConstructors(type);
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(getAvailableExecutableInfos(constructor));
		}
		return executableInfos;
	}
}