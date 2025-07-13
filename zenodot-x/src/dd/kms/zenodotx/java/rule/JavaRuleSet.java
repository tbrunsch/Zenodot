package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.compound.DelegatingRule;
import dd.kms.zenodotx.rule.compound.OrRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;

import static dd.kms.zenodotx.rule.Rules.*;

public class JavaRuleSet
{
	private static final String	INTEGER_LITERAL_REGEX				= regexOr("0", "[1-9][0-9]*");
	private static final String	LONG_LITERAL_REGEX					= "(" + INTEGER_LITERAL_REGEX + ")" + "[lL]";
	private static final String	DECIMALS_REGEX						= "\\.[0-9]+";
	private static final String	POTENTIALLY_EMPTY_DECIMALS_REGEX	= "\\.[0-9]*";
	private static final String EXPONENT_REGEX						= "[eE][+-]?[0-9]+";
	private static final String	FLOAT_LITERAL_REGEX 				= regexOr(
																			// 123f; suffix required to distinguish from int
																			"(" + INTEGER_LITERAL_REGEX + ")" + "[fF]",
																			"(" +
																			// suffix required to distinguish from double
																			regexOr(
																				// 123E-4f
																				INTEGER_LITERAL_REGEX + EXPONENT_REGEX,
																				// 123.f, 123.45E6f
																				INTEGER_LITERAL_REGEX + POTENTIALLY_EMPTY_DECIMALS_REGEX + "(" + EXPONENT_REGEX + ")?",
																				// .123f, .123E-4f
																				DECIMALS_REGEX + "(" + EXPONENT_REGEX + ")?"
																			) + ")" + "[fF]"
																		);
	private static final String	DOUBLE_LITERAL_REGEX 				= regexOr(
																			// 123d; suffix required to distinguish from int
																			"(" + INTEGER_LITERAL_REGEX + ")" + "[dD]",
																			"(" +
																			// suffix optional
																			regexOr(
																				// 123E-4f
																				INTEGER_LITERAL_REGEX + EXPONENT_REGEX,
																				// 123.f, 123.45E6f
																				INTEGER_LITERAL_REGEX + POTENTIALLY_EMPTY_DECIMALS_REGEX + "(" + EXPONENT_REGEX + ")?",
																				// .123f, .123E-4f
																				DECIMALS_REGEX + "(" + EXPONENT_REGEX + ")?"
																			) + ")" + "[dD]?"
																		);

	private final OrRule<Void, InstanceParseResult, JavaSettings>	expression			= Rules.<Void, InstanceParseResult, JavaSettings>or()
																							.name("Expression");
	private final Rule<Void, InstanceParseResult, JavaSettings>		fullExpression		= expression
																							.then(endOfInput())
																							.name("Full expression");
	private final Rule<Void, InstanceParseResult, JavaSettings>		parenthesizedExpression
																		= Rules.<Void, JavaSettings>character('(')
																			.then(expression)
																			.then(')')
																			.name("Expression in parentheses");

	// region Literals
	private final Rule<Void, InstanceParseResult, JavaSettings>		integerLiteral			=	new NumericLiteralRule<>(int.class, INTEGER_LITERAL_REGEX, Integer::parseInt, 0);
	private final Rule<Void, InstanceParseResult, JavaSettings>		longLiteral				=	new NumericLiteralRule<>(long.class, LONG_LITERAL_REGEX, Long::parseLong, 1);
	private final Rule<Void, InstanceParseResult, JavaSettings>		floatLiteral			=	new NumericLiteralRule<>(float.class, FLOAT_LITERAL_REGEX, Float::parseFloat, 1, 2);
	private final Rule<Void, InstanceParseResult, JavaSettings>		doubleLiteral			=	new NumericLiteralRule<>(double.class, DOUBLE_LITERAL_REGEX, Double::parseDouble, 1, 2);
	private final Rule<Void, InstanceParseResult, JavaSettings>		falseLiteral			=	new KeywordLiteralRule("false", InfoProvider.createObjectInfo(false, boolean.class));
	private final Rule<Void, InstanceParseResult, JavaSettings>		trueLiteral				=	new KeywordLiteralRule("true", InfoProvider.createObjectInfo(true, boolean.class));
	private final Rule<Void, InstanceParseResult, JavaSettings>		characterLiteral		=	new CharacterLiteralRule();
	private final Rule<Void, InstanceParseResult, JavaSettings>		stringLiteral			=	new StringLiteralRule();
	private final Rule<Void, InstanceParseResult, JavaSettings>		nullLiteral				=	new KeywordLiteralRule("null", InfoProvider.NULL_LITERAL);
	private final Rule<Void, InstanceParseResult, JavaSettings>		thisLiteral				=	new ThisRule();
	private final OrRule<Void, InstanceParseResult, JavaSettings>	literal					=	or(
																									integerLiteral,
																									longLiteral,
																									floatLiteral,
																									doubleLiteral,
																									falseLiteral,
																									trueLiteral,
																									characterLiteral,
																									stringLiteral,
																									nullLiteral,
																									thisLiteral
																								).name("Literal");
	// endregion

