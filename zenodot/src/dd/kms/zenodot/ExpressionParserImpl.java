package dd.kms.zenodot;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

class ExpressionParserImpl extends AbstractParser<ObjectParseResult, ObjectParseResultExpectation> implements ExpressionParser
{
	private static final ObjectParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ObjectParseResultExpectation().parseWholeText(true);

	private final ObjectInfo	thisValue;

	ExpressionParserImpl(String text, ParserSettings settings, ObjectInfo thisValue) {
		super(text, settings);
		this.thisValue = thisValue;
	}

	@Override
	public List<CodeCompletion> getCompletions(int caretPosition) throws ParseException {
		return getCodeCompletions(caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(int caretPosition) throws ParseException {
		return getCodeCompletions(caretPosition, PARSE_RESULT_EXPECTATION).getExecutableArgumentInfo();
	}

	@Override
	public ObjectInfo evaluate() throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		ObjectParseResult parseResult;
		try {
			if (!settings.isEnableDynamicTyping()) {
				// first iteration without evaluation to avoid side effects when errors occur
				parse(tokenStream, ParseMode.WITHOUT_EVALUATION, PARSE_RESULT_EXPECTATION);
				tokenStream.setPosition(0);
			}
			parseResult = parse(tokenStream, ParseMode.EVALUATION, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		return parseResult.getObjectInfo();
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ParseMode parseMode, ObjectParseResultExpectation parseResultExpectation) throws InternalErrorException, InternalEvaluationException, CodeCompletionException, AmbiguousParseResultException, InternalParseException {
		ParserToolbox parserToolbox  = new ParserToolbox(thisValue, settings, parseMode);
		return parserToolbox.createExpressionParser().parse(tokenStream, thisValue, parseResultExpectation);
	}
}
