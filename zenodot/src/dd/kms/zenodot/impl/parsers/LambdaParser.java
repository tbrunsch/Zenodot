package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses expressions of the form {@code x -> x*x} or {@code (x, y) -> x + y} in the context of {@code this}.
 */
public class LambdaParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public LambdaParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		List<Class<?>> expectedTypes = expectation.getExpectedTypes();
		if (expectedTypes == null) {
			throw new SyntaxException("No functional interface expected");
		}
		List<Class<?>> possibleFunctionalInterfaces = expectedTypes.stream()
			.filter(LambdaParser::isFunctionalInterface)
			.collect(Collectors.toList());
		if (possibleFunctionalInterfaces.isEmpty()) {
			throw new SyntaxException("No functional interface expected");
		}

		List<ConcreteLambdaParser> parsers = possibleFunctionalInterfaces.stream()
			.map(functionalInterface -> new ConcreteLambdaParser(parserToolbox, functionalInterface))
			.collect(Collectors.toList());
		return ParseUtils.parse(tokenStream, context, expectation, parsers);
	}

	private static boolean isFunctionalInterface(Class<?> clazz) {
		if (!clazz.isInterface()) {
			return false;
		}
		List<Method> unimplementedMethods = getUnimplementedMethods(clazz);
		return unimplementedMethods.size() == 1;
	}

	static List<Method> getUnimplementedMethods(Class<?> clazz) {
		MethodScanner scanner = MethodScannerBuilder.create().staticMode(StaticMode.NON_STATIC).build();
		List<Method> methods = scanner.getMethods(clazz);
		return methods.stream()
			.filter(method -> !method.isDefault())
			.filter(method -> Modifier.isAbstract(method.getModifiers()))
			.collect(Collectors.toList());
	}
}
