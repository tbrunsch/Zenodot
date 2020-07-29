package dd.kms.zenodot.api.settings;

public enum CompletionMode
{
	/**
	 * The word the caret is on until the caret is used to propose completions.
	 * That text will be replaced by the selected completion.
	 */
	COMPLETE_AND_REPLACE_UNTIL_CARET,

	/**
	 * The word the caret is on until the caret is used to propose completions.
	 * The whole word will be replaced by the selected completion.
	 */
	COMPLETE_UNTIL_CARET_REPLACE_WHOLE_WORDS,

	/**
	 * The word the caret is on is used to propose completions.
	 * That word will be replaced by the selected completion.
	 */
	COMPLETE_AND_REPLACE_WHOLE_WORDS
}
