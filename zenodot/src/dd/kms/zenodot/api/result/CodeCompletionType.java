package dd.kms.zenodot.api.result;

/**
 * Ordered by priority of code completions
 */
public enum CodeCompletionType
{
	VARIABLE		("Variable"),
	OBJECT_TREE_NODE("Object Tree Node"),
	FIELD			("Field"),
	METHOD			("Method"),
	CLASS			("Class"),
	PACKAGE			("Package"),
	KEYWORD			("Keyword");

	private final String text;

	CodeCompletionType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
