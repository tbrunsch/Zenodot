package dd.kms.zenodot.result;

/**
 * Common interface of all potential results when parsing an expression
 */
public interface ParseResultIF
{
	ParseResultType getResultType();
	int getPosition();
}
