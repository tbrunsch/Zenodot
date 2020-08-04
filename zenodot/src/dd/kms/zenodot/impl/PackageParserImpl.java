package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.PackageParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.PackageParseResult;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.PackageInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.RootpackageParser;
import dd.kms.zenodot.impl.parsers.expectations.PackageParseResultExpectation;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;

import java.util.List;

public class PackageParserImpl extends AbstractParser<PackageParseResult, PackageParseResultExpectation> implements PackageParser
{
	private static final PackageParseResultExpectation	PARSE_RESULT_EXPECTATION	= new PackageParseResultExpectation().parseWholeText(true);

	public PackageParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(int caretPosition) throws ParseException {
		return getCodeCompletions(InfoProvider.NULL_LITERAL, caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public PackageInfo evaluate() throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		PackageParseResult parseResult;
		try {
			parseResult = parse(tokenStream, InfoProvider.NULL_LITERAL, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		return parseResult.getPackage();
	}

	@Override
	PackageParseResult doParse(TokenStream tokenStream, ParserToolbox parserToolbox, PackageParseResultExpectation parseResultExpectation) throws InternalErrorException, EvaluationException, CodeCompletionException, SyntaxException {
		RootpackageParser<PackageParseResult, PackageParseResultExpectation> rootpackageParser = parserToolbox.createParser(RootpackageParser.class);
		return rootpackageParser.parse(tokenStream, null, parseResultExpectation);
	}
}
