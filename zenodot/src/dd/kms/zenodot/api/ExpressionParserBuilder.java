package dd.kms.zenodot.api;

public interface ExpressionParserBuilder
{
	ExpressionParserBuilder variables(Variables variables);
	ExpressionParser createExpressionParser();
}
