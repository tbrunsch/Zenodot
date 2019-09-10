package dd.kms.zenodot.utils.dataProviders;

import com.google.common.primitives.Primitives;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.wrappers.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Central utility class that evaluates certain constructs in terms of {@link ObjectInfo}s
 * taking the {@link EvaluationMode} into account.
 */
public class ObjectInfoProvider
{
	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public TypeInfo getType(Object object, TypeInfo declaredType) {
		if (object == null || object == InfoProvider.INDETERMINATE_VALUE) {
			return declaredType == InfoProvider.UNKNOWN_TYPE ? InfoProvider.NO_TYPE : declaredType;
		}

		Class<?> runtimeClass = object.getClass();
		if (declaredType == InfoProvider.UNKNOWN_TYPE) {
			return InfoProvider.createTypeInfo(runtimeClass);
		}

		if (evaluationMode == EvaluationMode.DYNAMICALLY_TYPED) {
			return declaredType.isPrimitive() ? declaredType : declaredType.getSubtype(runtimeClass);
		} else {
			return declaredType;
		}
	}

	public TypeInfo getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public ObjectInfo getFieldValueInfo(Object contextObject, FieldInfo fieldInfo) {
		Object fieldValue = InfoProvider.INDETERMINATE_VALUE;
		if (evaluationMode.isEvaluateValues()) {
			try {
				fieldValue = fieldInfo.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		ObjectInfo.ValueSetter valueSetter = getFieldValueSetter(contextObject, fieldInfo);
		return InfoProvider.createObjectInfo(fieldValue, fieldInfo.getType(), valueSetter);
	}

	private ObjectInfo.ValueSetter getFieldValueSetter(Object contextObject, FieldInfo fieldInfo) {
		if (fieldInfo.isFinal()) {
			return null;
		}
		return value -> {
			try {
				fieldInfo.set(contextObject, value);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Could not set field value", e);
			}
		};
	}

	public ObjectInfo getExecutableReturnInfo(Object contextObject, ExecutableInfo executableInfo, List<ObjectInfo> argumentInfos) throws InvocationTargetException, InstantiationException {
		final Object methodReturnValue;
		if (evaluationMode.isEvaluateValues()) {
			Object[] arguments = executableInfo.createArgumentArray(argumentInfos);
			try {
				methodReturnValue = executableInfo.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
		} else {
			methodReturnValue = InfoProvider.INDETERMINATE_VALUE;
		}
		TypeInfo methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
		return InfoProvider.createObjectInfo(methodReturnValue, methodReturnType);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) {
		final Object arrayElementValue;
		final ObjectInfo.ValueSetter valueSetter;
		if (evaluationMode.isEvaluateValues()) {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
			valueSetter = value -> Array.set(arrayObject, index, value);
		} else {
			arrayElementValue = InfoProvider.INDETERMINATE_VALUE;
			valueSetter = null;
		}
		TypeInfo arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return InfoProvider.createObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, ObjectInfo sizeInfo) {
		final int size;
		if (evaluationMode.isEvaluateValues()) {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		} else {
			size = 0;
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (evaluationMode.isEvaluateValues()) {
			Class<?> componentClass = componentType.getRawType();
			Object arrayObject = arrayInfo.getObject();
			for (int i = 0; i < size; i++) {
				Object element = elementInfos.get(i).getObject();
				Array.set(arrayObject, i, ReflectionUtils.convertTo(element, componentClass, false));
			}
		}
		return arrayInfo;
	}

	private ObjectInfo getArrayInfo(TypeInfo componentType, int size) {
		Class<?> componentClass = componentType.getRawType();
		int sizeToAllocate = evaluationMode.isEvaluateValues() ? size : 0;
		Object array = Array.newInstance(componentClass, sizeToAllocate);
		Class<?> arrayClass = array.getClass();
		TypeInfo arrayType = InfoProvider.createTypeInfo(arrayClass);
		Object arrayObject = evaluationMode.isEvaluateValues() ? array : InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeInfo targetType) throws ClassCastException {
		Object castedValue = evaluationMode.isEvaluateValues()
								? ReflectionUtils.convertTo(objectInfo.getObject(), targetType.getRawType(), true)
								: InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(castedValue, targetType);
	}
}
