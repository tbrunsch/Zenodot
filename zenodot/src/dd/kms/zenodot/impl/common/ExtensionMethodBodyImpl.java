package dd.kms.zenodot.impl.common;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.common.ExtensionMethodBody;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.VariablesImpl;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public class ExtensionMethodBodyImpl implements ExtensionMethodBody
{
	private final Class<?>		declaringClass;
	private final Class<?>[]	parameterTypes;
	private final String[]		parameterNames;
	private final boolean		varArgs;
	private final String		implementationExpression;

	private final ParserToolbox	parserToolbox;

	private ObjectParseResult	bodyParseResult	= null;

	public ExtensionMethodBodyImpl(Class<?> declaringClass, Class<?>[] parameterTypes, String[] parameterNames, boolean varArgs, String implementationExpression, ParserToolbox parserToolbox) {
		this.declaringClass = declaringClass;
		this.parameterTypes = parameterTypes.clone();
		this.parameterNames = parameterNames.clone();
		this.varArgs = varArgs;
		this.implementationExpression = implementationExpression;
		this.parserToolbox = parserToolbox;
	}

	@Override
	public Object execute(Object obj, Object[] args) throws InvocationTargetException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(obj, declaringClass);

		VariablesImpl variables;
		try {
			variables = createParameterVariables(args);
		} catch (InternalErrorException | IllegalArgumentException e) {
			throw new InvocationTargetException(e, "Error executing extension method: " + e);
		}

		ObjectParseResult bodyParseResult = getBodyParseResult();
		try {
			return bodyParseResult.evaluate(thisInfo, thisInfo, variables).getObject();
		} catch (ParseException e) {
			throw new InvocationTargetException(e, "Error executing extension method: " + e);
		}
	}

	private synchronized ObjectParseResult getBodyParseResult() throws InvocationTargetException {
		if (bodyParseResult == null) {
			try {
				bodyParseResult = parseBody();
			} catch (InternalErrorException | SyntaxException | EvaluationException e) {
				throw new InvocationTargetException(e, "Error parsing body of extension method: " + e);
			}
		}
		return bodyParseResult;
	}

	private ObjectParseResult parseBody() throws InternalErrorException, SyntaxException, EvaluationException {
		int numParameters = parameterNames.length;
		VariablesImpl variables = new VariablesImpl(parserToolbox.getVariables());
		for (int i = 0; i < numParameters; i++) {
			ObjectInfo valueInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, parameterTypes[i]);
			variables.createVariable(parameterNames[i], valueInfo, false);
		}

		TokenStream tokenStream = new TokenStream(implementationExpression, -1);

		ParserToolbox parserToolBox = parserToolbox
			.withEvaluationMode(EvaluationMode.STATIC_TYPING)
			.withVariables(variables);
		AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> expressionParser = parserToolBox.createExpressionParser();

		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, declaringClass);
		try {
			return expressionParser.parse(tokenStream, thisInfo, new ObjectParseResultExpectation());
		} catch (CodeCompletionException e) {
			throw new IllegalStateException("Unexpected code completion when parsing body of extension method");
		}
	}

	private VariablesImpl createParameterVariables(Object[] values) throws InternalErrorException {
		int numActualParameters = values.length;
		int numParameters = parameterNames.length;

		if (varArgs) {
			Preconditions.checkArgument(numParameters >= 1, "A variadic method must have at least one parameter.");
			Preconditions.checkArgument(numActualParameters >= numParameters, "Invalid number of parameters: Expected: >= " + numParameters + ", actual: " + numActualParameters);
		} else {
			Preconditions.checkArgument(numActualParameters == numParameters, "Invalid number of parameters: Expected: " + numParameters + ", actual: " + numActualParameters);
		}

		int lastParamIndex = numParameters - 1;
		Class<?> lastParamType = parameterTypes[lastParamIndex];
		Preconditions.checkArgument(lastParamType.getComponentType() != null, "A variadic method must have an array type for the last parameter, but the type is " + lastParamType.getName());


		boolean collectLastParametersInArray = varArgs &&
			(numActualParameters > numParameters ||
			!lastParamType.isInstance(values[lastParamIndex]));
		int numPlainParameters = collectLastParametersInArray ? numParameters - 1 : numParameters;

		VariablesImpl variables = new VariablesImpl(parserToolbox.getVariables());
		for (int i = 0; i < numPlainParameters; i++) {
			ObjectInfo valueInfo = InfoProvider.createObjectInfo(values[i], parameterTypes[i]);
			variables.createVariable(parameterNames[i], valueInfo, false);
		}

		int numParametersToCollectInArray = numActualParameters - numPlainParameters;
		if (numParametersToCollectInArray > 0) {
			Object parameterArray = Array.newInstance(lastParamType, numParametersToCollectInArray);
			for (int i = 0; i < numParametersToCollectInArray; i++) {
				Array.set(parameterArray, i, values[i + numPlainParameters]);
			}
			ObjectInfo valueInfo = InfoProvider.createObjectInfo(parameterArray, lastParamType);
			variables.createVariable(parameterNames[lastParamIndex], valueInfo, false);
		}

		return variables;
	}
}
