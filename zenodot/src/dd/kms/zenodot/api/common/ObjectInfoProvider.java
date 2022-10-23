package dd.kms.zenodot.api.common;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.impl.wrappers.ExecutableInfo;
import dd.kms.zenodot.impl.wrappers.FieldInfo;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ObjectInfoProvider
{
	public static Class<?> getRuntimeType(Object object, Class<?> declaredType) {
		return object == null || object == InfoProvider.INDETERMINATE_VALUE || (declaredType != null && declaredType.isPrimitive())
			? declaredType
			: object.getClass();
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

	public Class<?> getType(Object object, Class<?> declaredType) {
		return isEvaluate() ? getRuntimeType(object, declaredType) : declaredType;
	}

	public Class<?> getType(ObjectInfo objectInfo) {
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
		Class<?> methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
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
		Class<?> arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return InfoProvider.createObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(Class<?> componentType, ObjectInfo sizeInfo) {
		int size = 0;
		if (isEvaluate() && sizeInfo != InfoProvider.INDETERMINATE_VALUE) {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(Class<?> componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (isEvaluate()) {
			Object arrayObject = arrayInfo.getObject();
			for (int i = 0; i < size; i++) {
				Object element = elementInfos.get(i).getObject();
				Array.set(arrayObject, i, ReflectionUtils.convertTo(element, componentType, false));
			}
		}
		return arrayInfo;
	}

	private ObjectInfo getArrayInfo(Class<?> componentType, int size) {
		int sizeToAllocate = isEvaluate() ? size : 0;
		Object array = Array.newInstance(componentType, sizeToAllocate);
		Class<?> arrayClass = array.getClass();
		Object arrayObject = isEvaluate() ? array : InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(arrayObject, arrayClass);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, Class<?> targetType) throws ClassCastException {
		Object object = objectInfo.getObject();
		Object castedValue = isEvaluate() && object != InfoProvider.INDETERMINATE_VALUE
								? ReflectionUtils.convertTo(object, targetType, true)
								: InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(castedValue, targetType);
	}
}