	// region Packages
	private final Rule<Void, Package, JavaSettings>		rootPackage	=	new RootPackageRule();
	private final Rule<Package, Package, JavaSettings>	subPackage	=	new SubPackageRule();
	private final Rule<Void, Package, JavaSettings>		packageRule	=	rootPackage
																			.then(
																				repeat(
																					Rules.<Package, JavaSettings>character('.')
																					.then(subPackage)
																					.name(".SubPackage")
																				)
																			).name("Package");
	// endregion

	// region Classes
	private final Rule<Package, Class<?>, JavaSettings>		qualifiedTopLevelClass	=	new QualifiedTopLevelClassRule();
	private final Rule<Class<?>, Class<?>, JavaSettings>	nestedClass				=	new NestedClassRule();
	private final Rule<Void, Class<?>, JavaSettings>		importedClass			=	new ImportedClassRule();
	private final Rule<Void, Class<?>, JavaSettings>		classRule				=	or(
																							packageRule.then('.').then(qualifiedTopLevelClass),
																							importedClass
																						).name("Qualified or imported class")
																						.then(
																							repeat(
																								Rules.<Class<?>, JavaSettings>character('.')
																								.then(nestedClass)
																								.name(".NestedClass")
																							)
																						).name("Class");
	// endregion

	// region Fields and Methods
	private final AbstractFieldRule<InstanceParseResult>	instanceField	=	new InstanceFieldRule();
	private final AbstractFieldRule<Void>					fieldOfThis		=	new FieldOfThisRule();
	private final AbstractFieldRule<Class<?>>				classField 		=	new ClassFieldRule();

	private final Rule<ExecutableParseInfo, ExecutableParseInfo, JavaSettings>	methodParameter	= new MethodParameterRule(expression);
	private final Rule<ExecutableParseInfo, InstanceParseResult, JavaSettings>	invokeMethod	= new InvokeMethodRule();

	private final AbstractMethodNameRule<InstanceParseResult>	instanceMethodName	= new InstanceMethodNameRule();
	private final AbstractMethodNameRule<Void> 					methodNameOfThis	= new MethodNameOfThisRule();
	private final AbstractMethodNameRule<Class<?>>				classMethodName		= new ClassMethodNameRule();
	private final Rule<InstanceParseResult, InstanceParseResult, JavaSettings>	instanceMethod		= methodRule(instanceMethodName);
	private final Rule<Void, InstanceParseResult, JavaSettings>					methodOfThis		= methodRule(methodNameOfThis);
	private final Rule<Class<?>, InstanceParseResult, JavaSettings>				classMethod			= methodRule(classMethodName);
	// endregion

	// region Constructor
	private final Rule<Void, ConstructorParseInfo, JavaSettings>				constructorClass			=	new ConstructorClassRule(classRule);
	private final ConstructorParameterRule										constructorParameter		=	new ConstructorParameterRule(expression);
	private final Rule<ConstructorParseInfo, InstanceParseResult, JavaSettings>	invokeInstanceConstructor	= new InvokeInstanceConstructorRule();
	private final Rule<Void, InstanceParseResult, JavaSettings>		constructor				=	JavaRuleSet.<Void>keyword("new")
																								.then(space())
																								.then(constructorClass)
																								.then(
																									or(
																										Rules.<ConstructorParseInfo, JavaSettings>character('(')
																											.then(
																												or(
																													empty(),
																													constructorParameter
																														.then(
																															repeat(
																																Rules.<ConstructorParseInfo, JavaSettings>character(',')
																																	.then(constructorParameter)
																																	.name("Next parameter")
																															).name("Further parameters")
																														).name("Non-empty parameter list")
																												).name("Parameter list")
																											).then(')')
																											.then(invokeInstanceConstructor)
																											.name("Instance constructor")
																											// TODO: Add array constructor
																										)
																								);
	// endregion

