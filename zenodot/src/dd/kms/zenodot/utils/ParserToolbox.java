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

	private final AbstractParser<ObjectInfo> 		castParser;
	private final AbstractParser<TypeInfo>			classFieldParser;
	private final AbstractParser<TypeInfo>			classMethodParser;
	private final AbstractParser<TypeInfo>			classObjectParser;
	private final AbstractParser<TypeInfo>			classTailParser;
	private final AbstractParser<ObjectInfo>		constructorParser;
	private final AbstractParser<ObjectInfo>		customHierarchyParser;
	private final AbstractParser<ObjectInfo>		expressionParser;
	private final AbstractParser<ObjectInfo>		unqualifiedClassParser;
	private final AbstractParser<TypeInfo>			innerClassParser;
	private final AbstractParser<ObjectInfo>		literalParser;
	private final AbstractParser<ObjectInfo>		objectFieldParser;
	private final AbstractParser<ObjectInfo>		objectMethodParser;
	private final AbstractParser<ObjectInfo>		objectTailParser;
	private final AbstractParser<ObjectInfo>		parenthesizedExpressionParser;
	private final AbstractParser<PackageInfo>		qualifiedClassParser;
	private final AbstractParser<ObjectInfo>		rootpackageParser;
	private final AbstractParser<ObjectInfo>		simpleExpressionParser;
	private final AbstractParser<PackageInfo>		subpackageParser;
	private final AbstractParser<ObjectInfo>		unaryPrefixOperatorParser;
	private final AbstractParser<ObjectInfo>		variableParser;

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
		variableDataProvider			= new VariableDataProvider(settings.getVariables(), objectInfoProvider);

		castParser						= new CastParser(this);
		classFieldParser				= new ClassFieldParser(this);
		classMethodParser				= new ClassMethodParser(this);
		classObjectParser				= new ClassObjectParser(this);
		classTailParser					= new ClassTailParser(this);
		constructorParser				= new ConstructorParser(this);
		customHierarchyParser			= new CustomHierarchyParser(this);
		expressionParser				= createExpressionParser(OperatorResultProvider.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
		unqualifiedClassParser			= new UnqualifiedClassParser(this);
		innerClassParser				= new InnerClassParser(this);
		literalParser					= new LiteralParser(this);
		objectFieldParser				= new ObjectFieldParser(this);
		objectMethodParser				= new ObjectMethodParser(this);
		objectTailParser				= new ObjectTailParser(this);
		parenthesizedExpressionParser	= new ParenthesizedExpressionParser(this);
		qualifiedClassParser			= new QualifiedClassParser(this);
		rootpackageParser				= new RootpackageParser(this);
		simpleExpressionParser			= new SimpleExpressionParser(this);
		subpackageParser				= new SubpackageParser(this);
		unaryPrefixOperatorParser		= new UnaryPrefixOperatorParser(this);
		variableParser					= new VariableParser(this);
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
	public AbstractParser<ObjectInfo> getCastParser() { return castParser; }

	public AbstractParser<TypeInfo> getClassFieldParser() {
		return classFieldParser;
	}

	public AbstractParser<TypeInfo> getClassMethodParser() {
		return classMethodParser;
	}

	public AbstractParser<TypeInfo> getClassObjectParser() { return classObjectParser; }

	public AbstractParser<TypeInfo> getClassTailParser() {
		return classTailParser;
	}

	public AbstractParser<ObjectInfo> createExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new ExpressionParser(this, maxOperatorPrecedenceLevelToConsider);
	}

	public AbstractParser<ObjectInfo> getConstructorParser() {
		return constructorParser;
	}

	public AbstractParser<ObjectInfo> getCustomHierarchyParser() {
		return customHierarchyParser;
	}

	public AbstractParser<ObjectInfo> getExpressionParser() {
		return expressionParser;
	}

	public AbstractParser<ObjectInfo> getUnqualifiedClassParser() { return unqualifiedClassParser; }

	public AbstractParser<TypeInfo> getInnerClassParser() {
		return innerClassParser;
	}

	public AbstractParser<ObjectInfo> getLiteralParser() {
		return literalParser;
	}

	public AbstractParser<ObjectInfo> getObjectFieldParser() {
		return objectFieldParser;
	}

	public AbstractParser<ObjectInfo> getObjectMethodParser() {
		return objectMethodParser;
	}

	public AbstractParser<ObjectInfo> getObjectTailParser() {
		return objectTailParser;
	}

	public AbstractParser<ObjectInfo> getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	public AbstractParser<PackageInfo> getQualifiedClassParser() {
		return qualifiedClassParser;
	}

	public AbstractParser<ObjectInfo> getRootpackageParser() { return rootpackageParser; }

	public AbstractParser<ObjectInfo> getSimpleExpressionParser() {
		return simpleExpressionParser;
	}

	public AbstractParser<PackageInfo> getSubpackageParser() { return subpackageParser; }

	public AbstractParser<ObjectInfo> getUnaryPrefixOperatorParser() {
		return unaryPrefixOperatorParser;
	}

	public AbstractParser<ObjectInfo> getVariableParser() { return variableParser; }

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
