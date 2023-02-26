package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.wrappers.ExecutableInfo;
import dd.kms.zenodot.impl.wrappers.FieldInfo;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ObjectInfoProvider
{
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

	public Class<?> getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public Class<?> getType(Object object, Class<?> declaredType) {
		if (!isEvaluate()) {
			return declaredType;
		}
		return object == null || object == InfoProvider.INDETERMINATE_VALUE || (declaredType != null && declaredType.isPrimitive())
			? declaredType
			: object.getClass();
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
				fieldInfo.set(contextObject, value.getObject());
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
			valueSetter = value -> Array.set(arrayObject, index, value.getObject());
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

	public ObjectInfo getLambdaInfo(Class<?> functionalInterface, String methodName, List<Parameter> parameters, ObjectParseResult lambdaBodyParseResult, String lambdaStringRepresentation, ObjectInfo thisInfo, VariablesImpl variables) throws InternalErrorException {
		Object lambda = isEvaluateWithSideEffects()
			? createLambda(functionalInterface, methodName, parameters, lambdaBodyParseResult, lambdaStringRepresentation, thisInfo, variables)
			: InfoProvider.INDETERMINATE_VALUE;
		return InfoProvider.createObjectInfo(lambda, functionalInterface);
	}

	private Object createLambda(Class<?> functionalInterface, String methodName, List<Parameter> parameters, ObjectParseResult lambdaBodyParseResult, String lambdaStringRepresentation, ObjectInfo thisInfo, VariablesImpl variables) throws InternalErrorException {
		List<String> parameterNames = parameters.stream().map(Parameter::getName).collect(Collectors.toList());
		List<Class<?>> parameterTypes = parameters.stream().map(Parameter::getDeclaredType).collect(Collectors.toList());

		LambdaEvaluator targetMethodEvaluator = new LambdaEvaluator(thisInfo, parameterNames, variables, lambdaBodyParseResult);
		LambdaInvocationHandler invocationHandler = new LambdaInvocationHandler(functionalInterface, methodName, parameterTypes, targetMethodEvaluator, lambdaStringRepresentation);

		return Proxy.newProxyInstance(
			functionalInterface.getClassLoader(),
			new Class<?>[]{ functionalInterface },
			invocationHandler
		);
	}

	private static class LambdaEvaluator
	{
		private final ObjectInfo			thisInfo;
		private final List<String>			parameterNames;
		private final VariablesImpl			variables;
		private final ObjectParseResult		lambdaBodyParseResult;

		private LambdaEvaluator(ObjectInfo thisInfo, List<String> parameterNames, VariablesImpl variables, ObjectParseResult lambdaBodyParseResult) throws InternalErrorException {
			this.thisInfo = thisInfo;
			this.parameterNames = parameterNames;
			this.lambdaBodyParseResult = lambdaBodyParseResult;

			// create new scope for variables
			this.variables = new VariablesImpl(variables);
			for (String parameterName : parameterNames) {
				this.variables.createVariable(parameterName, InfoProvider.NULL_LITERAL, false);
			}
		}

		Object apply(Object[] objects) throws ParseException {
			setVariables(objects);
			ObjectInfo lambdaReturnInfo = lambdaBodyParseResult.evaluate(thisInfo, thisInfo, variables);
			return lambdaReturnInfo.getObject();
		}

		private void setVariables(Object[] objects) {
			if (objects == null) {
				if (parameterNames.isEmpty()) {
					return;
				}
				throw new IllegalStateException("Lambda requires parameters " + parameterNames + ", but got none");
			}
			int numParameters = parameterNames.size();
			if (objects.length != numParameters) {
				throw new IllegalStateException("Lambda requires parameters " + parameterNames + ", but got " + objects.length + " parameters");
			}
			for (int i = 0; i < numParameters; i++) {
				ObjectInfo variable = variables.getValueInfo(parameterNames.get(i));
				ObjectInfo.ValueSetter valueSetter = variable.getValueSetter();
				ObjectInfo valueInfo = InfoProvider.createObjectInfo(objects[i]);
				valueSetter.setObjectInfo(valueInfo);
			}
		}
	}

	private static class LambdaInvocationHandler implements InvocationHandler
	{
		private final Class<?>						functionalInterface;
		private final String						targetMethodName;
		private final List<Class<?>>				targetMethodTypes;
		private final LambdaEvaluator				targetMethodEvaluator;
		private final String						stringRepresentation;

		LambdaInvocationHandler(Class<?> functionalInterface, String targetMethodName, List<Class<?>> targetMethodTypes, LambdaEvaluator targetMethodEvaluator, String stringRepresentation) {
			this.functionalInterface = functionalInterface;
			this.targetMethodName = targetMethodName;
			this.targetMethodTypes = targetMethodTypes;
			this.targetMethodEvaluator = targetMethodEvaluator;
			this.stringRepresentation = stringRepresentation;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (isTargetMethod(method, args)) {
				return targetMethodEvaluator.apply(args);
			}
			String methodName = method.getName();
			switch (methodName) {
				case "toString": {
					if (args == null || args.length == 0) {
						return stringRepresentation;
					}
					break;
				}
				case "equals": {
					if (args != null && args.length == 1) {
						Object arg = args[0];
						return proxy == arg;
					}
					break;
				}
				case "hashCode": {
					if (args == null || args.length == 0) {
						return System.identityHashCode(proxy);
					}
					break;
				}
				default:
					break;
			}
			throw new IllegalStateException("Unexpected method " + methodName + "() of functional interface " + functionalInterface);
		}

		private boolean isTargetMethod(Method method, Object[] args) {
			String methodName = method.getName();
			if (!Objects.equals(methodName, targetMethodName)) {
				return false;
			}
			if (targetMethodTypes.isEmpty()) {
				return args == null || args.length == 0;
			}
			int numParameters = targetMethodTypes.size();
			if (args.length != numParameters) {
				return false;
			}
			for (int i = 0; i < numParameters; i++) {
				Class<?> targetParameterType = targetMethodTypes.get(i);
				Object arg = args[i];
				if (arg == null) {
					if (targetParameterType.isPrimitive()) {
						return false;
					}
				} else {
					Class<?> argClass = arg.getClass();
					if (MatchRatings.rateTypeMatch(targetParameterType, argClass) == TypeMatch.NONE) {
						return false;
					}
				}
			}
			return true;
		}
	}

	public static class Parameter
	{
		private final String	name;
		private final Class<?>	declaredType;

		public Parameter(String name, Class<?> declaredType) {
			this.name = name;
			this.declaredType = declaredType;
		}

		public String getName() {
			return name;
		}

		public Class<?> getDeclaredType() {
			return declaredType;
		}
	}
}
