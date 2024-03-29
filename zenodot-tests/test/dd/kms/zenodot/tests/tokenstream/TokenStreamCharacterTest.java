package dd.kms.zenodot.tests.tokenstream;

import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class TokenStreamCharacterTest
{
	private static final String	IDENTIFIER	= "field";

	@Parameterized.Parameters(name = "Expression: field{0}")
	public static List<String> getExpressionSuffixes() {
		return Arrays.asList("", ".", "[", "(");
	}

	private final String	expression;

	public TokenStreamCharacterTest(String expressionSuffix) {
		this.expression = IDENTIFIER + expressionSuffix;
	}

	@Test
	public void testReadOneOfMultipleCharacters() throws SyntaxException, CodeCompletionException, InternalErrorException {
		TokenStream tokenStream = new TokenStream(expression, -1);
		tokenStream.readIdentifier(TokenStream.NO_COMPLETIONS, "Unexpected parse exception");

		char tailCharacter = tokenStream.readCharacter('.', '[', TokenStream.EMPTY_CHARACTER);
		char expectedCharacter =	expression.endsWith(".")	? '.' :
									expression.endsWith("[")	? '['
																: TokenStream.EMPTY_CHARACTER;
		Assert.assertEquals("Unexpected tail character", expectedCharacter, tailCharacter);
	}
}
