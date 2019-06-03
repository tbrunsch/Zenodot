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
import dd.kms.zenodot.utils.wrappers.PackageInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
		ParseResult parseResult = parse(ParseMode.EVALUATION, -1);
		ParseResultType resultType = parseResult.getResultType();
		if (resultType == ParseResultType.PACKAGE_PARSE_RESULT) {
			PackageParseResult result = (PackageParseResult) parseResult;
			checkParsedWholeText(result);
			return result.getPackage();
		}
		handleInvalidResultType(parseResult);
		return null;
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ParseMode parseMode) {
		ParserToolbox parserToolbox  = new ParserToolbox(InfoProvider.createObjectInfo(null, InfoProvider.NO_TYPE), settings, parseMode);
		return parserToolbox.getRootpackageParser().parse(tokenStream, null, ParseExpectation.PACKAGE);
	}
}
