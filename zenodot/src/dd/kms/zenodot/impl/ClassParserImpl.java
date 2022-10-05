package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.ClassParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

import java.util.List;

public class ClassParserImpl extends AbstractParser<ClassParseResult, ClassParseResultExpectation> implements ClassParser
{
	private static final ClassParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ClassParseResultExpectation().parseWholeText(true);

	public ClassParserImpl(ParserSettings settings) {
		super(settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(String text, int caretPosition) throws ParseException {
		return getCodeCompletions(text, caretPosition, null, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public Class<?> evaluate(String className) throws ParseException {
		TokenStream tokenStream = new TokenStream(className, -1);
		ClassParseResult parseResult;
		try {
			parseResult = parse(tokenStream, InfoProvider.NULL_LITERAL, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream, t.getMessage(), t);
		}
		return parseResult.getType();
	}

	@Override
	ClassParseResult doParse(TokenStream tokenStream, ParserToolbox parserToolbox, ClassParseResultExpectation parseResultExpectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		return ParseUtils.parseClass(tokenStream, parserToolbox);
	}
}