	// region Lambda
	private final Rule<Void, LambdaParameterInfo, JavaSettings>					beginLambda 				=	new BeginLambdaRule();
	private final Rule<LambdaParameterInfo, LambdaParameterInfo, JavaSettings>	lambdaParameterName			=	new LambdaParameterNameRule();
	private final Rule<Void, LambdaParameterInfo, JavaSettings>					lambdaParameterDefinition	=	beginLambda
																								.then(
																									or(
																										lambdaParameterName,
																										Rules.<LambdaParameterInfo, JavaSettings>character('(')
																										.then(
																											or(
																												empty(),
																												lambdaParameterName
																												.then(
																													repeat(
																														Rules.<LambdaParameterInfo, JavaSettings>character('(')
																														.then(lambdaParameterName)
																														.name("Next lambda parameter")
																													).name("Further parameters")
																												).name("Non-empty lambda parameter list")
																											).name("Lambda parameter list")
																										).then(')')
																									).name("Lambda parameter definition")
																								);
	private final Rule<LambdaParameterInfo, InstanceParseResult, JavaSettings>	lambdaExpression	= new LambdaExpressionRule();
	private final Rule<Void, InstanceParseResult, JavaSettings>					lambda				= lambdaParameterDefinition
																										.then('-')
																										.then('>')
																										.then(lambdaExpression)
																										.name("Lambda");
	// endregion

	/*
	 * TODO: Rework grammar according to https://introcs.cs.princeton.edu/java/11precedence/
	 *
	 * For example, unary operators are in the precedence between cast and, e.g., object creation,
	 * but this is not represented by the current grammar.
	 */
	private final Rule<Void, InstanceParseResult, JavaSettings>	variable	=	 new VariableRule();

	private final Rule<InstanceParseResult, ArrayAccessInfo, JavaSettings>	arrayIndex			= 	new ArrayIndexRule(expression);
	private final Rule<ArrayAccessInfo, InstanceParseResult, JavaSettings>	accessArrayElement	=	new AccessArrayElementRule();
	private final OrRule<Class<?>, InstanceParseResult, JavaSettings>	classTail	= 	or(
																							Rules.<Class<?>, JavaSettings>character('.')
																							.then(
																								or(
																									classField,
																									classMethod
																								).name("Static field or method access")
																							).name("Class tail")
																						);
	private final OrRule<InstanceParseResult, InstanceParseResult, JavaSettings>	objectTail	=	or(
																										Rules.<InstanceParseResult, JavaSettings>character('.')
																										.then(
																											or(
																												instanceField,
																												instanceMethod
																											).name(".field or .method()")
																										),
																										Rules.<InstanceParseResult, JavaSettings>character('[')
																										.then(arrayIndex)
																										.then(']')
																										.then(accessArrayElement)
																										.name("Array element access")
																									).name("Object tail");

	private final OrRule<Void, InstanceParseResult, JavaSettings>	simpleExpression	= Rules.<Void, InstanceParseResult, JavaSettings>or()
																							.name("Simple expression (without binary operators)");
	private final Rule<Class<?>, InstanceParseResult, JavaSettings>	classCastRule		= new ClassCastRule(simpleExpression);
	private final Rule<Void, InstanceParseResult, JavaSettings>		castExpression		=  Rules.<Void, JavaSettings>character('(')
																							.then(classRule)
																							.then(')')
																							.then(classCastRule)
																							.name("Class cast");
	
