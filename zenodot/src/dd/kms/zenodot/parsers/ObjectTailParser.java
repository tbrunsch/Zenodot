package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Optional;

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
	ParseResult parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		// array access
		TypeInfo currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		TypeInfo elementType = currentContextType.getComponentType();
		if (elementType == InfoProvider.NO_TYPE) {
			log(LogLevel.ERROR, "cannot apply operator [] for non-array types");
			return ParseResults.createParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ErrorPriority.RIGHT_PARSER);
		}

		int indexStartPosition = tokenStream.getPosition();
		ParseExpectation indexExpectation = ParseExpectationBuilder.expectObject().allowedType(InfoProvider.createTypeInfo(int.class)).build();
		ParseResult arrayIndexParseResult = parseArrayIndex(tokenStream, indexExpectation);

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(arrayIndexParseResult, indexExpectation, ErrorPriority.RIGHT_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
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
			return ParseResults.createParseError(indexStartPosition, e.getClass().getSimpleName() + " during array index evaluation", ErrorPriority.EVALUATION_EXCEPTION, e);
		}
		tokenStream.moveTo(parsedToPosition);
		return parserToolbox.getObjectTailParser().parse(tokenStream, elementInfo, expectation);

	}

	@Override
	ParseResult createParseResult(int position, ObjectInfo objectInfo) {
		return ParseResults.createObjectParseResult(position, objectInfo);
	}

	private ParseResult parseArrayIndex(TokenStream tokenStream, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing array index");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		ParseResult arrayIndexParseResult = parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, expectation);

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(arrayIndexParseResult, expectation, ErrorPriority.RIGHT_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}

		ObjectParseResult parseResult = ((ObjectParseResult) arrayIndexParseResult);
		int parsedToPosition = parseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return ParseResults.createParseError(parsedToPosition, "Expected closing bracket ']'", ErrorPriority.RIGHT_PARSER);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return ParseResults.createObjectParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}
}
