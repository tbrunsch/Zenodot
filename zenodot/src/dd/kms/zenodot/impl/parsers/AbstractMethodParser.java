package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.AbstractObjectParseResult;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.utils.dataproviders.ExecutableDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for {@link ClassMethodParser} and {@link ObjectMethodParser}
 */
abstract class AbstractMethodParser<C> extends AbstractParserWithObjectTail<C>
{
	AbstractMethodParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract Object getContextObject(C context);
	abstract TypeInfo getContextType(C context);
	abstract boolean isContextStatic();

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, EvaluationException, InternalErrorException {
		String methodName = tokenStream.readIdentifier(info -> suggestMethods(context, expectation, info), "Expected a method");
		int positionAfterMethodName = tokenStream.getPosition();
		tokenStream.readCharacter('(');
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		List<ExecutableInfo> methods = getMethods(context, getMethodScanner(methodName, true));
		if (methods.isEmpty()) {
			tokenStream.setPosition(positionAfterMethodName);
			throw createMethodNotFoundException(context, methodName);
		}
		log(LogLevel.SUCCESS, "Detected " + methods.size() + " method(s) '" + methodName + "'");

		log(LogLevel.INFO, "Parsing method arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ObjectParseResult> argumentResults = executableDataProvider.parseArguments(tokenStream, methods);
		List<ObjectInfo> arguments = argumentResults.stream().map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());

		List<ExecutableInfo> bestMatchingMethods = executableDataProvider.getBestMatchingExecutables(methods, arguments);

		switch (bestMatchingMethods.size()) {
			case 0:
				throw new SyntaxException("No method '" + methodName + "' matches the given arguments");
			case 1: {
				ExecutableInfo bestMatchingMethod = Iterables.getOnlyElement(bestMatchingMethods);
				ObjectInfo methodReturnInfo;
				try {
					log(LogLevel.SUCCESS, "Found unique matching method");
					methodReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(getContextObject(context), bestMatchingMethod, arguments);
				} catch (Exception e) {
					throw new EvaluationException("Exception when evaluating method '" + methodName + "'", e);
				}
				return new MethodParseResult(isContextStatic(), bestMatchingMethod, argumentResults, methodReturnInfo, tokenStream.getPosition());
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethods.stream().map(Object::toString).collect(Collectors.joining("\n"));
				throw new SyntaxException(error);
			}
		}
	}

	private CodeCompletions suggestMethods(C context, ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting methods matching '" + nameToComplete + "'");

		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ExecutableInfo> methodInfos = getMethods(context, getMethodScanner());
		boolean contextIsStatic = isContextStatic();
		return executableDataProvider.completeMethod(methodInfos, contextIsStatic, nameToComplete, expectation, insertionBegin, insertionEnd);
	}

	private SyntaxException createMethodNotFoundException(C context, String methodName) {
		// distinguish between "unknown method" and "method not visible"
		List<ExecutableInfo> allMethods = getMethods(context, getMethodScanner(methodName, false));
		String error = allMethods.isEmpty() ? "Unknown method '" + methodName + "'" : "Method '" + methodName + "' is not visible";
		return new SyntaxException(error);
	}

	private MethodScanner getMethodScanner() {
		AccessModifier minimumAccessModifier = parserToolbox.getSettings().getMinimumAccessModifier();
		return new MethodScanner().staticOnly(isContextStatic()).minimumAccessModifier(minimumAccessModifier);
	}

	private MethodScanner getMethodScanner(String name, boolean considerMinimumAccessModifier) {
		MethodScanner methodScanner = getMethodScanner().name(name);
		if (!considerMinimumAccessModifier) {
			methodScanner.minimumAccessModifier(AccessModifier.PRIVATE);
		}
		return methodScanner;
	}

	private List<ExecutableInfo> getMethods(C context, MethodScanner methodScanner) {
		return InfoProvider.getMethodInfos(getContextType(context), methodScanner);
	}

	private static class MethodParseResult extends AbstractObjectParseResult
	{
		private final ExecutableInfo			method;
		private final List<ObjectParseResult>	arguments;
		private final boolean					contextStatic;

		MethodParseResult(boolean contextStatic, ExecutableInfo method, List<ObjectParseResult> arguments, ObjectInfo methodReturnInfo, int position) {
			super(methodReturnInfo, position);
			this.contextStatic = contextStatic;
			this.method = method;
			this.arguments = arguments;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			Object contextObject = contextStatic ? null : contextInfo.getObject();
			List<ObjectInfo> arguments = new ArrayList<>(this.arguments.size());
			for (ObjectParseResult argument : this.arguments) {
				arguments.add(argument.evaluate(thisInfo, thisInfo));
			}
			return OBJECT_INFO_PROVIDER.getExecutableReturnInfo(contextObject, method, arguments);
		}
	}
}
