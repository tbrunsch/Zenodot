package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.util.Arrays;
import java.util.List;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[] or</li>
 *     <li>{@code <class>.}</li>
 * </ul>
 */
public class TailParser extends AbstractEntityParser
{
	private final boolean staticOnly;

	public TailParser(ParserContext parserContext, ObjectInfo thisInfo, boolean staticOnly) {
		super(parserContext, thisInfo);
		this.staticOnly = staticOnly;
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		if (tokenStream.hasMore()) {
			char nextChar = tokenStream.peekCharacter();
			if (nextChar == '.') {
				return parseDot(tokenStream, currentContextInfo, expectedResultClasses);
			} else if (nextChar == '[') {
				if (staticOnly) {
					return new ParseError(tokenStream.getPosition(), "Cannot apply [] to classes", ErrorType.SYNTAX_ERROR);
				}

				Class<?> currentContextClass = parserContext.getObjectInfoProvider().getClass(currentContextInfo);
				Class<?> elementClass = currentContextClass.getComponentType();
				if (elementClass == null) {
					// no array type
					return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ErrorType.SEMANTIC_ERROR);
				}

				ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream);

				// propagate anything except results
				if (arrayIndexParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
					return arrayIndexParseResult;
				}

				ParseResult parseResult = (ParseResult) arrayIndexParseResult;
				int parsedToPosition = parseResult.getParsedToPosition();
				ObjectInfo indexInfo = parseResult.getObjectInfo();
				ObjectInfo elementInfo;
				try {
					elementInfo = parserContext.getObjectInfoProvider().getArrayElementInfo(currentContextInfo, indexInfo);
				} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
					return new ParseError(startPosition, e.getClass().getSimpleName() + " during array index evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				tokenStream.moveTo(parsedToPosition);
				return parserContext.getTailParser(false).parse(tokenStream, elementInfo, expectedResultClasses);
			}
		}
		/*
		 * finished parsing
		 *
		 * expectedResultClasses are only evaluated for completion suggestions, not for parse results.
		 *
		 * The root caller (CompoundExpressionParser) is responsible for verifying that the result matches one of the expected types.
		 * The reason for this is that CompoundExpressionParser calls the ExpressionParser for each of the operands, but it does not
		 * know which type to expect from each of its operands. However, for completion suggestions it might be helpful to take the
		 * expected type of the CompoundExpressionParser into consideration.
		 *
		 * Example: CompoundExpressionParser expects to return a String. However, the first operand might be anything because
		 *          object + "a string" results in a String. Nevertheless, suggesting a string for the first operand before
		 *          an arbitrary object seems to be reasonable, for example if there is no second operand.
		 */
		return new ParseResult(tokenStream.getPosition(), currentContextInfo);
	}

	private ParseResultIF parseDot(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");
		if (characterToken.isContainsCaret()) {
			int insertionBegin = tokenStream.getPosition();
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = insertionBegin;
			}
			return parserContext.getFieldAndMethodDataProvider().suggestFieldsAndMethods(currentContextInfo, expectedResultClasses, insertionBegin, insertionEnd, staticOnly);
		}

		return ParseUtils.parse(tokenStream, currentContextInfo, expectedResultClasses,
			parserContext.getFieldParser(staticOnly),
			parserContext.getMethodParser(staticOnly)
		);
	}

	private ParseResultIF parseArrayIndex(TokenStream tokenStream) {
		List<Class<?>> expectedResultClasses = Arrays.asList(int.class);
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");
		if (characterToken.isContainsCaret()) {
			return parserContext.getFieldAndMethodDataProvider().suggestFieldsAndMethods(tokenStream, expectedResultClasses);
		}

		ParseResultIF arrayIndexParseResult = parserContext.getCompoundExpressionParser().parse(tokenStream, thisInfo, expectedResultClasses);

		// propagate anything except results
		if (arrayIndexParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return arrayIndexParseResult;
		}

		ParseResult parseResult = ((ParseResult) arrayIndexParseResult);
		int parsedToPosition = parseResult.getParsedToPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			return new ParseError(parsedToPosition, "Expected closing bracket ']'", ErrorType.SYNTAX_ERROR);
		}

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ']'
			return CompletionSuggestions.NONE;
		}

		// propagate parse result with corrected position (includes ']')
		return new ParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}
}