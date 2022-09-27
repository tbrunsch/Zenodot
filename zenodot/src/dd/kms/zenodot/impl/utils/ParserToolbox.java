package dd.kms.zenodot.impl.utils;

import dd.kms.zenodot.api.common.ObjectInfoProvider;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.InternalLogger;
import dd.kms.zenodot.impl.parsers.AbstractParser;
import dd.kms.zenodot.impl.parsers.ExpressionParser;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.utils.dataproviders.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class used by most parsers to access other parsers, data providers,
 * and the parser settings.
 */
public class ParserToolbox
{
	private final ObjectInfo					thisInfo;
	private final ParserSettings				settings;

	private final InternalLogger				logger;

	private final ClassDataProvider				classDataProvider;
	private final ExecutableDataProvider		executableDataProvider;
	private final FieldDataProvider				fieldDataProvider;
	private final ObjectInfoProvider objectInfoProvider;
	private final ObjectTreeNodeDataProvider	objectTreeNodeDataProvider;
	private final OperatorResultProvider 		operatorResultProvider;
	private final VariableDataProvider			variableDataProvider;

	public ParserToolbox(ObjectInfo thisInfo, ParserSettings settings) {
		this.thisInfo = thisInfo;
		this.settings = settings;

		logger	= new InternalLogger(settings.getLogger());

		EvaluationMode evaluationMode = settings.getEvaluationMode();

		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);

		classDataProvider				= new ClassDataProvider(this);
		executableDataProvider			= new ExecutableDataProvider(this);
		fieldDataProvider				= new FieldDataProvider(this);
		objectTreeNodeDataProvider		= new ObjectTreeNodeDataProvider();
		operatorResultProvider 			= new OperatorResultProvider(objectInfoProvider, evaluationMode);
		variableDataProvider			= new VariableDataProvider(settings.getVariables(), objectInfoProvider);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
	}

	public InternalLogger getLogger() {
		return logger;
	}

	/*
	 * Data Providers
	 */
	public ClassDataProvider getClassDataProvider() {
		return classDataProvider;
	}

	public ExecutableDataProvider getExecutableDataProvider() {
		return executableDataProvider;
	}

	public FieldDataProvider getFieldDataProvider() {
		return fieldDataProvider;
	}

	public ObjectInfoProvider getObjectInfoProvider() {
		return objectInfoProvider;
	}

	public ObjectTreeNodeDataProvider getObjectTreeNodeDataProvider() {
		return objectTreeNodeDataProvider;
	}

	public OperatorResultProvider getOperatorResultProvider() {
		return operatorResultProvider;
	}

	public VariableDataProvider getVariableDataProvider() {
		return variableDataProvider;
	}

	/*
	 * Parsers
	 */
	public <C, T extends ParseResult, S extends ParseResultExpectation<T>, P extends AbstractParser<C, T, S>> P createParser(Class<P> parserClass) throws InternalErrorException {
		try {
			Constructor<P> constructor = parserClass.getConstructor(ParserToolbox.class);
			return constructor.newInstance(this);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new InternalErrorException("Creating parser failed: " + e.getMessage(), e);
		}
	}

	public AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> createExpressionParser() {
		return createExpressionParser(OperatorResultProvider.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
	}

	public AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> createExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new ExpressionParser(this, maxOperatorPrecedenceLevelToConsider);
	}
}
