package dd.kms.zenodot.framework.tokenizer;

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
