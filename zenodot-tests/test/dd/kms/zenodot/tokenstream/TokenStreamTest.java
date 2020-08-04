package dd.kms.zenodot.tokenstream;

import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.tokenizer.CompletionGenerator;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TokenStreamTest
{
	private final List<TokenDescription>	tokenDescriptions;

	public TokenStreamTest(List<TokenDescription> tokenDescriptions) {
		this.tokenDescriptions = tokenDescriptions;
	}

	@Parameters(name = "Tokens: {0}")
	public static List<List<TokenDescription>> getTokenDescriptions() {
		List<List<TokenDescription>> tokenDescriptions = new ArrayList<>();
		for (int length = 1; length <= 4; length++) {
			tokenDescriptions.addAll(createTokenDescriptionLists(length));
		}
		return tokenDescriptions;
	}

	private static List<List<TokenDescription>> createTokenDescriptionLists(int length) {
		if (length == 0) {
			List<List<TokenDescription>> lists = new ArrayList<>();
			lists.add(new ArrayList<>());
			return lists;
		}
		List<List<TokenDescription>> shorterLists = createTokenDescriptionLists(length-1);
		List<List<TokenDescription>> lists = new ArrayList<>();
		List<TokenDescription> tokenDescriptions;
		int minNumSpacesBefore = length == 1 ? 0 : 1;
		for (List<TokenDescription> shorterTokenDescriptions : shorterLists) {
			for (int numSpacesBefore : new int[]{ minNumSpacesBefore, 3 }) {
				tokenDescriptions = new ArrayList<>(shorterTokenDescriptions);
				tokenDescriptions.add(new CharacterTokenDescription('(', numSpacesBefore));
				lists.add(tokenDescriptions);
				tokenDescriptions = new ArrayList<>(shorterTokenDescriptions);
				tokenDescriptions.add(new IdentifierTokenDescription("identifier", numSpacesBefore));
				lists.add(tokenDescriptions);
			}
		}
		return lists;
	}

	@Test
	public void testReadTokens() throws SyntaxException, CodeCompletionException, InternalErrorException {
		String expression = tokenDescriptions.stream().map(TokenDescription::toString).collect(Collectors.joining());
		TokenStream tokenStream = new TokenStream(expression, -1);
		for (TokenDescription tokenDescription : tokenDescriptions) {
			if (tokenDescription instanceof CharacterTokenDescription) {
				CharacterTokenDescription description = (CharacterTokenDescription) tokenDescription;
				char c = tokenStream.readCharacter(description.getCharacter());
				Assert.assertEquals("Deviating characters", description.getCharacter(), c);
			} else {
				IdentifierTokenDescription description = (IdentifierTokenDescription) tokenDescription;
				String identifier = tokenStream.readIdentifier(TokenStream.NO_COMPLETIONS, "Unexpected parse exception when parsing '" + expression + "'");
				Assert.assertEquals("Deviating identifiers", description.getIdentifier(), identifier);
			}
		}
	}

	@Test
	public void testException() throws SyntaxException, CodeCompletionException, InternalErrorException {
		String expression = tokenDescriptions.stream().map(TokenDescription::toString).collect(Collectors.joining());
		TokenStream tokenStream = new TokenStream(expression, -1);

		// Read all but last token correctly
		int numTokens = tokenDescriptions.size();
		for (int i = 0; i < numTokens - 1; i++) {
			TokenDescription tokenDescription = tokenDescriptions.get(i);
			if (tokenDescription instanceof CharacterTokenDescription) {
				CharacterTokenDescription description = (CharacterTokenDescription) tokenDescription;
				tokenStream.readCharacter(description.getCharacter());
			} else {
				tokenStream.readIdentifier(TokenStream.NO_COMPLETIONS, "Unexpected parse exception when parsing '" + expression + "'");
			}
		}

		// Pretend to expect something different for the last token
		TokenDescription tokenDescription = tokenDescriptions.get(numTokens - 1);
		int errorPos = getTokenEndPosition(tokenDescriptions.subList(0, tokenDescriptions.size()-1)) + tokenDescription.getNumSpacesBefore();
		try {
			if (tokenDescription instanceof CharacterTokenDescription) {
				tokenStream.readIdentifier(TokenStream.NO_COMPLETIONS, "Expected parse exception when parsing '" + expression + "'");
			} else {
				tokenStream.readCharacter('(');
			}
		} catch (SyntaxException e) {
			Assert.assertEquals("Deviating parse error positions", errorPos, tokenStream.getPosition());
			return;
		} catch (CodeCompletionException e) {
			Assert.fail("Unexpected code completion");
		}
		Assert.fail("Expected a parse exception");
	}

	@Test
	public void testCompletion() throws SyntaxException {
		String expression = tokenDescriptions.stream().map(TokenDescription::toString).collect(Collectors.joining());
		final int numTokens = tokenDescriptions.size();
		int lastTokenIndex = numTokens - 1;
		TokenDescription lastTokenDescription = tokenDescriptions.get(lastTokenIndex);
		int firstCaretPos = getTokenEndPosition(tokenDescriptions.subList(0, lastTokenIndex));
		int lastCaretPos = getTokenEndPosition(tokenDescriptions);

		for (int caretPos = firstCaretPos; caretPos <= lastCaretPos; caretPos++) {
			String expr = expression.replace(" ", "°");
			String context = "expression \"" + expr.substring(0, caretPos) + "^" + expr.substring(caretPos) + "\", caret position " + caretPos;
			TokenStream tokenStream = new TokenStream(expression, caretPos);

			int tokenDescriptionStartPos = 0;
			boolean prevTokenWasIdentifierToken = false;
			for (int i = 0; i < lastTokenIndex; i++) {
				TokenDescription tokenDescription = tokenDescriptions.get(i);

				// Test current position in token stream
				int startPos = tokenStream.getPosition();
				int expectedStartPos = prevTokenWasIdentifierToken
					? tokenDescriptionStartPos + tokenDescription.getNumSpacesBefore()	// leading spaces of this token have already been parse
					: tokenDescriptionStartPos;
				Assert.assertEquals("Unexpected token stream position for " + context, expectedStartPos, startPos);

				if (tokenDescription instanceof CharacterTokenDescription) {
					// Character token: No completions expected
					CharacterTokenDescription description = (CharacterTokenDescription) tokenDescription;
					try {
						tokenStream.readCharacter(description.getCharacter());
					} catch (CodeCompletionException e) {
						Assert.fail("Unexpected code completion for " + context + ": " + e.getMessage());
					} catch (InternalErrorException e) {
						Assert.fail(e.getMessage());
					}
					prevTokenWasIdentifierToken = false;
				} else {
					/*
					 * Identifier token: Completions only expected if
					 *
					 * - second to last token and
					 * - caret inside spaces after this token (= spaces before last token)
					 */
					int capturedCaretPos = caretPos;
					boolean expectCodeCompletion = i == lastTokenIndex - 1
						&& capturedCaretPos < firstCaretPos + lastTokenDescription.getNumSpacesBefore();
					final int textStartPos, textEndPos, endPos;
					if (expectCodeCompletion) {
						textStartPos = prevTokenWasIdentifierToken ? startPos : startPos + tokenDescription.getNumSpacesBefore();
						textEndPos = textStartPos + tokenDescription.getTokenText().length();
						endPos = textEndPos + lastTokenDescription.getNumSpacesBefore();
					} else {
						textStartPos = textEndPos = endPos = -1;
					}
					CompletionGenerator completionGenerator = info -> {
						if (expectCodeCompletion) {
							Assert.assertEquals("Deviating token start positions for " + context,	startPos,		info.getTokenStartPosition());
							Assert.assertEquals("Deviating text start positions for " + context,	textStartPos,	info.getTokenTextStartPosition());
							Assert.assertEquals("Deviating text end positions for " + context,		textEndPos,		info.getTokenTextEndPosition());
							Assert.assertEquals("Deviating token end positions for " + context,		endPos,			info.getTokenEndPosition());
						} else {
							Assert.fail("Unexpected code completion for caret position " + capturedCaretPos);
						}
						return CodeCompletions.NONE;
					};
					try {
						tokenStream.readIdentifier(completionGenerator, "Unexpected parse exception for " + context);
					} catch (CodeCompletionException e) {
						Assert.assertTrue("Unexpected code completion for " + context + ": " + e.getMessage(), expectCodeCompletion);
						return;
					}
					Assert.assertFalse("Did not encounter a code completion for " + context, expectCodeCompletion);
					prevTokenWasIdentifierToken = true;
				}
				tokenDescriptionStartPos += tokenDescription.getTokenLength();
			}

			/*
			 * If we reach this position, then the caret is not within the spaces before the last token
			 * or the previous token was not an identifier token.
			 */
			Assert.assertTrue("Previous token was an identifier token and the caret is within the spaces before the last token for " + context, !prevTokenWasIdentifierToken || caretPos >= tokenDescriptionStartPos + lastTokenDescription.getNumSpacesBefore());

			// Test current position in token stream
			int startPos = tokenStream.getPosition();
			int expectedStartPos = prevTokenWasIdentifierToken
				? tokenDescriptionStartPos + lastTokenDescription.getNumSpacesBefore()	// leading spaces of last token have already been parse
				: tokenDescriptionStartPos;
			Assert.assertEquals("Unexpected token stream position for " + context, expectedStartPos, startPos);

			if (lastTokenDescription instanceof CharacterTokenDescription) {
				// Character token: Completions only expected within spaces before this token
				CharacterTokenDescription description = (CharacterTokenDescription) lastTokenDescription;
				boolean caretWithinWhitespacesBeforeLastToken = caretPos <=  tokenDescriptionStartPos + lastTokenDescription.getNumSpacesBefore();
				try {
					tokenStream.readCharacter(description.getCharacter());
				} catch (CodeCompletionException e) {
					Assert.assertTrue("Unexpected code completion for " + context + ": " + e.getMessage(), caretWithinWhitespacesBeforeLastToken);
				} catch (InternalErrorException e) {
					Assert.fail(e.getMessage());
				}
			} else {
				// Identifier token: Completions expected
				final int textStartPos, textEndPos, endPos;
				if (caretPos < tokenDescriptionStartPos + lastTokenDescription.getNumSpacesBefore()) {
					textStartPos = textEndPos = endPos = caretPos;
				} else {
					textStartPos = prevTokenWasIdentifierToken ? startPos + lastTokenDescription.getNumSpacesBefore() : startPos;
					textEndPos = textStartPos + lastTokenDescription.getTokenText().length();
					endPos = textEndPos;
				}
				CompletionGenerator completionGenerator = info -> {
					Assert.assertEquals("Deviating token start positions for " + context,	startPos,		info.getTokenStartPosition());
					Assert.assertEquals("Deviating text start positions for " + context,	textStartPos,	info.getTokenTextStartPosition());
					Assert.assertEquals("Deviating text end positions for " + context,		textEndPos,		info.getTokenTextEndPosition());
					Assert.assertEquals("Deviating token end positions for " + context,		endPos,			info.getTokenEndPosition());
					return CodeCompletions.NONE;
				};
				try {
					tokenStream.readIdentifier(completionGenerator, "Unexpected parse exception for " + context);
				} catch (CodeCompletionException e) {
					return;
				}
				Assert.fail("Did not encounter a code completion for " + context);
			}
		}
	}

	private int getTokenEndPosition(List<TokenDescription> tokenDescriptions) {
		return tokenDescriptions.stream()
			.mapToInt(TokenDescription::getTokenLength)
			.sum();
	}
}
