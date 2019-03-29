package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * An instance of this class is returned if the subexpression describes an object.
 */
public class ObjectParseResult implements ParseResult
{
	private final int			position; // exclusive
	private final ObjectInfo 	objectInfo;

	public ObjectParseResult(int position, ObjectInfo objectInfo) {
		this.position = position;
		this.objectInfo = objectInfo;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.OBJECT_PARSE_RESULT;
	}

	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Parsed until " + position + ": " + objectInfo;
	}
}
