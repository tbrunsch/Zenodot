package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.AbstractObjectParseResult;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

import java.util.Arrays;
import java.util.List;

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
public class ObjectTailParser extends AbstractTailParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public ObjectTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		if (contextInfo.getObject() == null) {
			throw new NullPointerException();
		}
		List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>> parsers = Arrays.asList(
			parserToolbox.createParser(ObjectFieldParser.class),
			parserToolbox.createParser(ObjectMethodParser.class)
		);
		return ParseUtils.parse(tokenStream, contextInfo, expectation, parsers);
	}

	@Override
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, EvaluationException, InternalErrorException {
		if (contextInfo.getObject() == null) {
			throw new NullPointerException();
		}

		// array access
		Class<?> currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		Class<?> elementType = currentContextType.getComponentType();
		if (elementType == InfoProvider.NO_TYPE) {
			throw new SyntaxException("Cannot apply [] to non-array types");
		}

		ObjectParseResultExpectation indexExpectation = new ObjectParseResultExpectation(ImmutableList.of(int.class), true);
		ObjectParseResult indexParseResult = parseArrayIndex(tokenStream, indexExpectation);
		ObjectInfo indexInfo = indexParseResult.getObjectInfo();
		ObjectInfo elementInfo;
		try {
			elementInfo = parserToolbox.getObjectInfoProvider().getArrayElementInfo(contextInfo, indexInfo);
			log(LogLevel.SUCCESS, "detected valid array access");
		} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
			throw new EvaluationException(e.getClass().getSimpleName() + " during array index evaluation", e);
		}
		ParseResult arrayParseResult = new ArrayParseResult(indexParseResult, elementInfo, tokenStream);
		return ParseUtils.parseTail(tokenStream, arrayParseResult, parserToolbox, expectation);
	}

	@Override
	ObjectParseResult createParseResult(TokenStream tokenStream, ObjectInfo objectInfo) {
		return ParseResults.createCompiledIdentityObjectParseResult(objectInfo, tokenStream);
	}

	private ObjectParseResult parseArrayIndex(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		tokenStream.readCharacter('[');
		log(LogLevel.INFO, "parsing array index");
		ObjectParseResult indexParseResult = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);
		tokenStream.readCharacter(']');
		return indexParseResult;
	}

	private static class ArrayParseResult extends AbstractObjectParseResult
	{
		private final ObjectParseResult indexParseResult;

		ArrayParseResult(ObjectParseResult indexParseResult, ObjectInfo elementInfo, TokenStream tokenStream) {
			super(elementInfo, tokenStream);
			this.indexParseResult = indexParseResult;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws ParseException {
			ObjectInfo indexInfo = indexParseResult.evaluate(thisInfo, thisInfo);
			return OBJECT_INFO_PROVIDER.getArrayElementInfo(contextInfo, indexInfo);
		}
	}
}
