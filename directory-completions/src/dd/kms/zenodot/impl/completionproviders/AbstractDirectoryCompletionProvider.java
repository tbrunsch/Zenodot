package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.framework.result.codecompletions.CodeCompletionStringLiteralImpl;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDirectoryCompletionProvider<P> implements CompletionProvider
{
	@Nullable
	protected abstract P doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception;

	protected abstract List<? extends CodeCompletion> doGetCodeCompletions(@Nullable P parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext);

	protected CodeCompletion createCodeCompletion(String childName, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode) {
		return createCodeCompletion(childName, childCompletionInfo, completionMode, childName);
	}

	protected CodeCompletion createCodeCompletion(String childName, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, String childNameToDisplay) {
		String expectedChildName = completionMode.getTextToComplete(childCompletionInfo.getTokenTextUntilCaret(), childCompletionInfo.getTokenText());
		StringMatch nameMatch = MatchRatings.rateStringMatch(expectedChildName, childName);
		MatchRating rating = MatchRatings.create(nameMatch, TypeMatch.NONE, false);
		int insertionBegin = completionMode.getInsertionBegin(childCompletionInfo.getTokenStartPosition(), childCompletionInfo.getTokenTextStartPosition());
		int insertionEnd = completionMode.getInsertionEnd(childCompletionInfo.getCaretPosition(), childCompletionInfo.getTokenTextEndPosition(), childCompletionInfo.getTokenEndPosition());
		return new CodeCompletionStringLiteralImpl(childName, insertionBegin, insertionEnd, rating, childNameToDisplay);
	}

	@Override
	public List<? extends CodeCompletion> getCodeCompletions(CompletionInfo completionInfo, CompletionMode completionMode, CallerContext callerContext) {
		ChildCompletionInfo childCompletionInfo = new ChildCompletionInfo(completionInfo);
		P parent;
		try {
			parent = doGetParent(childCompletionInfo.getParentPath(), callerContext);
		} catch (Exception e) {
			return Collections.emptyList();
		}
		return doGetCodeCompletions(parent, childCompletionInfo, completionMode, callerContext);
	}

	/**
	 * The completion info we get by Zenodot refers to the whole path string. However,
	 * we do not want to complete the whole path, but only the child the caret
	 * is currently on. This class represents this part of the full path.
	 */
	static class ChildCompletionInfo implements CompletionInfo
	{
		private final CompletionInfo	fullPathCompletionInfo;
		private final String			parentPath;
		private final int				childBeginPos;
		private final int				childEndPos;

		ChildCompletionInfo(CompletionInfo fullPathCompletionInfo) {
			this.fullPathCompletionInfo = fullPathCompletionInfo;

			String text = fullPathCompletionInfo.getTokenText();
			int relativeCaretPosition = fullPathCompletionInfo.getCaretPosition() - fullPathCompletionInfo.getTokenTextStartPosition();
			int separatorPos = relativeCaretPosition - 1;
			while (separatorPos >= 0 && !isSeparator(text.charAt(separatorPos))) {
				separatorPos--;
			}
			if (separatorPos >= 0) {
				childBeginPos = separatorPos + 1;
				parentPath = text.substring(0, childBeginPos);
			} else {
				childBeginPos = 0;
				parentPath = null;
			}

			separatorPos = relativeCaretPosition;
			while (separatorPos < text.length() && !isSeparator(text.charAt(separatorPos))) {
				separatorPos++;
			}
			childEndPos = separatorPos;
		}

		String getParentPath() {
			return parentPath;
		}

		@Override
		public int getTokenStartPosition() {
			return childBeginPos == 0
				? fullPathCompletionInfo.getTokenStartPosition()
				: getTokenTextStartPosition();
		}

		@Override
		public int getTokenEndPosition() {
			return childEndPos == fullPathCompletionInfo.getTokenText().length()
				? fullPathCompletionInfo.getTokenEndPosition()
				: getTokenTextEndPosition();
		}

		@Override
		public int getTokenTextStartPosition() {
			return fullPathCompletionInfo.getTokenTextStartPosition() + childBeginPos;
		}

		@Override
		public int getTokenTextEndPosition() {
			return fullPathCompletionInfo.getTokenTextStartPosition() + childEndPos;
		}

		@Override
		public int getCaretPosition() {
			return fullPathCompletionInfo.getCaretPosition();
		}

		@Override
		public String getTokenTextUntilCaret() {
			return fullPathCompletionInfo.getTokenTextUntilCaret().substring(childBeginPos);
		}

		@Override
		public String getTokenText() {
			return fullPathCompletionInfo.getTokenText().substring(childBeginPos, childEndPos);

		}

		private static boolean isSeparator(char c) {
			return c == '/' || c == '\\';
		}
	}
}
