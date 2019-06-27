package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.TypeInfo;

class ClassParseResultImpl implements ClassParseResult
{
	private final int		position; // exclusive
	private final TypeInfo type;

	ClassParseResultImpl(int position, TypeInfo type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.RESULT;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.CLASS;
	}

	@Override
	public TypeInfo getType() {
		return type;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Parsed until " + position + ": " + type;
	}
}
