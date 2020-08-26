package dd.kms.zenodot.impl.result;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A collection of {@link CodeCompletion} including their ratings
 */
public class CodeCompletions
{
	public static final CodeCompletions	NONE	= new CodeCompletions(ImmutableList.of());

	public static CodeCompletions of(CodeCompletion codeCompletion) {
		return new CodeCompletions(ImmutableList.of(codeCompletion));
	}

	private final List<CodeCompletion>				completions;
	private final @Nullable ExecutableArgumentInfo	executableArgumentInfo;

	public CodeCompletions(List<CodeCompletion> completions) {
		this(completions, null);
	}

	public CodeCompletions(List<CodeCompletion> completions, ExecutableArgumentInfo executableArgumentInfo) {
		this.completions = completions;
		this.executableArgumentInfo = executableArgumentInfo;
	}

	public List<CodeCompletion> getCompletions() {
		return completions;
	}

	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo() {
		return Optional.ofNullable(executableArgumentInfo);
	}
}
