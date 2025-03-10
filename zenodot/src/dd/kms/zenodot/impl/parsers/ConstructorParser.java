package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.ConstructorScanner;
import dd.kms.zenodot.api.common.ConstructorScannerBuilder;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ClassParseResult;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ExecutableInfo;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.ExecutableDataProvider;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses expressions of the form
 * <ul>
 *     <li>{@code new <class>(<arguments>)},</li>
 *     <li>{@code new <class>[<size>]}, and</li>
 *     <li>{@code new <class>[]{<elements>}}.</li>
 * </ul>
 * The (ignored) context of the parser is {@code this}.
 */
public class ConstructorParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	private static final String	NEW_KEYWORD		= "new";
	private static final String	ERROR_MESSAGE	= "Expected keyword '" + NEW_KEYWORD + "'";

	public ConstructorParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		String keyword = tokenStream.readKeyword(TokenStream.NO_COMPLETIONS, ERROR_MESSAGE);
		if (!NEW_KEYWORD.equals(keyword)) {
			throw new SyntaxException(ERROR_MESSAGE);
		}
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ClassParseResult classParseResult = ParseUtils.parseClass(tokenStream, parserToolbox);
		Class<?> type = classParseResult.getType();

		char nextChar = tokenStream.readCharacter('(', '[');
		switch (nextChar) {
			case '(':
				return parseObjectConstructor(tokenStream, type);
			case '[':
				return parseArrayConstructor(tokenStream, type);
			default:
				throw new InternalErrorException(tokenStream.toString() +  ": Expected '(' or '[', but found '" + nextChar + "'. This case should have been handled earlier.");
		}
	}

	private ObjectParseResult parseObjectConstructor(TokenStream tokenStream, Class<?> constructorType) throws SyntaxException, InternalErrorException, CodeCompletionException, EvaluationException {
		if (constructorType.getEnclosingClass() != null && !Modifier.isStatic(constructorType.getModifiers())) {
			throw new SyntaxException("Cannot instantiate non-static inner class '" + constructorType.getName() + "'");
		}
		List<ExecutableInfo> constructorInfos = getConstructorInfos(constructorType);

		log(LogLevel.INFO, "parsing constructor arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.inject(ExecutableDataProvider.class);
		List<ObjectParseResult> argumentResults = executableDataProvider.parseArguments(tokenStream, null, constructorInfos);
		List<ObjectInfo> argumentInfos = argumentResults.stream().map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingConstructorInfos = executableDataProvider.getBestMatchingExecutables(constructorInfos, argumentInfos);

		switch (bestMatchingConstructorInfos.size()) {
			case 0:
				throw new SyntaxException("No constructor matches the given arguments");
			case 1: {
				ExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					log(LogLevel.SUCCESS, "found unique matching constructor");
					constructorReturnInfo = parserToolbox.inject(ObjectInfoProvider.class).getExecutableReturnInfo(null, bestMatchingConstructorInfo, argumentInfos);
				} catch (ReflectiveOperationException e) {
					throw new EvaluationException(e.getMessage(), e);
				}
				return new ObjectConstructorParseResult(bestMatchingConstructorInfo, argumentResults, constructorReturnInfo, tokenStream);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				throw new SyntaxException(error);
			}
		}
	}

	private ObjectParseResult parseArrayConstructor(TokenStream tokenStream, Class<?> componentType) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		// TODO: currently, only 1d arrays are supported
		@Nullable ObjectParseResult arraySizeParseResult = parseArraySize(tokenStream);
		if (arraySizeParseResult == null) {
			// array constructor with initializer list (e.g., "new int[] { 1, 2, 3 }")
			log(LogLevel.INFO, "parsing array elements at " + tokenStream);
			ObjectParseResultExpectation elementExpectation = new ObjectParseResultExpectation(ImmutableList.of(componentType), true);
			List<ObjectParseResult> elementParseResults = parseArrayElements(tokenStream, elementExpectation);
			List<ObjectInfo> elementInfos = elementParseResults.stream().map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
			ObjectInfo arrayInfo = parserToolbox.inject(ObjectInfoProvider.class).getArrayInfo(componentType, elementInfos);
			log(LogLevel.SUCCESS, "detected valid array construction with initializer list");
			return new ArrayConstructorWithInitializerListParseResult(componentType, elementParseResults, arrayInfo, tokenStream);
		} else {
			// array constructor with default initialization (e.g., "new int[3]")
			ObjectInfo arrayInfo;
			try {
				ObjectInfo sizeInfo = arraySizeParseResult.getObjectInfo();
				arrayInfo = parserToolbox.inject(ObjectInfoProvider.class).getArrayInfo(componentType, sizeInfo);
				log(LogLevel.SUCCESS, "detected valid array construction with null initialization");
			} catch (ClassCastException | NegativeArraySizeException e) {
				log(LogLevel.ERROR, "caught exception: " + e.getMessage());
				throw new EvaluationException("Exception during array construction: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
			return new ArrayConstructorWithDefaultInitializationParseResult(componentType, arraySizeParseResult, arrayInfo, tokenStream);
		}
	}

	// returns null if no size is specified (e.g. in "new int[] { 1, 2, 3 }")
	private @Nullable ObjectParseResult parseArraySize(TokenStream tokenStream) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		log(LogLevel.INFO, "parsing array size");

		if (tokenStream.skipCharacter(']')) {
			return null;
		}

		ObjectParseResultExpectation sizeExpectation = new ObjectParseResultExpectation(ImmutableList.of(int.class), true);
		ObjectParseResult arraySizeParseResult = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), sizeExpectation);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(']');

		return arraySizeParseResult;
	}

	private List<ObjectParseResult> parseArrayElements(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		List<ObjectParseResult> elements = new ArrayList<>();

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter('{');

		boolean foundClosingCurlyBrace = false;
		if (tokenStream.skipCharacter('}')) {
			foundClosingCurlyBrace = true;
		}
		while (!foundClosingCurlyBrace) {
			/*
			 * Parse expression for argument i
			 */
			ObjectParseResult element = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);
			elements.add(element);

			foundClosingCurlyBrace = tokenStream.readCharacter(',', '}') == '}';
		}
		return elements;
	}

	private List<ExecutableInfo> getConstructorInfos(Class<?> constructorType) {
		ConstructorScanner constructorScanner = getConstructorScanner();
		return InfoProvider.getConstructorInfos(constructorType, constructorScanner);
	}

	private ConstructorScanner getConstructorScanner() {
		AccessModifier minimumAccessModifier = parserToolbox.getSettings().getMinimumMethodAccessModifier();
		return ConstructorScannerBuilder.create()
			.minimumAccessModifier(minimumAccessModifier)
			.build();
	}

	private static String formatConstructorInfo(ExecutableInfo constructorInfo) {
		return constructorInfo.getName()
				+ "("
				+ constructorInfo.formatArguments()
				+ ")";
	}

	private static class ObjectConstructorParseResult extends ObjectParseResult
	{
		private final ExecutableInfo			constructor;
		private final List<ObjectParseResult>	arguments;

		ObjectConstructorParseResult(ExecutableInfo constructor, List<ObjectParseResult> arguments, ObjectInfo constructorReturnInfo, TokenStream tokenStream) {
			super(constructorReturnInfo, tokenStream);
			this.constructor = constructor;
			this.arguments = arguments;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws Exception {
			List<ObjectInfo> arguments = new ArrayList<>(this.arguments.size());
			for (ObjectParseResult argument : this.arguments) {
				arguments.add(argument.evaluate(thisInfo, thisInfo, variables));
			}
			return ObjectInfoProvider.DYNAMIC_OBJECT_INFO_PROVIDER.getExecutableReturnInfo(null, constructor, arguments);
		}
	}

	private static class ArrayConstructorWithInitializerListParseResult extends ObjectParseResult
	{
		private final Class<?>					componentType;
		private final List<ObjectParseResult>	elementParseResults;

		ArrayConstructorWithInitializerListParseResult(Class<?> componentType, List<ObjectParseResult> elementParseResults, ObjectInfo arrayInfo, TokenStream tokenStream) {
			super(arrayInfo, tokenStream);
			this.componentType = componentType;
			this.elementParseResults = elementParseResults;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws ParseException {
			List<ObjectInfo> elementInfos = new ArrayList<>(elementParseResults.size());
			for (ObjectParseResult elementParseResult : elementParseResults) {
				elementInfos.add(elementParseResult.evaluate(thisInfo, contextInfo, variables));
			}
			return ObjectInfoProvider.DYNAMIC_OBJECT_INFO_PROVIDER.getArrayInfo(componentType, elementInfos);
		}
	}

	private static class ArrayConstructorWithDefaultInitializationParseResult extends ObjectParseResult
	{
		private final Class<?>			componentType;
		private final ObjectParseResult	sizeParseResult;

		ArrayConstructorWithDefaultInitializationParseResult(Class<?> componentType, ObjectParseResult sizeParseResult, ObjectInfo arrayInfo, TokenStream tokenStream) {
			super(arrayInfo, tokenStream);
			this.componentType = componentType;
			this.sizeParseResult = sizeParseResult;
		}


		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws ParseException {
			ObjectInfo sizeInfo = sizeParseResult.evaluate(thisInfo, contextInfo, variables);
			return ObjectInfoProvider.DYNAMIC_OBJECT_INFO_PROVIDER.getArrayInfo(componentType, sizeInfo);
		}
	}
}
