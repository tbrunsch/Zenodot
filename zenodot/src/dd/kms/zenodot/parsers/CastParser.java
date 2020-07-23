package dd.kms.zenodot.parsers;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.AbstractObjectParseResult;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses expressions of the form {@code (<class>) <expression>} in the context of {@code this}.
 */
public class CastParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public CastParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		tokenStream.readCharacter('(');

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ClassParseResult classParseResult = ParseUtils.parseClass(tokenStream, parserToolbox);
		TypeInfo targetType = classParseResult.getType();

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(')');

		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		return parseAndCast(tokenStream, targetType);
	}

	private ObjectParseResult parseAndCast(TokenStream tokenStream, TypeInfo targetType) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
		ObjectParseResult parseResult = parserToolbox.createParser(SimpleExpressionParser.class).parse(tokenStream, parserToolbox.getThisInfo(), new ObjectParseResultExpectation());
		ObjectInfo objectInfo = parseResult.getObjectInfo();

		try {
			ObjectInfo castInfo = parserToolbox.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return new CastParseResult(parseResult, targetType, castInfo, tokenStream.getPosition());
		} catch (ClassCastException e) {
			throw new InternalEvaluationException("Cannot cast expression to '" + targetType + "'", e);
		}
	}

	private static class CastParseResult extends AbstractObjectParseResult
	{
		private final ObjectParseResult	parseResult;
		private final TypeInfo			targetType;

		CastParseResult(ObjectParseResult parseResult, TypeInfo targetType, ObjectInfo castInfo, int position) {
			super(castInfo, position);
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
