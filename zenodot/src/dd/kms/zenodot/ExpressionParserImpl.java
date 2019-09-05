package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;
import java.util.Optional;

class ExpressionParserImpl extends AbstractParser implements ExpressionParser
{
	private final ObjectInfo	thisValue;

	public ExpressionParserImpl(String text, ParserSettings settings, ObjectInfo thisValue) {
		super(text, settings);
		this.thisValue = thisValue;
	}

	@Override
	public Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(int caretPosition) throws ParseException {
		return getCompletionSuggestions(caretPosition).getRatedSuggestions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(int caretPosition) throws ParseException {
		return getCompletionSuggestions(caretPosition).getExecutableArgumentInfo();
	}

	@Override
	public ObjectInfo evaluate() throws ParseException {
		ParseOutcome parseOutcome;

		if (!settings.isEnableDynamicTyping()) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseOutcome = parse(ParseMode.WITHOUT_EVALUATION, -1);
			if (ParseOutcomes.isParseResultOfType(parseOutcome, ParseResultType.OBJECT)) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseOutcome = parse(ParseMode.EVALUATION, -1);
			}
		} else {
			parseOutcome = parse(ParseMode.EVALUATION, -1);
		}

		if (ParseOutcomes.isParseResultOfType(parseOutcome, ParseResultType.OBJECT)) {
			ObjectParseResult result = (ObjectParseResult) parseOutcome;
			checkParsedWholeText(result);
			return result.getObjectInfo();
		}
		throw createInvalidResultTypeException(parseOutcome);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ParseMode parseMode) {
		ParserToolbox parserToolbox  = new ParserToolbox(thisValue, settings, parseMode);
		return parserToolbox.getExpressionParser().parse(tokenStream, thisValue, ParseExpectation.OBJECT);
	}
}
