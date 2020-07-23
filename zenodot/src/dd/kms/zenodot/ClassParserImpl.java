package dd.kms.zenodot;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

class ClassParserImpl extends AbstractParser<ClassParseResult, ClassParseResultExpectation> implements ClassParser
{
	private static final ClassParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ClassParseResultExpectation().parseWholeText(true);

	ClassParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(int caretPosition) throws ParseException {
		return getCodeCompletions(InfoProvider.NULL_LITERAL, caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public ClassInfo evaluate() throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		ClassParseResult parseResult;
		try {
			parseResult = parse(tokenStream, InfoProvider.NULL_LITERAL, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		TypeInfo type = parseResult.getType();
		return InfoProvider.createClassInfoUnchecked(type.getRawType().getName());
	}

	@Override
	ClassParseResult doParse(TokenStream tokenStream, ParserToolbox parserToolbox, ClassParseResultExpectation parseResultExpectation) throws CodeCompletionException, InternalErrorException, AmbiguousParseResultException, InternalParseException, InternalEvaluationException {
		return ParseUtils.parseClass(tokenStream, parserToolbox);
	}
}
