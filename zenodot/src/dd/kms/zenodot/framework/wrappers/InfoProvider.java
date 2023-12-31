package dd.kms.zenodot.framework.wrappers;

import dd.kms.zenodot.api.common.*;
import dd.kms.zenodot.impl.common.GeneralizedConstructorImpl;
import dd.kms.zenodot.impl.common.GeneralizedMethodImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoProvider
{
	public static final Object 		INDETERMINATE_VALUE = new Object();

	public static final Class<?>	NO_TYPE				= null;

	public static final ObjectInfo NULL_LITERAL		= createObjectInfo(null, NO_TYPE);

	public static List<ExecutableInfo> getAvailableExecutableInfos(GeneralizedExecutable executable) {
		return executable.isVarArgs()
			? Arrays.asList(new RegularExecutableInfo(executable), new VariadicExecutableInfo(executable))
			: Arrays.asList(new RegularExecutableInfo(executable));
	}

	public static ClassInfo createClassInfo(Class<?> clazz) {
		return createClassInfoUnchecked(clazz.getName());
	}

	public static ClassInfo createClassInfoUnchecked(String qualifiedClassName) {
		return new ClassInfo(qualifiedClassName);
	}

	public static ObjectInfo createObjectInfo(Object object) {
		return createObjectInfo(object, object == null ? NO_TYPE : object.getClass());
	}

	public static ObjectInfo createObjectInfo(Object object, Class<?> declaredType) {
		return createObjectInfo(object, declaredType, null);
	}

	public static ObjectInfo createObjectInfo(Object object, Class<?> declaredType, ObjectInfo.ValueSetter valueSetter) {
		return new ObjectInfo(object, declaredType, valueSetter);
	}

	public static FieldInfo createFieldInfo(GeneralizedField field) {
		return new FieldInfo(field);
	}

	public static List<FieldInfo> getFieldInfos(Class<?> type, FieldScanner fieldScanner) {
		List<GeneralizedField> fields = fieldScanner.getFields(type);
		List<FieldInfo> fieldInfos = new ArrayList<>(fields.size());
		for (GeneralizedField field : fields) {
			FieldInfo fieldInfo = createFieldInfo(field);
			fieldInfos.add(fieldInfo);
		}
		return fieldInfos;
	}

	public static List<ExecutableInfo> getMethodInfos(Class<?> type, MethodScanner methodScanner) {
		List<Method> methods = methodScanner.getMethods(type);
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			GeneralizedMethod generalizedMethod = new GeneralizedMethodImpl(method);
			executableInfos.addAll(getAvailableExecutableInfos(generalizedMethod));
		}
		return executableInfos;
	}

	public static List<ExecutableInfo> getConstructorInfos(Class<?> type, ConstructorScanner constructorScanner) {
		List<Constructor<?>> constructors = constructorScanner.getConstructors(type);
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			GeneralizedConstructor generalizedConstructor = new GeneralizedConstructorImpl(constructor);
			executableInfos.addAll(getAvailableExecutableInfos(generalizedConstructor));
		}
		return executableInfos;
	}
}