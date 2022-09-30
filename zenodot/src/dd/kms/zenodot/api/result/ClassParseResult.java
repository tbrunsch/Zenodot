package dd.kms.zenodot.api.result;

/**
 * An instance of this interface is returned if the subexpression describes a class.
 */
public interface ClassParseResult extends ParseResult
{
	Class<?> getType();
}
