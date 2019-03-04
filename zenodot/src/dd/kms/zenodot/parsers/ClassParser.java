package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public class ClassParser extends AbstractEntityParser<ObjectInfo>
{
	public ClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing class");
		ParseResultIF classParseResult = parserToolbox.getClassDataProvider().readClass(tokenStream);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		if (ParseUtils.propagateParseResult(classParseResult, ParseExpectation.CLASS)) {
			return classParseResult;
		}

		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, type, expectation);
	}
}
