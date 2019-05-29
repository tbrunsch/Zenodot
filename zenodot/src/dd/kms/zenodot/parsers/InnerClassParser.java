package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Arrays;
import java.util.Optional;

/**
 * Parses subexpressions {@code <inner class>} of expressions of the form {@code <class>.<inner class>}.
 * The class {@code <class>} is the context for the parser.
 */
public class InnerClassParser extends AbstractEntityParser<TypeInfo>
{
	public InnerClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, TypeInfo contextType, ParseExpectation expectation) {
		ParseResult innerClassParseResult = readInnerClass(tokenStream, contextType);

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(innerClassParseResult, ParseExpectation.CLASS, ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}

		ClassParseResult parseResult = (ClassParseResult) innerClassParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo innerClassType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, innerClassType, expectation);
	}

	private ParseResult readInnerClass(TokenStream tokenStream, TypeInfo contextType) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();

		Class<?> contextClass = contextType.getRawType();
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			return classDataProvider.suggestInnerClasses("", contextClass, startPosition, startPosition);
		}

		Token identifierToken;
		try {
			identifierToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			return ParseResults.createParseError(startPosition, "Expected inner class name", ErrorPriority.WRONG_PARSER);
		}

		String innerClassName = identifierToken.getValue();

		if (identifierToken.isContainsCaret()) {
			return classDataProvider.suggestInnerClasses(innerClassName, contextClass, startPosition, tokenStream.getPosition());
		}

		Optional<Class<?>> firstClassMatch = Arrays.stream(contextClass.getDeclaredClasses())
			.filter(clazz -> clazz.getSimpleName().equals(innerClassName))
			.findFirst();
		if (!firstClassMatch.isPresent()) {
			return ParseResults.createParseError(startPosition, "Unknown inner class '" + innerClassName + "'", ErrorPriority.WRONG_PARSER);
		}

		Class<?> innerClass = firstClassMatch.get();
		TypeInfo innerClassType = contextType.resolveType(innerClass);
		return ParseResults.createClassParseResult(tokenStream.getPosition(), innerClassType);
	}
}
