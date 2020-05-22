package dd.kms.zenodot.flowcontrol;

import dd.kms.zenodot.result.CodeCompletions;

/**
 * A completion suggestion is not an error, but it "disturbs" the regular
 * parsing process. To model this alternate control flow, we use Java's
 * exception mechanism.
 */
public class InternalCodeCompletionException extends Exception
{
	private final CodeCompletions completions;

	public InternalCodeCompletionException(CodeCompletions completions) {
		this.completions = completions;
	}

	public CodeCompletions getCompletions() {
		return completions;
	}
}