	private final OrRule<Void, InstanceParseResult, JavaSettings>	simpleExpressionWithTailPotential	=	or(
																												literal,
																												variable,
																												fieldOfThis,
																												methodOfThis,
																												constructor,
																												parenthesizedExpression,
																												classRule
																													.then(classTail)
																													.name("Class.Tail")
																											).name("Simple expressions that may have an object tail");
	private final OrRule<Void, InstanceParseResult, JavaSettings>	simpleExpressionWithoutTailPotential	=	or(
																													castExpression,
																													lambda
																												).name("Simple expressions that may not have an object tail");

	private final UnaryOperatorRegistry unaryOperatorRegistry = new UnaryOperatorRegistry();
	private final Rule<Void, String, JavaSettings>					unaryPrefixOperator				= new UnaryPrefixOperatorParseRule(unaryOperatorRegistry);
	private final Rule<String, InstanceParseResult, JavaSettings>	unaryPrefixOperatorExecuteRule	= new UnaryPrefixOperatorExecuteRule(unaryOperatorRegistry, simpleExpression);

	// region Binary and ternary operators

	// TODO: Support lazy evaluation/short circuit evaluation!
	private final BinaryOperatorParseRule		operator12				= new BinaryOperatorParseRule();	// *, /, %  (left to right)
	private final BinaryOperatorParseRule		operator11				= new BinaryOperatorParseRule();	// +, -  (left to right)
	private final BinaryOperatorParseRule		operator10				= new BinaryOperatorParseRule();	// <<, >>, >>>  (left to right)
	private final BinaryOperatorParseRule		operator9				= new BinaryOperatorParseRule();	// <, <=, >, >=  (left to right)
	private final Rule<InstanceParseResult, InstanceParseResult, JavaSettings>	instanceofCheck			= new InstanceOfRule(classRule);
	private final BinaryOperatorParseRule		operator8				= new BinaryOperatorParseRule();	// ==, != (left to right)
	private final BinaryOperatorParseRule		operator7				= new BinaryOperatorParseRule();	// & (left to right)
	private final BinaryOperatorParseRule		operator6				= new BinaryOperatorParseRule();	// ^ (left to right)
	private final BinaryOperatorParseRule		operator5				= new BinaryOperatorParseRule();	// | (left to right)
	private final BinaryOperatorParseRule		operator4				= new BinaryOperatorParseRule();	// && (left to right)
	private final BinaryOperatorParseRule		operator3				= new BinaryOperatorParseRule();	// || (left to right)
	private final BinaryOperatorParseRule		operator1				= new BinaryOperatorParseRule();	// =, +=, -=, *=, /=, %=, &=, ^=, |=, <<=, >>=, >>>= (right-to-left)

	private final Rule<Void, InstanceParseResult, JavaSettings>	expression12	=	binaryOperatorLeftToRight(simpleExpression, operator12);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression11	=	binaryOperatorLeftToRight(expression12, operator11);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression10	=	binaryOperatorLeftToRight(expression11, operator10);

	/*
	 * Originally, we wrote
	 *	private final Rule<Void, InstanceParseResult, JavaSettings>	expression9		=	or(
	 *																						binaryOperatorLeftToRight(expression10, operator9),
	 *																						expression10
	 *																							.then(keyword("instanceof"))
	 *																							.then(space())
	 *																							.then(instanceofCheck)
	 *																					);
	 *
	 * but that way the first expression10 is evaluated in both cases of the or(). If the evaluation
	 * of expression10 causes side effects, then this side effect would occur multiple times that way.
	 */
	private final BinaryOperatorExecuteRule 					operator9ExecuteRule	= new BinaryOperatorExecuteRule(operator9, expression10);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression9				= expression10
																							.then(
																								or(
																									repeat(operator9ExecuteRule),
																									JavaRuleSet.<InstanceParseResult>keyword("instanceof")
																										.then(space())
																										.then(instanceofCheck)
																								)
																							).name("Expression op expression (left to right) or expression instanceof Class");

	private final Rule<Void, InstanceParseResult, JavaSettings>	expression8		=	binaryOperatorLeftToRight(expression9, operator8);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression7		=	binaryOperatorLeftToRight(expression8, operator7);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression6		=	binaryOperatorLeftToRight(expression7, operator6);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression5		=	binaryOperatorLeftToRight(expression6, operator5);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression4		=	binaryOperatorLeftToRight(expression5, operator4);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression3		=	binaryOperatorLeftToRight(expression4, operator3);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression2		=	conditionalOperator(expression3);
	private final Rule<Void, InstanceParseResult, JavaSettings>	expression1		=	binaryOperatorRightToLeft(expression2, operator1);
	// endregion

