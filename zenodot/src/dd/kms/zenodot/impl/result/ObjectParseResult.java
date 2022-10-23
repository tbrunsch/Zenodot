package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

public interface ObjectParseResult extends ParseResult
{
	String getExpression();
	int getPosition();
	ObjectInfo getObjectInfo();
	ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws ParseException;
}
