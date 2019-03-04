package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseError;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions
 * <ul>
 *     <li>{@code .<field>} of expressions of the form {@code <instance>.<field>},</li>
 *     <li>{@code .<method>(<arguments>)} of expressions of the form {@code <instance>.<method>(<arguments>)}, and</li>
 *     <li>{@code [<array index>]} of expressions of the form {@code <instance>[<array index>]}.</li>
 * </ul>
 * The instance {@code <instance>} is the context for the parser. If the subexpression neither starts with a dot ({@code .})
 * nor an opening bracket ({@code [}), then {@code <instance>} is returned as parse result.
 */
public class ObjectTailParser extends AbstractTailParser<ObjectInfo>
{
	public ObjectTailParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractEntityParser<ObjectInfo> fieldParser = parserToolbox.getObjectFieldParser();
		AbstractEntityParser<ObjectInfo> methodParser = parserToolbox.getObjectMethodParser();
		return ParseUtils.parse(tokenStream, contextInfo, expectation,
			fieldParser,
			methodParser
		);
	}

	@Override
	ParseResultIF parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		// array access
		TypeInfo currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		TypeInfo elementType = currentContextType.getComponentType();
		if (elementType == TypeInfo.NONE) {
			log(LogLevel.ERROR, "cannot apply operator [] for non-array types");
			return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ParseError.ErrorType.SEMANTIC_ERROR);
		}

		int indexStartPosition = tokenStream.getPosition();
		ParseExpectation indexExpectation = ParseExpectationBuilder.expectObject().allowedType(TypeInfo.of(int.class)).build();
		ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream, indexExpectation);

		if (ParseUtils.propagateParseResult(arrayIndexParseResult, indexExpectation)) {
			return arrayIndexParseResult;
		}

		ObjectParseResult parseResult = (ObjectParseResult) arrayIndexParseResult;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo indexInfo = parseResult.getObjectInfo();
		ObjectInfo elementInfo;
		try {
			elementInfo = parserToolbox.getObjectInfoProvider().getArrayElementInfo(contextInfo, indexInfo);
			log(LogLevel.SUCCESS, "detected valid array access");
		} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
			log(LogLevel.ERROR, "caught exception: " + e.getMessage());
			return new ParseError(indexStartPosition, e.getClass().getSimpleName() + " during array index evaluation", ParseError.ErrorType.EVALUATION_EXCEPTION, e);
		}
		tokenStream.moveTo(parsedToPosition);
		return parserToolbox.getObjectTailParser().parse(tokenStream, elementInfo, expectation);

	}

	@Override
	ParseResultIF createParseResult(int position, ObjectInfo objectInfo) {
		return new ObjectParseResult(position, objectInfo);
	}

	private ParseResultIF parseArrayIndex(TokenStream tokenStream, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing array index");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		ParseResultIF arrayIndexParseResult = parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, expectation);

		if (ParseUtils.propagateParseResult(arrayIndexParseResult, expectation)) {
			return arrayIndexParseResult;
		}

		ObjectParseResult parseResult = ((ObjectParseResult) arrayIndexParseResult);
		int parsedToPosition = parseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return new ParseError(parsedToPosition, "Expected closing bracket ']'", ParseError.ErrorType.SYNTAX_ERROR);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return new ObjectParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}
}
