package dd.kms.zenodot.api.common;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.wrappers.*;

public class ObjectInfoProvider
{
	public static TypeInfo getRuntimeType(Object object, TypeInfo declaredType) {
		if (object == null || object == InfoProvider.INDETERMINATE_VALUE) {
			return declaredType;
		}
		return getRuntimeType(object.getClass(), declaredType);
	}

	public static TypeInfo getRuntimeType(Class<?> runtimeClass, TypeInfo declaredType) {
		if (declaredType.isPrimitive()) {
			return declaredType;
		}
		try {
			return declaredType.getSubtype(runtimeClass);
		} catch (Throwable t) {
			return InfoProvider.createTypeInfo(runtimeClass);
		}
	}

	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	private boolean isEvaluate() {
		return evaluationMode != EvaluationMode.STATIC_TYPING;
	}

	private boolean isEvaluateWithSideEffects() {
		return evaluationMode == EvaluationMode.DYNAMIC_TYPING;
	}

	public TypeInfo getType(Object object, TypeInfo declaredType) {
		return isEvaluate() ? getRuntimeType(object, declaredType) : declaredType;
	}

	public TypeInfo getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public ObjectInfo getFieldValueInfo(Object contextObject, FieldInfo fieldInfo) {
		Object fieldValue = InfoProvider.INDETERMINATE_VALUE;
		if (isEvaluate() && contextObject != InfoProvider.INDETERMINATE_VALUE) {
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
		Object methodReturnValue = InfoProvider.INDETERMINATE_VALUE;
		if (isEvaluateWithSideEffects() && contextObject != InfoProvider.INDETERMINATE_VALUE) {
			Object[] arguments = executableInfo.createArgumentArray(argumentInfos);
			try {
				methodReturnValue = executableInfo.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
		}
		TypeInfo methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
		return InfoProvider.createObjectInfo(methodReturnValue, methodReturnType);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) {
		Object arrayElementValue = InfoProvider.INDETERMINATE_VALUE;
		ObjectInfo.ValueSetter valueSetter = null;
		Object arrayObject = arrayInfo.getObject();
		Object indexObject = indexInfo.getObject();
		if (isEvaluate() && arrayObject != InfoProvider.INDETERMINATE_VALUE && indexInfo != InfoProvider.INDETERMINATE_VALUE) {
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
			valueSetter = value -> Array.set(arrayObject, index, value);
		}
		TypeInfo arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return InfoProvider.createObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, ObjectInfo sizeInfo) {
		int size = 0;
		if (isEvaluate() && sizeInfo != InfoProvider.INDETERMINATE_VALUE) {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (isEvaluate()) {
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
		int sizeToAllocate = isEvaluate() ? size : 0;
		Object array = Array.newInstance(componentClass, sizeToAllocate);
		Class<?> arrayClass = array.getClass();
		TypeInfo arrayType = InfoProvider.createTypeInfo(arrayClass);
		Object arrayObject = isEvaluate() ? array : InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeInfo targetType) throws ClassCastException {
		Object object = objectInfo.getObject();
		Object castedValue = isEvaluate() && object != InfoProvider.INDETERMINATE_VALUE
								? ReflectionUtils.convertTo(object, targetType.getRawType(), true)
								: InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(castedValue, targetType);
	}
}
