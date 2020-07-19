package dd.kms.zenodot;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.RootpackageParser;
import dd.kms.zenodot.parsers.expectations.PackageParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.PackageParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.List;

class PackageParserImpl extends AbstractParser<PackageParseResult, PackageParseResultExpectation> implements PackageParser
{
	private static final PackageParseResultExpectation	PARSE_RESULT_EXPECTATION	= new PackageParseResultExpectation().parseWholeText(true);

	PackageParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(int caretPosition) throws ParseException {
		return getCodeCompletions(caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public PackageInfo evaluate() throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		PackageParseResult parseResult;
		try {
			parseResult = parse(tokenStream, ParseMode.EVALUATION, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		return parseResult.getPackage();
	}

	@Override
	PackageParseResult doParse(TokenStream tokenStream, ParseMode parseMode, PackageParseResultExpectation parseResultExpectation) throws InternalErrorException, InternalEvaluationException, CodeCompletionException, AmbiguousParseResultException, InternalParseException {
		ParserToolbox parserToolbox  = new ParserToolbox(InfoProvider.NULL_LITERAL, settings, parseMode);
		RootpackageParser<PackageParseResult, PackageParseResultExpectation> rootpackageParser = parserToolbox.createParser(RootpackageParser.class);
		return rootpackageParser.parse(tokenStream, null, parseResultExpectation);
	}
}
