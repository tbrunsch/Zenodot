package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.MethodScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract TypeInfo getContextType(C context);
	abstract boolean isContextStatic();

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalEvaluationException, InternalErrorException {
		if (contextCausesNullPointerException(context)) {
			throw new InternalParseException("Null pointer exception");
		}

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
				throw new InternalParseException("No method '" + methodName + "' matches the given arguments");
			case 1: {
				ExecutableInfo bestMatchingMethod = Iterables.getOnlyElement(bestMatchingMethods);
				ObjectInfo methodReturnInfo;
				try {
					log(LogLevel.SUCCESS, "Found unique matching method");
					methodReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(getContextObject(context), bestMatchingMethod, arguments);
				} catch (Exception e) {
					throw new InternalEvaluationException("Exception when evaluating method '" + methodName + "'", e);
				}
				return isCompile()
						? new CompiledMethodParseResult(bestMatchingMethod, (List) argumentResults, methodReturnInfo)
						: ParseResults.createObjectParseResult(methodReturnInfo);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethods.stream().map(Object::toString).collect(Collectors.joining("\n"));
				throw new AmbiguousParseResultException(error);
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

	private InternalParseException createMethodNotFoundException(C context, String methodName) {
		// distinguish between "unknown method" and "method not visible"
		List<ExecutableInfo> allMethods = getMethods(context, getMethodScanner(methodName, false));
		String error = allMethods.isEmpty() ? "Unknown method '" + methodName + "'" : "Method '" + methodName + "' is not visible";
		return new InternalParseException(error);
	}

	private MethodScanner getMethodScanner() {
		AccessModifier minimumAccessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		return new MethodScanner().staticOnly(isContextStatic()).minimumAccessLevel(minimumAccessLevel);
	}

	private MethodScanner getMethodScanner(String name, boolean considerMinimumAccessLevel) {
		MethodScanner methodScanner = getMethodScanner().name(name);
		if (!considerMinimumAccessLevel) {
			methodScanner.minimumAccessLevel(AccessModifier.PRIVATE);
		}
		return methodScanner;
	}

	private List<ExecutableInfo> getMethods(C context, MethodScanner methodScanner) {
		return InfoProvider.getMethodInfos(getContextType(context), methodScanner);
	}

	private class CompiledMethodParseResult extends AbstractCompiledParseResult
	{
		private final ExecutableInfo					method;
		private final List<CompiledObjectParseResult>	compiledArguments;

		CompiledMethodParseResult(ExecutableInfo method, List<CompiledObjectParseResult> compiledArguments, ObjectInfo methodReturnInfo) {
			super(methodReturnInfo);
			this.method = method;
			this.compiledArguments = compiledArguments;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			List<ObjectInfo> arguments = new ArrayList<>(compiledArguments.size());
			for (CompiledObjectParseResult compiledArgument : compiledArguments) {
				arguments.add(compiledArgument.evaluate(thisInfo, thisInfo));
			}
			Object contextObject = isContextStatic() ? null : contextInfo.getObject();
			return OBJECT_INFO_PROVIDER.getExecutableReturnInfo(contextObject, method, arguments);
		}
	}
}
