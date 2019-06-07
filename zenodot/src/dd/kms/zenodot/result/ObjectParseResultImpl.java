package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

class ObjectParseResultImpl implements ObjectParseResult
{
	private final int			position; // exclusive
	private final ObjectInfo 	objectInfo;

	ObjectParseResultImpl(int position, ObjectInfo objectInfo) {
		this.position = position;
		this.objectInfo = objectInfo;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.RESULT;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.OBJECT;
	}

	@Override
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
