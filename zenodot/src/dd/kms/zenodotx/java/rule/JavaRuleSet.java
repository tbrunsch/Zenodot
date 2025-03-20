package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.Rules;
import dd.kms.zenodotx.rule.compound.OrRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;

import static dd.kms.zenodotx.rule.Rules.*;

public class JavaRuleSet
{
	private final Rule<JavaState>	endOfInput				= new EndOfInputRule();
	private final OrRule<JavaState>	expression				= or("Expression");
	private final Rule<JavaState>	fullExpression			= sequence("Expression until end of input", expression, endOfInput);
	private final Rule<JavaState>	parenthesizedExpression	= 	sequence(
																	"Expression in parentheses",
																	character('('),
																	expression,
																	character(')')
																);

	// region Literals
	private final Rule<JavaState>	integerLiteral			=	new IntegerLiteralRule();
	private final Rule<JavaState>	floatingPointLiteral	=	new FloatingPointLiteralRule();
	private final Rule<JavaState>	booleanLiteral			=	new BooleanLiteralRule();
	private final Rule<JavaState>	characterLiteral		=	new CharacterLiteralRule();
	private final Rule<JavaState>	stringLiteral			=	new StringLiteralRule();
	private final Rule<JavaState>	nullLiteral				=	new NullLiteralRule();
	private final OrRule<JavaState>	literal					=	or(
																	"Literal",
																	integerLiteral,
																	floatingPointLiteral,
																	booleanLiteral,
																	characterLiteral,
																	stringLiteral,
																	nullLiteral
																);
	// endregion

	// region Packages
	private final Rule<JavaState>	rootPackage	=	new RootPackageRule();
	private final Rule<JavaState>	subPackage	=	new SubPackageRule();
	private final Rule<JavaState>	packageRule	=	sequence(
														"Package",
														rootPackage,
														repeat(
															sequence(
																".SubPackage",
																character('.'),
																subPackage
															)
														)
													);
	// endregion

	// region Classes
	private final Rule<JavaState>	qualifiedTopLevelClass	=	new QualifiedTopLevelClassRule();
	private final Rule<JavaState>	nestedClass				=	new NestedClassRule();
	private final Rule<JavaState>	importedClass			=	new ImportedClassRule();
	private final Rule<JavaState>	classRule				=	sequence(
																	"Class",
																	or(
																		"Qualified or imported class",
																		sequence(
																			"Qualified class",
																			packageRule,
																			character('.'),
																			qualifiedTopLevelClass
																		),
																		importedClass
																	),
																	repeat(
																		sequence(
																			".NestedClass",
																			character('.'),
																			nestedClass
																		)
																	)
																);
	// endregion

	// region Fields and Methods
	private final Rule<JavaState>		pushThis			=	action(JavaState::pushThis);

	private final FieldRule				field				=	new FieldRule(false);
	private final Rule<JavaState>		fieldOfThis			=	sequence("Field of this", pushThis, field);
	private final FieldRule				staticField			=	new FieldRule(true);

	private final MethodParameterRule	methodParameter		=	new MethodParameterRule();

	private final MethodNameRule		methodName			=	new MethodNameRule();
	private final StaticMethodNameRule	staticMethodName	=	new StaticMethodNameRule();
	private final Rule<JavaState>		method				=	sequence(
																	"Method",
																	methodName,
																	character('('),
																	or(
																		"Parameter list",
																		empty(),
																		sequence(
																			"Non-empty parameter list",
																			methodParameter,
																			repeat(
																				sequence(
																					"Next parameter",
																					character(','),
																					methodParameter)
																				)
																			)
																	),
																	character(')'),
																	action(JavaRuleSetUtils::executeMethod)
																);
	private final Rule<JavaState>		staticMethod		=	Rules.replaceRule(method, methodName, staticMethodName);
	private final Rule<JavaState>		methodOfThis		=	sequence("Method of this", pushThis, method);
	// endregion

