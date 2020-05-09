package dd.kms.zenodot.result;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

/**
 * A collection of {@link CodeCompletion} including their ratings
 */
public class CodeCompletions implements ParseOutcome
{
	public static CodeCompletions none(int position) {
		return new CodeCompletions(position, ImmutableList.of());
	}

	public static CodeCompletions of(CodeCompletion codeCompletion) {
		return new CodeCompletions(codeCompletion.getInsertionRange().getEnd(), ImmutableList.of(codeCompletion));
	}

	private final int								position;
	private final List<CodeCompletion>				completions;
	private final Optional<ExecutableArgumentInfo>	executableArgumentInfo;

	public CodeCompletions(int position, List<CodeCompletion> completions) {
		this(position, completions, Optional.empty());
	}

	public CodeCompletions(int position, List<CodeCompletion> completions, Optional<ExecutableArgumentInfo> executableArgumentInfo) {
		this.position = position;
		this.completions = completions;
		this.executableArgumentInfo = executableArgumentInfo;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.CODE_COMPLETIONS;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public List<CodeCompletion> getCompletions() {
		return completions;
	}

	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo() {
		return executableArgumentInfo;
	}
}
