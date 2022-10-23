package dd.kms.zenodot.impl.result;

/**
 * An instance of this interface is returned if the subexpression describes a class.
 */
public interface ClassParseResult extends ParseResult
{
	Class<?> getType();
}
