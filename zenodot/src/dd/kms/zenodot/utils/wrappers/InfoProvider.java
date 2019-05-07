package dd.kms.zenodot.utils.wrappers;

import com.google.common.reflect.TypeToken;
import dd.kms.zenodot.utils.ClassUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class InfoProvider
{
	public static final Object 		INDETERMINATE_VALUE = new Object();

	public static final TypeInfo	NO_TYPE				= new TypeInfoImpl(null);
	public static final TypeInfo	UNKNOWN_TYPE		= new TypeInfoImpl(null);


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

	public static FieldInfo createFieldInfo(Field field, TypeInfo declaringType) {
		return new FieldInfoImpl(field, declaringType);
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


}