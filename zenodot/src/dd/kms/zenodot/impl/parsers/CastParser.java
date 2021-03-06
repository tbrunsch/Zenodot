package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.AbstractObjectParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

/**
 * Parses expressions of the form {@code (<class>) <expression>} in the context of {@code this}.
 */
public class CastParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public CastParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		tokenStream.readCharacter('(');

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ClassParseResult classParseResult = ParseUtils.parseClass(tokenStream, parserToolbox);
		TypeInfo targetType = classParseResult.getType();

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(')');

		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		return parseAndCast(tokenStream, targetType);
	}

	private ObjectParseResult parseAndCast(TokenStream tokenStream, TypeInfo targetType) throws CodeCompletionException, SyntaxException, EvaluationException, InternalErrorException {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
		ObjectParseResult parseResult = parserToolbox.createParser(SimpleExpressionParser.class).parse(tokenStream, parserToolbox.getThisInfo(), new ObjectParseResultExpectation());
		ObjectInfo objectInfo = parseResult.getObjectInfo();

		try {
			ObjectInfo castInfo = parserToolbox.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return new CastParseResult(parseResult, targetType, castInfo, tokenStream);
		} catch (ClassCastException e) {
			throw new EvaluationException("Cannot cast expression to '" + targetType + "'", e);
		}
	}

	private static class CastParseResult extends AbstractObjectParseResult
	{
		private final ObjectParseResult	parseResult;
		private final TypeInfo			targetType;

		CastParseResult(ObjectParseResult parseResult, TypeInfo targetType, ObjectInfo castInfo, TokenStream tokenStream) {
			super(castInfo, tokenStream);
			this.parseResult = parseResult;
			this.targetType = targetType;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws ParseException {
			ObjectInfo objectInfo = parseResult.evaluate(thisInfo, contextInfo);
			return OBJECT_INFO_PROVIDER.getCastInfo(objectInfo, targetType);
		}
	}
}