	// region Constructor
	private final ConstructorParameterRule	constructorParameter	=	new ConstructorParameterRule();
	private final Rule<JavaState>			constructor				=	sequence(
																			"Constructor call",
																			keyword("new"),
																			space(),
																			classRule,
																			character('('),
																			or(
																				"Parameter list",
																				empty(),
																				sequence(
																					"Non-empty parameter list",
																					constructorParameter,
																					repeat(
																						sequence(
																							"Next parameter",
																							character(','),
																							constructorParameter
																						)
																					)
																				)
																			),
																			character(')'),
																			action(JavaRuleSetUtils::invokeConstructor)
																		);
	// endregion

	// region Lambda
	private final Rule<JavaState>			lambdaParameterName			=	new LambdaParameterNameRule();
	private final Rule<JavaState>			lambdaParameterDefinition	=	or(
																				"Lambda parameter list definition",
																				lambdaParameterName,
																				sequence(
																					"Lambda parameter list definition in parentheses",
																					character('('),
																					or(
																						"Lambda parameter list",
																						empty(),
																						sequence(
																							"Non-empty lambda parameter list",
																							lambdaParameterName,
																							repeat(
																								sequence(
																									"Next lambda parameter",
																									character(','),
																									lambdaParameterName
																								)
																							)
																						)
																					),
																					character(')')
																				)
																			);
	private final Rule<JavaState>			lambdaExpression			= new LambdaExpressionRule();
	private final Rule<JavaState>			lambda						=	sequence(
																				"Lambda",
																				action(JavaRuleSetUtils::beginParsingLambda),
																				lambdaParameterDefinition,
																				character('-'),
																				character('>'),
																				lambdaExpression
																			);
	// endregion

	private final SimpleRule<JavaState>		variable		=	 new VariableRule();

	private final Rule<JavaState>			arrayIndex		= 	sequence(
																	"Array index",
																	expression,
																	action(s -> JavaRuleSetUtils.checkType(s, int.class))
																);
	private final OrRule<JavaState>			classTail		= 	or(
																	"Class tail",
																	sequence(
																		character('.'),
																		or(
																			"Static field or method access",
																			staticField,
																			staticMethod
																		)
																	)
																);
	private final OrRule<JavaState>			objectTail		=	or(
																	"Object tail",
																	sequence(
																		".Field or .Method",
																		character('.'),
																		or(
																			"Field or method access",
																			field,
																			method
																		)
																	),
																	sequence(
																		"Array element access",
																		action(JavaRuleSetUtils::checkArrayType),
																		character('['),
																		arrayIndex,
																		character(']'),
																		action(JavaRuleSetUtils::accessArrayElement)
																	)
																);

	private final OrRule<JavaState>			simpleExpression	=	or("Simple expression (without binary operators)");
	private final Rule<JavaState>			castExpression		=	sequence(
																		"Class cast",
																		character('('),
																		classRule,
																		character(')'),
																		simpleExpression,
																		action(JavaRuleSetUtils::performCast)
																	);
	private final OrRule<JavaState>	simpleExpressionWithTailPotential		=	or(
																					"Simple expressions that may have an object tail",
																					literal,
																					variable,
																					fieldOfThis,
																					methodOfThis,
																					constructor,
																					parenthesizedExpression,
																					sequence(
																						"Class.Tail",
																						classRule,
																						classTail
																					)
																				);
	private final OrRule<JavaState>	simpleExpressionWithoutTailPotential	=	or(
																					"Simple expressions that may not have an object tail",
																					castExpression,
																					lambda
																				);

	// region Binary and ternary operators

