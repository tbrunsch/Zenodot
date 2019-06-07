package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.Map;

class PackageParserImpl extends AbstractParser implements PackageParser
{
	public PackageParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public Map<CompletionSuggestion, StringMatch> suggestCodeCompletion(int caretPosition) throws ParseException {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = getCompletionSuggestions(caretPosition).getRatedSuggestions();
		return extractNameMatchRatings(ratedSuggestions);
	}

	@Override
	public PackageInfo evaluate() throws ParseException {
		ParseOutcome parseOutcome = parse(ParseMode.EVALUATION, -1);
		ParseOutcomeType resultType = parseOutcome.getOutcomeType();
		if (resultType == ParseOutcomeType.PACKAGE_PARSE_RESULT) {
			PackageParseResult result = (PackageParseResult) parseOutcome;
			checkParsedWholeText(result);
			return result.getPackage();
		}
		handleInvalidResultType(parseOutcome);
		return null;
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ParseMode parseMode) {
		ParserToolbox parserToolbox  = new ParserToolbox(InfoProvider.createObjectInfo(null, InfoProvider.NO_TYPE), settings, parseMode);
		return parserToolbox.getRootpackageParser().parse(tokenStream, null, ParseExpectation.PACKAGE);
	}
}
