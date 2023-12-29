package dd.kms.zenodot.impl.settings.extensions;

import dd.kms.zenodot.api.settings.extensions.CompletionProvider;

import java.lang.reflect.Executable;
import java.util.Objects;

class StringLiteralCompletionEntry
{
	private final Executable			executable;
	private final int					parameterIndex;
	private final CompletionProvider	completionProvider;

	StringLiteralCompletionEntry(Executable executable, int parameterIndex, CompletionProvider completionProvider) {
		this.executable = executable;
		this.parameterIndex = parameterIndex;
		this.completionProvider = completionProvider;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StringLiteralCompletionEntry that = (StringLiteralCompletionEntry) o;
		return parameterIndex == that.parameterIndex && Objects.equals(executable, that.executable) && Objects.equals(completionProvider, that.completionProvider);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executable, parameterIndex, completionProvider);
	}
}
