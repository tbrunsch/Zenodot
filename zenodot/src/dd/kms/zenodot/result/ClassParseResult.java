package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * An instance of this interface is returned if the subexpression describes a class.
 */
public interface ClassParseResult extends ParseOutcome
{
	TypeInfo getType();
}
