package dd.kms.zenodot.impl.utils.dataproviders;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.wrappers.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ObjectInfoProvider
{
	private final boolean evaluate;

	public ObjectInfoProvider(boolean evaluate) {
		this.evaluate = evaluate;
	}

	public TypeInfo getType(Object object, TypeInfo declaredType) {
		if (object == null || object == InfoProvider.INDETERMINATE_VALUE) {
			return declaredType;
		}

		if (!evaluate || declaredType.isPrimitive()) {
			return declaredType;
		}

		Class<?> runtimeClass = object.getClass();
		try {
			return declaredType.getSubtype(runtimeClass);
		} catch (Throwable t) {
			return InfoProvider.createTypeInfo(runtimeClass);
		}
	}

	public TypeInfo getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public ObjectInfo getFieldValueInfo(Object contextObject, FieldInfo fieldInfo) {
		Object fieldValue = InfoProvider.INDETERMINATE_VALUE;
		if (evaluate) {
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
		if (evaluate) {
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
		if (evaluate) {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
			valueSetter = value -> Array.set(arrayObject, index, value);
		}
		TypeInfo arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return InfoProvider.createObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, ObjectInfo sizeInfo) {
		int size = 0;
		if (evaluate) {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (evaluate) {
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
		int sizeToAllocate = evaluate ? size : 0;
		Object array = Array.newInstance(componentClass, sizeToAllocate);
		Class<?> arrayClass = array.getClass();
		TypeInfo arrayType = InfoProvider.createTypeInfo(arrayClass);
		Object arrayObject = evaluate ? array : InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeInfo targetType) throws ClassCastException {
		Object castedValue = evaluate
								? ReflectionUtils.convertTo(objectInfo.getObject(), targetType.getRawType(), true)
								: InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(castedValue, targetType);
	}
}
