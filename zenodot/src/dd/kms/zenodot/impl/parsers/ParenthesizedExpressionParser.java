package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

/**
 * Parses expressions of the form {@code (<expression>)} in the context of {@code this}.
 */
public class ParenthesizedExpressionParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public ParenthesizedExpressionParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		tokenStream.readCharacter('(');

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		ObjectParseResult parseResult = parserToolbox.createExpressionParser().parse(tokenStream, contextInfo, expectation);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(')');
		return parseResult;
	}
}
