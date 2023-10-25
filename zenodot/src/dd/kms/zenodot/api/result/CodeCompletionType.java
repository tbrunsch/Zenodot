package dd.kms.zenodot.api.result;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ordered by priority of code completions
 */
public class CodeCompletionType implements Comparable<CodeCompletionType>
{
	private static final Map<String, CodeCompletionType>	REGISTERED_CODE_COMPLETION_TYPES	= new LinkedHashMap<>();

	public static final CodeCompletionType	VARIABLE			= register("Variable",	true);
	public static final CodeCompletionType	FIELD				= register("Field",		true);
	public static final CodeCompletionType	METHOD				= register("Method",	true);
	public static final CodeCompletionType	CLASS				= register("Class",		true);
	public static final CodeCompletionType	PACKAGE				= register("Package",	true);
	public static final CodeCompletionType	KEYWORD				= register("Keyword",	true);

	public static CodeCompletionType register(String text) {
		return register(text, false);
	}

	private static CodeCompletionType register(String text, boolean predefined) {
		if (REGISTERED_CODE_COMPLETION_TYPES.containsKey(text)) {
			throw new IllegalArgumentException("A code completion type with text '" + text + "' is already registered");
		}
		int ordinal = REGISTERED_CODE_COMPLETION_TYPES.size();
		CodeCompletionType codeCompletionType = new CodeCompletionType(text, ordinal, predefined);
		REGISTERED_CODE_COMPLETION_TYPES.put(text, codeCompletionType);
		return codeCompletionType;
	}

	public static CodeCompletionType[] values() {
		return REGISTERED_CODE_COMPLETION_TYPES.values().toArray(new CodeCompletionType[0]);
	}

	private final String	text;
	private final int		ordinal;
	private final boolean	predefined;

	private CodeCompletionType(String text, int ordinal, boolean predefined) {
		this.text = text;
		this.ordinal = ordinal;
		this.predefined = predefined;
	}

	public int ordinal() {
		return ordinal;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public int compareTo(CodeCompletionType that) {
		if (this == that) {
			return 0;
		}
		int predefinedCompareResult = Boolean.compare(predefined, that.predefined);
		if (predefinedCompareResult != 0) {
			return predefinedCompareResult;
		}
		return Integer.compare(ordinal, that.ordinal);
	}
}
