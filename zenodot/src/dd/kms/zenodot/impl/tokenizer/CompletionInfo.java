package dd.kms.zenodot.impl.tokenizer;

public interface CompletionInfo
{
	int getTokenStartPosition();
	int getTokenEndPosition();
	int getTokenTextStartPosition();
	int getTokenTextEndPosition();
	int getCaretPosition();
	String getTokenTextUntilCaret();
	String getTokenText();
}
