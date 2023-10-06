package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.PackageParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.expectations.PackageParseResultExpectation;
import dd.kms.zenodot.framework.result.PackageParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.impl.parsers.RootpackageParser;

import java.util.List;

public class PackageParserImpl extends AbstractParser<PackageParseResult, PackageParseResultExpectation> implements PackageParser
{
	private static final PackageParseResultExpectation	PARSE_RESULT_EXPECTATION	= new PackageParseResultExpectation().parseWholeText(true);

	public PackageParserImpl(ParserSettings settings) {
		super(settings, new VariablesImpl());
	}

	@Override
	public List<CodeCompletion> getCompletions(String text, int caretPosition) throws ParseException {
		return getCodeCompletions(text, caretPosition, InfoProvider.NULL_LITERAL, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public String evaluate(String packageName) throws ParseException {
		TokenStream tokenStream = new TokenStream(packageName, -1);
		PackageParseResult parseResult;
		try {
			parseResult = parse(tokenStream, InfoProvider.NULL_LITERAL, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream, t.getMessage(), t);
		}
		return parseResult.getPackageName();
	}

	@Override
	PackageParseResult doParse(TokenStream tokenStream, ParserToolbox parserToolbox, PackageParseResultExpectation parseResultExpectation) throws InternalErrorException, EvaluationException, CodeCompletionException, SyntaxException {
		RootpackageParser<PackageParseResult, PackageParseResultExpectation> rootpackageParser = parserToolbox.createParser(RootpackageParser.class);
		return rootpackageParser.parse(tokenStream, null, parseResultExpectation);
	}
}
