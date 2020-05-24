package dd.kms.zenodot.tokenstream;

import dd.kms.zenodot.flowcontrol.InternalCodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ParseError;
import dd.kms.zenodot.tokenizer.CompletionGenerator;
import dd.kms.zenodot.tokenizer.ParseExceptionGenerator;
import dd.kms.zenodot.tokenizer.TokenStream;
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
	public void testReadOneOfMultipleCharacters() throws InternalParseException, InternalCodeCompletionException {
		TokenStream tokenStream = new TokenStream(expression, -1);

		CompletionGenerator identifierCompletionGenerator = info -> new InternalCodeCompletionException(CodeCompletions.none(info.getCaretPosition()));
		ParseExceptionGenerator identifierExceptionGenerator = stream -> new InternalParseException(stream.getPosition(), "Unexpected parse exception", ParseError.ErrorPriority.INTERNAL_ERROR);
		tokenStream.readIdentifier(identifierCompletionGenerator, identifierExceptionGenerator);

		char tailCharacter = tokenStream.readCharacter(ParseError.ErrorPriority.RIGHT_PARSER, '.', '[', TokenStream.EMPTY_CHARACTER);
		char expectedCharacter =	expression.endsWith(".")	? '.' :
									expression.endsWith("[")	? '['
																: TokenStream.EMPTY_CHARACTER;
		Assert.assertEquals("Unexpected tail character", expectedCharacter, tailCharacter);
	}
}
