package dd.kms.zenodot.result;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public interface ObjectParseResult extends ParseResult
{
	int getPosition();
	ObjectInfo getObjectInfo();
	ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws ParseException;
}
