package dd.kms.zenodot.api.result;

import dd.kms.zenodot.api.wrappers.TypeInfo;

/**
 * An instance of this interface is returned if the subexpression describes a class.
 */
public interface ClassParseResult extends ParseResult
{
	TypeInfo getType();
}
