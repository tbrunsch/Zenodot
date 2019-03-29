package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * An instance of this class is returned if the subexpression describes a class.
 */
public class ClassParseResult implements ParseResult
{
	private final int		position; // exclusive
	private final TypeInfo type;

	public ClassParseResult(int position, TypeInfo type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.CLASS_PARSE_RESULT;
	}

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
