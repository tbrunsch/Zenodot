package dd.kms.zenodot.impl.flowcontrol;

import dd.kms.zenodot.impl.result.CodeCompletions;

/**
 * A completion suggestion is not an error, but it "disturbs" the regular
 * parsing process. To model this alternate control flow, we use Java's
 * exception mechanism.
 */
public class CodeCompletionException extends Exception
{
	private final CodeCompletions completions;

	public CodeCompletionException(CodeCompletions completions) {
		this.completions = completions;
	}

	public CodeCompletions getCompletions() {
		return completions;
	}
}