	// TODO: Support unary postfix operator
	public JavaRuleSet() {
		simpleExpression.setAlternatives(
			simpleExpressionWithTailPotential
			.then(
				repeat(objectTail)
			),
			simpleExpressionWithoutTailPotential,
			unaryPrefixOperator
				.then(unaryPrefixOperatorExecuteRule)
		);

		expression.setAlternatives(expression1);

		registerUnaryPrefixOperators();
	}

	protected void registerUnaryPrefixOperators() {
		registerUnaryPrefixOperatorsWithAssignment();
		registerSignOperators();
		registerNegationOperators();
	}

	protected void registerUnaryPrefixOperatorsWithAssignment() {
		UnaryOperators.registerPrefixOperatorsWithAssignment(unaryOperatorRegistry);
	}

	protected void registerSignOperators() {
		UnaryOperators.registerSignOperators(unaryOperatorRegistry);
	}

	protected void registerNegationOperators() {
		UnaryOperators.registerNegationOperators(unaryOperatorRegistry);
	}

	public Rule<Void, InstanceParseResult, JavaSettings> getFullExpression() {
		return fullExpression;
	}

	private static <IO> Rule<IO, IO, JavaSettings> endOfInput() {
		return new EndOfInputRule<IO>();
	}

	private static <IO> SimpleRule<IO, IO, JavaSettings> keyword(String keyword) {
		return new KeywordRule<>(keyword);
	}

	private <C> Rule<C, InstanceParseResult, JavaSettings> methodRule(Rule<C, ExecutableParseInfo, JavaSettings> methodName) {
		return methodName
			.then('(')
			.then(
				or(
					empty(),
					methodParameter
						.then(
							repeat(
								Rules.<ExecutableParseInfo, JavaSettings>character(',')
									.then(methodParameter)
									.name("Next parameter")
							).name("Further parameters")
						).name("Non-empty parameter list")
				).name("Parameter list")
			).then(')')
			.then(invokeMethod)
			.name("Method");
	}

	private Rule<Void, InstanceParseResult, JavaSettings> binaryOperatorLeftToRight(Rule<Void, InstanceParseResult, JavaSettings> subExpression, Rule<Void, String, JavaSettings> operator) {
		BinaryOperatorExecuteRule binaryOperatorExecuteRule = new BinaryOperatorExecuteRule(operator, subExpression);
		return subExpression
			.then(repeat(binaryOperatorExecuteRule))
			.name("Expression op expression (left to right)");
	}

	private Rule<Void, InstanceParseResult, JavaSettings> binaryOperatorRightToLeft(Rule<Void, InstanceParseResult, JavaSettings> subExpression, Rule<Void, String, JavaSettings> operator) {
		DelegatingRule<Void, InstanceParseResult, JavaSettings> expression = Rules.createDelegate();
		BinaryOperatorExecuteRule binaryOperatorExecuteRule = new BinaryOperatorExecuteRule(operator, expression);
		expression.setDelegate(
			subExpression
			.then(
				or(
					empty(),
					binaryOperatorExecuteRule
				)
			)
			.name("Expression op expression (right to left)")
		);
		return expression;
	}

	private Rule<Void, InstanceParseResult, JavaSettings> conditionalOperator(Rule<Void, InstanceParseResult, JavaSettings> subExpression) {
		DelegatingRule<Void, InstanceParseResult, JavaSettings> expression = Rules.createDelegate();
		ConditionalOperatorExecuteRule conditionalOperatorExecuteRule = new ConditionalOperatorExecuteRule(expression);
		expression.setDelegate(
			subExpression
			.then(
				or(
					empty(),
					conditionalOperatorExecuteRule
				)
			)
			.name("Condition ? expression1 : expression2")
		);
		return expression;
	}

	private static String regexOr(String... alternatives) {
		return String.join("|", alternatives);
	}
}
