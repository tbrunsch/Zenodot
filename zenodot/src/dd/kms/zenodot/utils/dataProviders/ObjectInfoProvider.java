package dd.kms.zenodot.utils.dataProviders;

import com.google.common.primitives.Primitives;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
		if (object == null) {
			return declaredType == TypeInfo.UNKNOWN ? TypeInfo.NONE : declaredType;
		}

		Class<?> runtimeClass = object.getClass();
		if (declaredType == TypeInfo.UNKNOWN) {
			return TypeInfo.of(runtimeClass);
		}

		if (evaluationMode == EvaluationMode.DYNAMICALLY_TYPED) {
			if (declaredType.isPrimitive()) {
				return declaredType;
			}
			TypeInfo runtimeType = declaredType.getSubtype(runtimeClass);
			return declaredType.isPrimitive()
					? TypeInfo.of(Primitives.unwrap(runtimeType.getRawType()))
					: runtimeType;
		} else {
			return declaredType;
		}
	}

	public TypeInfo getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public ObjectInfo getFieldValueInfo(Object contextObject, FieldInfo fieldInfo) {
		Object fieldValue = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
			try {
				fieldValue = fieldInfo.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		ObjectInfo.ValueSetter valueSetter = getFieldValueSetter(contextObject, fieldInfo);
		return new ObjectInfo(fieldValue, fieldInfo.getType(), valueSetter);
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

	public ObjectInfo getExecutableReturnInfo(Object contextObject, AbstractExecutableInfo executableInfo, List<ObjectInfo> argumentInfos) throws InvocationTargetException, InstantiationException {
		final Object methodReturnValue;
		if (evaluationMode == EvaluationMode.NONE) {
			methodReturnValue = ObjectInfo.INDETERMINATE;
		} else {
			Object[] arguments = executableInfo.createArgumentArray(argumentInfos);
			try {
				methodReturnValue = executableInfo.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
		}
		TypeInfo methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
		return new ObjectInfo(methodReturnValue, methodReturnType);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) {
		final Object arrayElementValue;
		final ObjectInfo.ValueSetter valueSetter;
		if (evaluationMode == EvaluationMode.NONE) {
			arrayElementValue = ObjectInfo.INDETERMINATE;
			valueSetter = null;
		} else {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
			valueSetter = value -> Array.set(arrayObject, index, value);
		}
		TypeInfo arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, ObjectInfo sizeInfo) {
		final int size;
		if (evaluationMode == EvaluationMode.NONE) {
			size = 0;
		} else {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (evaluationMode != EvaluationMode.NONE) {
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
		Object array = Array.newInstance(componentClass, size);
		Class<?> arrayClass = array.getClass();
		TypeInfo arrayType = TypeInfo.of(arrayClass);
		Object arrayObject = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : array;
		return new ObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeInfo targetType) throws ClassCastException {
		Object castedValue = evaluationMode == EvaluationMode.NONE
								? ObjectInfo.INDETERMINATE
								: ReflectionUtils.convertTo(objectInfo.getObject(), targetType.getRawType(), true);
		return new ObjectInfo(castedValue, targetType);
	}

	public ObjectInfo getVariableInfo(Variable variable) {
		Object value = variable.getValue();
		return new ObjectInfo(value, TypeInfo.UNKNOWN);
	}
}
