package dd.kms.zenodot.utils;

import dd.kms.zenodot.parsers.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.utils.dataProviders.*;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Utility class used by most parsers to access other parsers, data providers,
 * and the parser settings.
 */
public class ParserToolbox
{
	private final ObjectInfo						thisInfo;
	private final ParserSettings					settings;
	private final EvaluationMode					evaluationMode;

	private final ClassDataProvider					classDataProvider;
	private final ExecutableDataProvider			executableDataProvider;
	private final FieldDataProvider					fieldDataProvider;
	private final ObjectInfoProvider				objectInfoProvider;
	private final ObjectTreeNodeDataProvider		objectTreeNodeDataProvider;
	private final OperatorResultProvider 			operatorResultProvider;
	private final VariableDataProvider				variableDataProvider;

	private final AbstractEntityParser<ObjectInfo>	castParser;
	private final AbstractEntityParser<TypeInfo>	classFieldParser;
	private final AbstractEntityParser<TypeInfo>	classMethodParser;
	private final AbstractEntityParser<TypeInfo>	classObjectParser;
	private final AbstractEntityParser<TypeInfo>	classTailParser;
	private final AbstractEntityParser<ObjectInfo>	constructorParser;
	private final AbstractEntityParser<ObjectInfo>	customHierarchyParser;
	private final AbstractEntityParser<ObjectInfo>	expressionParser;
	private final AbstractEntityParser<ObjectInfo>	importedClassParser;
	private final AbstractEntityParser<TypeInfo>	innerClassParser;
	private final AbstractEntityParser<ObjectInfo>	literalParser;
	private final AbstractEntityParser<ObjectInfo>	objectFieldParser;
	private final AbstractEntityParser<ObjectInfo>	objectMethodParser;
	private final AbstractEntityParser<ObjectInfo>	objectTailParser;
	private final AbstractEntityParser<ObjectInfo>	parenthesizedExpressionParser;
	private final AbstractEntityParser<PackageInfo>	qualifiedClassParser;
	private final AbstractEntityParser<ObjectInfo>	rootpackageParser;
	private final AbstractEntityParser<ObjectInfo>	simpleExpressionParser;
	private final AbstractEntityParser<PackageInfo>	subpackageParser;
	private final AbstractEntityParser<ObjectInfo>	unaryPrefixOperatorParser;
	private final AbstractEntityParser<ObjectInfo>	variableParser;

	public ParserToolbox(ObjectInfo thisInfo, ParserSettings settings, ParseMode parseMode) {
		this.thisInfo = thisInfo;
		this.settings = settings;
		this.evaluationMode = getEvaluationMode(settings, parseMode);

		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);

		classDataProvider				= new ClassDataProvider(this);
		executableDataProvider			= new ExecutableDataProvider(this);
		fieldDataProvider				= new FieldDataProvider(this);
		objectTreeNodeDataProvider		= new ObjectTreeNodeDataProvider();
		operatorResultProvider 			= new OperatorResultProvider(objectInfoProvider, evaluationMode);
		variableDataProvider			= new VariableDataProvider(settings.getVariables());

		castParser						= new CastParser(this, thisInfo);
		classFieldParser				= new ClassFieldParser(this, thisInfo);
		classMethodParser				= new ClassMethodParser(this, thisInfo);
		classObjectParser				= new ClassObjectParser(this, thisInfo);
		classTailParser					= new ClassTailParser(this, thisInfo);
		constructorParser				= new ConstructorParser(this, thisInfo);
		customHierarchyParser			= new CustomHierarchyParser(this, thisInfo);
		expressionParser				= createExpressionParser(OperatorResultProvider.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
		importedClassParser				= new ImportedClassParser(this, thisInfo);
		innerClassParser				= new InnerClassParser(this, thisInfo);
		literalParser					= new LiteralParser(this, thisInfo);
		objectFieldParser				= new ObjectFieldParser(this, thisInfo);
		objectMethodParser				= new ObjectMethodParser(this, thisInfo);
		objectTailParser				= new ObjectTailParser(this, thisInfo);
		parenthesizedExpressionParser	= new ParenthesizedExpressionParser(this, thisInfo);
		qualifiedClassParser			= new QualifiedClassParser(this, thisInfo);
		rootpackageParser				= new RootpackageParser(this, thisInfo);
		simpleExpressionParser			= new SimpleExpressionParser(this, thisInfo);
		subpackageParser				= new SubpackageParser(this, thisInfo);
		unaryPrefixOperatorParser		= new UnaryPrefixOperatorParser(this, thisInfo);
		variableParser					= new VariableParser(this, thisInfo);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
	}

	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
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
	public AbstractEntityParser<ObjectInfo> getCastParser() { return castParser; }

	public AbstractEntityParser<TypeInfo> getClassFieldParser() {
		return classFieldParser;
	}

	public AbstractEntityParser<TypeInfo> getClassMethodParser() {
		return classMethodParser;
	}

	public AbstractEntityParser<TypeInfo> getClassObjectParser() { return classObjectParser; }

	public AbstractEntityParser<TypeInfo> getClassTailParser() {
		return classTailParser;
	}

	public AbstractEntityParser<ObjectInfo> createExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new ExpressionParser(this, thisInfo, maxOperatorPrecedenceLevelToConsider);
	}

	public AbstractEntityParser<ObjectInfo> getConstructorParser() {
		return constructorParser;
	}

	public AbstractEntityParser<ObjectInfo> getCustomHierarchyParser() {
		return customHierarchyParser;
	}

	public AbstractEntityParser<ObjectInfo> getExpressionParser() {
		return expressionParser;
	}

	public AbstractEntityParser<ObjectInfo> getImportedClassParser() { return importedClassParser; }

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

	public AbstractEntityParser<PackageInfo> getQualifiedClassParser() {
		return qualifiedClassParser;
	}

	public AbstractEntityParser<ObjectInfo> getRootpackageParser() { return rootpackageParser; }

	public AbstractEntityParser<ObjectInfo> getSimpleExpressionParser() {
		return simpleExpressionParser;
	}

	public AbstractEntityParser<PackageInfo> getSubpackageParser() { return subpackageParser; }

	public AbstractEntityParser<ObjectInfo> getUnaryPrefixOperatorParser() {
		return unaryPrefixOperatorParser;
	}

	public AbstractEntityParser<ObjectInfo> getVariableParser() { return variableParser; }

	private static EvaluationMode getEvaluationMode(ParserSettings settings, ParseMode parseMode) {
		switch (parseMode) {
			case CODE_COMPLETION:
				return settings.isEnableDynamicTyping() ? EvaluationMode.DYNAMICALLY_TYPED : EvaluationMode.NONE;
			case EVALUATION:
				return settings.isEnableDynamicTyping() ? EvaluationMode.DYNAMICALLY_TYPED : EvaluationMode.STATICALLY_TYPED;
			case WITHOUT_EVALUATION:
				return EvaluationMode.NONE;
			case COMPILATION:
				return EvaluationMode.COMPILED;
			default:
				throw new IllegalArgumentException("Unexpected parse mode: " + parseMode);
		}
	}
}
