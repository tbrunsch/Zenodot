package dd.kms.zenodot.impl.result;

/**
 * An instance of this class is returned if the subexpression describes a class.
 */
public class ClassParseResult implements ParseResult
{
	private final Class<?> type;

	ClassParseResult(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
