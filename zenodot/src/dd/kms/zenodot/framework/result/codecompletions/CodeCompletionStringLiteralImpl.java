package dd.kms.zenodot.framework.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionStringLiteral;

import java.util.Objects;

public class CodeCompletionStringLiteralImpl extends AbstractSimpleCodeCompletion implements CodeCompletionStringLiteral
{
	private final String	string;
	private final String	textToDisplay;

	public CodeCompletionStringLiteralImpl(String string, int insertionBegin, int insertionEnd, MatchRating rating) {
		this(string, insertionBegin, insertionEnd, rating, string);
	}

	public CodeCompletionStringLiteralImpl(String string, int insertionBegin, int insertionEnd, MatchRating rating, String textToDisplay) {
		super(CodeCompletionType.STRING_LITERAL, insertionBegin, insertionEnd, rating);
		this.string = string;
		this.textToDisplay = textToDisplay;
	}

	@Override
	public String toString() {
		return textToDisplay;
	}

	@Override
	public String getTextToInsert() {
		return string;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionStringLiteralImpl that = (CodeCompletionStringLiteralImpl) o;
		return Objects.equals(string, that.string);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), string);
	}
}