	// TODO: Support lazy evaluation/short circuit evaluation!
	private final BinaryOperatorParseRule		operator12				= new BinaryOperatorParseRule();	// *, /, %  (left to right)
	private final BinaryOperatorParseRule		operator11				= new BinaryOperatorParseRule();	// +, -  (left to right)
	private final BinaryOperatorParseRule		operator10				= new BinaryOperatorParseRule();	// <<, >>, >>>  (left to right)
	private final BinaryOperatorParseRule		operator9				= new BinaryOperatorParseRule();	// <, <=, >, >=  (left to right)
	private final BinaryOperatorParseRule		operator8				= new BinaryOperatorParseRule();	// ==, != (left to right)
	private final BinaryOperatorParseRule		operator7				= new BinaryOperatorParseRule();	// & (left to right)
	private final BinaryOperatorParseRule		operator6				= new BinaryOperatorParseRule();	// ^ (left to right)
	private final BinaryOperatorParseRule		operator5				= new BinaryOperatorParseRule();	// | (left to right)
	private final BinaryOperatorParseRule		operator4				= new BinaryOperatorParseRule();	// && (left to right)
	private final BinaryOperatorParseRule		operator3				= new BinaryOperatorParseRule();	// || (left to right)
	private final TernaryOperatorParseRule1		operator2_1				= new TernaryOperatorParseRule1();	// ?
	private final TernaryOperatorParseRule2		operator2_2				= new TernaryOperatorParseRule2();	// : (right-to-left)
	private final BinaryOperatorParseRule		operator1				= new BinaryOperatorParseRule();	// =, +=, -=, *=, /=, %=, &=, ^=, |=, <<=, >>=, >>>= (right-to-left)
	private final BinaryOperatorExecuteRule		executeBinaryOperator	= new BinaryOperatorExecuteRule();
	private final TernaryOperatorExecuteRule	executeTernaryOperator	= new TernaryOperatorExecuteRule();

	private final Rule<JavaState>	expression12	=	binaryOperatorLeftToRight(simpleExpression, operator12);
	private final Rule<JavaState>	expression11	=	binaryOperatorLeftToRight(expression12, operator11);
	private final Rule<JavaState>	expression10	=	binaryOperatorLeftToRight(expression11, operator10);
	private final Rule<JavaState>	expression9		=	or(
															binaryOperatorLeftToRight(expression10, operator9),
															sequence(
																expression10,
																keyword("instanceof"),
																space(),
																classRule
															)
														);
	private final Rule<JavaState>	expression8		=	binaryOperatorLeftToRight(expression9, operator8);
	private final Rule<JavaState>	expression7		=	binaryOperatorLeftToRight(expression8, operator7);
	private final Rule<JavaState>	expression6		=	binaryOperatorLeftToRight(expression7, operator6);
	private final Rule<JavaState>	expression5		=	binaryOperatorLeftToRight(expression6, operator5);
	private final Rule<JavaState>	expression4		=	binaryOperatorLeftToRight(expression5, operator4);
	private final Rule<JavaState>	expression3		=	binaryOperatorLeftToRight(expression4, operator3);
	private final Rule<JavaState>	expression2		=	ternaryOperatorRightToLeft(expression3, operator2_1, operator2_2);
	private final Rule<JavaState>	expression1		=	binaryOperatorRightToLeft(expression2, operator1);
	// endregion

	// TODO: Support unary prefix operator
	public JavaRuleSet() {
		simpleExpression.setAlternatives(
			sequence(
				simpleExpressionWithTailPotential,
				repeat(objectTail)
			),
			simpleExpressionWithoutTailPotential
		);

		expression.setAlternatives(expression1);
	}

	public Rule<JavaState> getFullExpression() {
		return fullExpression;
	}

	private static SimpleRule<JavaState> keyword(String keyword) {
		return new KeywordRule(keyword);
	}

	private Rule<JavaState> binaryOperatorLeftToRight(Rule<JavaState> subExpression, Rule<JavaState> operator) {
		// TODO: Consider lazy evaluation
		return sequence(
			"Expression op expression (left to right)",
			subExpression,
			repeat(
				sequence(
					operator,
					subExpression,
					executeBinaryOperator
				)
			)
		);
	}

	private Rule<JavaState> binaryOperatorRightToLeft(Rule<JavaState> subExpression, Rule<JavaState> operator) {
		OrRule<JavaState> expression = or("Expression op expression (right to left)");
		expression.setAlternatives(
			subExpression,
			sequence(
				subExpression,
				operator,
				expression,
				executeBinaryOperator
			)
		);
		return expression;
	}

	private Rule<JavaState> ternaryOperatorRightToLeft(Rule<JavaState> subExpression, Rule<JavaState> operatorPart1, Rule<JavaState> operatorPart2) {
		// TODO: Consider lazy evaluation
		OrRule<JavaState> expression = or();
		expression.setAlternatives(
			expression3,
			sequence(
				subExpression,
				operatorPart1,
				expression,
				operatorPart2,
				expression,
				executeTernaryOperator
			)
		);
		return expression;
	}
}
