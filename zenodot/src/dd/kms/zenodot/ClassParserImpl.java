package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Map;

class ClassParserImpl extends AbstractParser implements ClassParser
{
	public ClassParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public Map<CompletionSuggestion, StringMatch> suggestCodeCompletion(int caretPosition) throws ParseException {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = getCompletionSuggestions(caretPosition).getRatedSuggestions();
		return extractNameMatchRatings(ratedSuggestions);
	}

	@Override
	public ClassInfo evaluate() throws ParseException {
		ParseOutcome parseOutcome = parse(ParseMode.EVALUATION, -1);
		if (ParseOutcomes.isParseResultOfType(parseOutcome, ParseResultType.CLASS)) {
			ClassParseResult result = (ClassParseResult) parseOutcome;
			checkParsedWholeText(result);
			TypeInfo type = result.getType();
			return InfoProvider.createClassInfoUnchecked(type.getRawType().getName());
		}
		throw createInvalidResultTypeException(parseOutcome);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ParseMode parseMode) {
		ParserToolbox parserToolbox  = new ParserToolbox(InfoProvider.NULL_LITERAL, settings, parseMode);
		return ParseUtils.parse(tokenStream, null, ParseExpectation.CLASS,
			parserToolbox.getImportedClassParser(),
			parserToolbox.getRootpackageParser()
		);
	}
}
