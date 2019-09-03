package dd.kms.zenodot.result;

/**
 * Ordered by priority for suggestions
 */
public enum CompletionSuggestionType
{
	VARIABLE		("Variable"),
	OBJECT_TREE_NODE("Object Tree Node"),
	FIELD			("Field"),
	METHOD			("Method"),
	CLASS			("Class"),
	PACKAGE			("Package"),
	KEYWORD			("Keyword");

	private final String text;

	CompletionSuggestionType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
