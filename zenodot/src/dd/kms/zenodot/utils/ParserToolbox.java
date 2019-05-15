package dd.kms.zenodot.utils;

import dd.kms.zenodot.parsers.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.utils.dataProviders.*;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Utility class used by most parsers to access other parsers, data providers,
 * and the parser settings.
 */
public class ParserToolbox
{
	private final ObjectInfo						thisInfo;
	private final ParserSettings					settings;

	private final ClassDataProvider					classDataProvider;
	private final ExecutableDataProvider			executableDataProvider;
	private final FieldDataProvider					fieldDataProvider;
	private final InspectionDataProvider			inspectionDataProvider;
	private final ObjectInfoProvider				objectInfoProvider;
	private final ObjectTreeNodeDataProvider		objectTreeNodeDataProvider;
	private final OperatorResultProvider 			operatorResultProvider;
	private final VariableDataProvider				variableDataProvider;

	private final AbstractEntityParser<ObjectInfo>	castParser;
	private final AbstractEntityParser<TypeInfo>	classFieldParser;
	private final AbstractEntityParser<TypeInfo>	classMethodParser;
	private final AbstractEntityParser<TypeInfo>	classObjectParser;
	private final AbstractEntityParser<ObjectInfo>	classParser;
	private final AbstractEntityParser<TypeInfo>	classTailParser;
	private final AbstractEntityParser<ObjectInfo>	constructorParser;
	private final AbstractEntityParser<ObjectInfo>	customHierarchyParser;
	private final AbstractEntityParser<ObjectInfo>	expressionParser;
	private final AbstractEntityParser<TypeInfo>	innerClassParser;
	private final AbstractEntityParser<ObjectInfo>	literalParser;
	private final AbstractEntityParser<ObjectInfo>	objectFieldParser;
	private final AbstractEntityParser<ObjectInfo>	objectMethodParser;
	private final AbstractEntityParser<ObjectInfo>	objectTailParser;
	private final AbstractEntityParser<ObjectInfo>	parenthesizedExpressionParser;
	private final AbstractEntityParser<ObjectInfo>	simpleExpressionParser;
	private final AbstractEntityParser<ObjectInfo>	unaryPrefixOperatorParser;
	private final AbstractEntityParser<ObjectInfo>	variableParser;

	public ParserToolbox(ObjectInfo thisInfo, ParserSettings settings, ParseMode parseMode) {
		this.thisInfo = thisInfo;
		this.settings = settings;

		EvaluationMode evaluationMode = getEvaluationMode(settings, parseMode);

		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);

		classDataProvider				= new ClassDataProvider(this);
		executableDataProvider			= new ExecutableDataProvider(this);
		fieldDataProvider				= new FieldDataProvider(this);
		inspectionDataProvider 			= new InspectionDataProvider(this);
		objectTreeNodeDataProvider		= new ObjectTreeNodeDataProvider();
		operatorResultProvider 			= new OperatorResultProvider(this, evaluationMode);
		variableDataProvider			= new VariableDataProvider(settings.getVariables());

		castParser						= new CastParser(this, thisInfo);
		classFieldParser				= new ClassFieldParser(this, thisInfo);
		classMethodParser				= new ClassMethodParser(this, thisInfo);
		classObjectParser				= new ClassObjectParser(this, thisInfo);
		classParser						= new ClassParser(this, thisInfo);
		classTailParser					= new ClassTailParser(this, thisInfo);
		constructorParser				= new ConstructorParser(this, thisInfo);
		customHierarchyParser			= new CustomHierarchyParser(this, thisInfo);
		expressionParser				= createExpressionParser(OperatorResultProvider.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
		innerClassParser				= new InnerClassParser(this, thisInfo);
		literalParser					= new LiteralParser(this, thisInfo);
		objectFieldParser				= new ObjectFieldParser(this, thisInfo);
		objectMethodParser				= new ObjectMethodParser(this, thisInfo);
		objectTailParser				= new ObjectTailParser(this, thisInfo);
		parenthesizedExpressionParser	= new ParenthesizedExpressionParser(this, thisInfo);
		simpleExpressionParser			= new SimpleExpressionParser(this, thisInfo);
		unaryPrefixOperatorParser		= new UnaryPrefixOperatorParser(this, thisInfo);
		variableParser					= new VariableParser(this, thisInfo);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
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

	public InspectionDataProvider getInspectionDataProvider() {
		return inspectionDataProvider;
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
	public AbstractEntityParser<ObjectInfo> getCastParser() { return castParser; }

	public AbstractEntityParser<TypeInfo> getClassFieldParser() {
		return classFieldParser;
	}

	public AbstractEntityParser<TypeInfo> getClassMethodParser() {
		return classMethodParser;
	}

	public AbstractEntityParser<TypeInfo> getClassObjectParser() { return classObjectParser; }

	public AbstractEntityParser<ObjectInfo> getClassParser() { return classParser; }

	public AbstractEntityParser<TypeInfo> getClassTailParser() {
		return classTailParser;
	}

	public AbstractEntityParser<ObjectInfo> createExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new ExpressionParser(this, thisInfo, maxOperatorPrecedenceLevelToConsider);
	}

	public AbstractEntityParser<ObjectInfo> getExpressionParser() {
		return expressionParser;
	}

	public AbstractEntityParser<ObjectInfo> getConstructorParser() {
		return constructorParser;
	}

	public AbstractEntityParser<ObjectInfo> getCustomHierarchyParser() {
		return customHierarchyParser;
	}

	public AbstractEntityParser<ObjectInfo> getSimpleExpressionParser() {
		return simpleExpressionParser;
	}

	public AbstractEntityParser<TypeInfo> getInnerClassParser() {
		return innerClassParser;
	}

	public AbstractEntityParser<ObjectInfo> getLiteralParser() {
		return literalParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectFieldParser() {
		return objectFieldParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectMethodParser() {
		return objectMethodParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectTailParser() {
		return objectTailParser;
	}

	public AbstractEntityParser<ObjectInfo> getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	public AbstractEntityParser<ObjectInfo> getUnaryPrefixOperatorParser() {
		return unaryPrefixOperatorParser;
	}

	public AbstractEntityParser<ObjectInfo> getVariableParser() { return variableParser; }

	private static EvaluationMode getEvaluationMode(ParserSettings settings, ParseMode parseMode) {
		if (settings.isEnableDynamicTyping()) {
			return EvaluationMode.DYNAMICALLY_TYPED;
		}
		return parseMode == ParseMode.EVALUATION ? EvaluationMode.STATICALLY_TYPED : EvaluationMode.NONE;
	}
}
