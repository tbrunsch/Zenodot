package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.parsers.CompletionProvider;

import java.lang.reflect.Executable;

class StringLiteralCompletionEntry
{
	private final Object				owner;
	private final Executable			executable;
	private final int					parameterIndex;
	private final CompletionProvider	completionProvider;

	StringLiteralCompletionEntry(Object owner, Executable executable, int parameterIndex, CompletionProvider completionProvider) {
		this.owner = owner;
		this.executable = executable;
		this.parameterIndex = parameterIndex;
		this.completionProvider = completionProvider;
	}

	Object getOwner() {
		return owner;
	}

	Executable getExecutable() {
		return executable;
	}

	int getParameterIndex() {
		return parameterIndex;
	}

	CompletionProvider getCompletionProvider() {
		return completionProvider;
	}
}
