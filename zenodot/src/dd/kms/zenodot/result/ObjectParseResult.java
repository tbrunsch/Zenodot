package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * An instance of this interface is returned if the subexpression describes an object.
 */
public interface ObjectParseResult extends ParseResult
{
	ObjectInfo getObjectInfo();
}
