package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
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
	ParseOutcome parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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
	ParseOutcome parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		// array access
		TypeInfo currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		TypeInfo elementType = currentContextType.getComponentType();
		if (elementType == InfoProvider.NO_TYPE) {
			log(LogLevel.ERROR, "cannot apply operator [] for non-array types");
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ErrorPriority.RIGHT_PARSER);
		}

		int indexStartPosition = tokenStream.getPosition();
		ParseExpectation indexExpectation = ParseExpectationBuilder.expectObject().allowedType(InfoProvider.createTypeInfo(int.class)).build();
		ParseOutcome arrayIndexParseOutcome = parseArrayIndex(tokenStream, indexExpectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(arrayIndexParseOutcome, indexExpectation, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		ObjectParseResult arrayIndexParseResult = (ObjectParseResult) arrayIndexParseOutcome;
		int parsedToPosition = arrayIndexParseResult.getPosition();
		ObjectInfo indexInfo = arrayIndexParseResult.getObjectInfo();
		ObjectInfo elementInfo;
		try {
			elementInfo = parserToolbox.getObjectInfoProvider().getArrayElementInfo(contextInfo, indexInfo);
			log(LogLevel.SUCCESS, "detected valid array access");
		} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
			log(LogLevel.ERROR, "caught exception: " + e.getMessage());
			return ParseOutcomes.createParseError(indexStartPosition, e.getClass().getSimpleName() + " during array index evaluation", ErrorPriority.EVALUATION_EXCEPTION, e);
		}
		tokenStream.moveTo(parsedToPosition);
		ParseOutcome parseOutcome = parserToolbox.getObjectTailParser().parse(tokenStream, elementInfo, expectation);
		return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
				? compileArrayParseResult(parseOutcome, arrayIndexParseResult)
				: parseOutcome;

	}

	@Override
	ParseOutcome createParseOutcome(int position, ObjectInfo objectInfo) {
		return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
				? ParseOutcomes.createCompiledIdentityObjectParseResult(position, objectInfo)
				: ParseOutcomes.createObjectParseResult(position, objectInfo);
	}

	private ParseOutcome parseArrayIndex(TokenStream tokenStream, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing array index");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		ParseOutcome arrayIndexParseOutcome = parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, expectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(arrayIndexParseOutcome, expectation, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		ObjectParseResult parseResult = ((ObjectParseResult) arrayIndexParseOutcome);
		int parsedToPosition = parseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return ParseOutcomes.createParseError(parsedToPosition, "Expected closing bracket ']'", ErrorPriority.RIGHT_PARSER);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
				? compileArrayIndex(arrayIndexParseOutcome, tokenStream.getPosition())
				: ParseOutcomes.createObjectParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}

	private ParseOutcome compileArrayParseResult(ParseOutcome tailParseOutcome, ObjectParseResult arrayIndexParseResult) {
		if (!ParseOutcomes.isCompiledParseResult(tailParseOutcome)) {
			return tailParseOutcome;
		}
		CompiledObjectParseResult compiledTailParseResult = (CompiledObjectParseResult) tailParseOutcome;
		CompiledObjectParseResult compiledArrayIndexParseResult = (CompiledObjectParseResult) arrayIndexParseResult;
		return new CompiledArrayParseResult(compiledTailParseResult, compiledArrayIndexParseResult);
	}

	private ParseOutcome compileArrayIndex(ParseOutcome arrayIndexParseOutcome, int position) {
		if (!ParseOutcomes.isCompiledParseResult(arrayIndexParseOutcome)) {
			return arrayIndexParseOutcome;
		}
		CompiledObjectParseResult compiledArrayIndexParseResult = (CompiledObjectParseResult) arrayIndexParseOutcome;
		return new CompiledArrayIndexParseResult(compiledArrayIndexParseResult, position);
	}

	private static class CompiledArrayParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledTailParseResult;
		private final CompiledObjectParseResult	compiledArrayIndexParseResult;

		CompiledArrayParseResult(CompiledObjectParseResult compiledTailParseResult, CompiledObjectParseResult compiledArrayIndexParseResult) {
			super(compiledTailParseResult.getPosition(), compiledTailParseResult.getObjectInfo());
			this.compiledTailParseResult = compiledTailParseResult;
			this.compiledArrayIndexParseResult = compiledArrayIndexParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			ObjectInfo indexInfo = compiledArrayIndexParseResult.evaluate(thisInfo, context);
			ObjectInfo elementInfo = OBJECT_INFO_PROVIDER.getArrayElementInfo(context, indexInfo);
			return compiledTailParseResult.evaluate(thisInfo, elementInfo);
		}
	}

	private static class CompiledArrayIndexParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledArrayIndexParseResult;

		private CompiledArrayIndexParseResult(CompiledObjectParseResult compiledArrayIndexParseResult, int position) {
			super(position, compiledArrayIndexParseResult.getObjectInfo());
			this.compiledArrayIndexParseResult = compiledArrayIndexParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			return compiledArrayIndexParseResult.evaluate(thisInfo, thisInfo);
		}
	}
}
