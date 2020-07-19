package dd.kms.zenodot.parsers;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses expressions of the form {@code (<expression>)} in the context of {@code this}.
 */
public class ParenthesizedExpressionParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public ParenthesizedExpressionParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		tokenStream.readCharacter('(');

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		ObjectParseResult parseResult = parserToolbox.createExpressionParser().parse(tokenStream, contextInfo, expectation);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(')');
		return parseResult;
	}
}
