package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
	ObjectParseResult parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, AmbiguousParseResultException, InternalParseException, InternalEvaluationException {
		List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>> parsers = Arrays.asList(
			parserToolbox.createParser(ObjectFieldParser.class),
			parserToolbox.createParser(ObjectMethodParser.class)
		);
		return ParseUtils.parse(tokenStream, contextInfo, expectation, parsers);
	}

	@Override
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalEvaluationException, InternalErrorException {
		// array access
		TypeInfo currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		TypeInfo elementType = currentContextType.getComponentType();
		if (elementType == InfoProvider.NO_TYPE) {
			throw new InternalParseException("Cannot apply [] to non-array types");
		}

		ObjectParseResultExpectation indexExpectation = new ObjectParseResultExpectation(ImmutableList.of(InfoProvider.createTypeInfo(int.class)), true);
		ObjectParseResult indexParseResult = parseArrayIndex(tokenStream, indexExpectation);
		ObjectInfo indexInfo = indexParseResult.getObjectInfo();
		ObjectInfo elementInfo;
		try {
			elementInfo = parserToolbox.getObjectInfoProvider().getArrayElementInfo(contextInfo, indexInfo);
			log(LogLevel.SUCCESS, "detected valid array access");
		} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
			throw new InternalEvaluationException(e.getClass().getSimpleName() + " during array index evaluation", e);
		}
		ParseResult arrayParseResult = isCompile()
										? new CompiledArrayParseResult((CompiledObjectParseResult) indexParseResult, elementInfo)
										: ParseResults.createObjectParseResult(elementInfo);
		return ParseResults.parseTail(tokenStream, arrayParseResult, parserToolbox, expectation);
	}

	@Override
	ObjectParseResult createParseResult(ObjectInfo objectInfo) {
		return isCompile()
				? ParseResults.createCompiledIdentityObjectParseResult(objectInfo)
				: ParseResults.createObjectParseResult(objectInfo);
	}

	private ObjectParseResult parseArrayIndex(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalErrorException, InternalEvaluationException {
		tokenStream.readCharacter('[');
		log(LogLevel.INFO, "parsing array index");
		ObjectParseResult indexParseResult = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);
		tokenStream.readCharacter(']');
		return indexParseResult;
	}

	private static class CompiledArrayParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult indexParseResult;

		CompiledArrayParseResult(CompiledObjectParseResult indexParseResult, ObjectInfo elementInfo) {
			super(elementInfo);
			this.indexParseResult = indexParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			ObjectInfo indexInfo = indexParseResult.evaluate(thisInfo, thisInfo);
			return OBJECT_INFO_PROVIDER.getArrayElementInfo(contextInfo, indexInfo);
		}
	}
}
