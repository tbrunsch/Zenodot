package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

class ObjectParseResultImpl implements ObjectParseResult
{
	private final ObjectInfo 	objectInfo;

	ObjectParseResultImpl(ObjectInfo objectInfo) {
		this.objectInfo = objectInfo;
	}

	@Override
	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	@Override
	public boolean isCompiled() {
		return false;
	}

	@Override
	public String toString() {
		return objectInfo.toString();
	}
}
