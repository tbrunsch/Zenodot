package dd.kms.zenodot.result;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.AbstractParser;
import dd.kms.zenodot.parsers.ClassTailParser;
import dd.kms.zenodot.parsers.ObjectTailParser;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Map;

public class ParseResults
{
	public static ClassParseResult createClassParseResult(TypeInfo type) {
		return new ClassParseResultImpl(type);
	}

	public static PackageParseResult createPackageParseResult(PackageInfo packageInfo) {
		return new PackageParseResultImpl(packageInfo);
	}

	public static ObjectParseResult createObjectParseResult(ObjectInfo objectInfo) {
		return new ObjectParseResultImpl(objectInfo);
	}

	public static CompiledObjectParseResult createCompiledIdentityObjectParseResult(ObjectInfo objectInfo) {
		return new CompiledIdentityObjectParseResult(objectInfo);
	}

	public static CompiledObjectParseResult createCompiledConstantObjectParseResult(ObjectInfo objectInfo) {
		return new CompiledConstantObjectParseResult(objectInfo);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	public static ObjectParseResult parseTail(TokenStream tokenStream, ParseResult parseResult, ParserToolbox parserToolbox, ObjectParseResultExpectation expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalErrorException, InternalEvaluationException {
		if (parseResult instanceof ObjectParseResult) {
			ObjectParseResult objectParseResult = (ObjectParseResult) parseResult;
			ObjectInfo objectInfo = objectParseResult.getObjectInfo();
			AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> objectTailParser = parserToolbox.createParser(ObjectTailParser.class);
			ObjectParseResult tailParseResult = objectTailParser.parse(tokenStream, objectInfo, expectation);
			return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
				? new CompiledParseResultWithTail((CompiledObjectParseResult) objectParseResult, (CompiledObjectParseResult) tailParseResult)
				: tailParseResult;
		} else if (parseResult instanceof ClassParseResult) {
			ClassParseResult classParseResult = (ClassParseResult) parseResult;
			TypeInfo type = classParseResult.getType();
			AbstractParser<TypeInfo, ObjectParseResult, ObjectParseResultExpectation> classTailParser = parserToolbox.createParser(ClassTailParser.class);
			return classTailParser.parse(tokenStream, type, expectation);
		} else {
			throw new InternalErrorException("Can only parse tails of objects and classes, but requested for " + parseResult.getClass().getSimpleName());
		}
	}

	private static class CompiledIdentityObjectParseResult extends AbstractCompiledParseResult
	{
		CompiledIdentityObjectParseResult(ObjectInfo objectInfo) {
			super(objectInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return context;
		}
	}

	private static class CompiledConstantObjectParseResult extends AbstractCompiledParseResult
	{
		CompiledConstantObjectParseResult(ObjectInfo objectInfo) {
			super(objectInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return getObjectInfo();
		}
	}

	private static class CompiledParseResultWithTail extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	parseResult;
		private final CompiledObjectParseResult	tailParseResult;

		CompiledParseResultWithTail(CompiledObjectParseResult parseResult, CompiledObjectParseResult tailParseResult) {
			super(tailParseResult.getObjectInfo());
			this.parseResult = parseResult;
			this.tailParseResult = tailParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			ObjectInfo nextObjectInfo = parseResult.evaluate(thisInfo, context);
			return tailParseResult.evaluate(thisInfo, nextObjectInfo);
		}
	}
}
