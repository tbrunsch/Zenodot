package dd.kms.zenodot.result;

import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Map;

public class ParseOutcomes
{
	public static AmbiguousParseResult createAmbiguousParseResult(int position, String message) {
		return new AmbiguousParseResultImpl(position, message);
	}

	public static ClassParseResult createClassParseResult(int position, TypeInfo type) {
		return new ClassParseResultImpl(position, type);
	}

	public static PackageParseResult createPackageParseResult(int position, PackageInfo packageInfo) {
		return new PackageParseResultImpl(position, packageInfo);
	}

	public static ObjectParseResult createObjectParseResult(int position, ObjectInfo objectInfo) {
		return new ObjectParseResultImpl(position, objectInfo);
	}

	public static CompiledObjectParseResult createCompiledIdentityObjectParseResult(int position, ObjectInfo objectInfo) {
		return new CompiledIdentityObjectParseResult(position, objectInfo);
	}

	public static CompiledObjectParseResult createCompiledConstantObjectParseResult(int position, ObjectInfo objectInfo) {
		return new CompiledConstantObjectParseResult(position, objectInfo);
	}

	public static CompiledObjectParseResult deriveCompiledObjectParseResult(CompiledObjectParseResult compiledObjectParseResult, int position) {
		return new DerivedCompiledObjectParseResult(compiledObjectParseResult, position);
	}

	public static ParseOutcome parseTail(TokenStream tokenStream, ParseOutcome nextParseOutcome, ParserToolbox parserToolbox, ParseExpectation expectation) {
		if (!ParseOutcomes.isParseResultOfType(nextParseOutcome, ParseResultType.OBJECT)) {
			return nextParseOutcome;
		}
		ObjectParseResult nextParseResult = (ObjectParseResult) nextParseOutcome;
		ObjectInfo nextObjectInfo = nextParseResult.getObjectInfo();
		int parsedToPosition = nextParseResult.getPosition();
		tokenStream.moveTo(parsedToPosition);
		ParseOutcome tailParseOutcome = parserToolbox.getObjectTailParser().parse(tokenStream, nextObjectInfo, expectation);
		return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
			? compileWithTail(tailParseOutcome, nextParseResult)
			: tailParseOutcome;
	}

	private static ParseOutcome compileWithTail(ParseOutcome tailParseOutcome, ObjectParseResult nextParseResult) {
		if (!ParseOutcomes.isCompiledParseResult(tailParseOutcome)) {
			return tailParseOutcome;
		}
		CompiledObjectParseResult compiledTailParseResult = (CompiledObjectParseResult) tailParseOutcome;
		CompiledObjectParseResult compiledNextParseResult = (CompiledObjectParseResult) nextParseResult;
		return isEmptyTail(compiledTailParseResult, compiledNextParseResult)
			? compiledNextParseResult
			: new CompiledParseResultWithTail(compiledTailParseResult, compiledNextParseResult);
	}

	private static boolean isEmptyTail(CompiledObjectParseResult compiledTailParseResult, CompiledObjectParseResult compiledNextParseResult) {
		return compiledTailParseResult.getPosition() == compiledNextParseResult.getPosition()
			&& compiledTailParseResult.getObjectInfo() == compiledNextParseResult.getObjectInfo();
	}

	public static ParseError createParseError(int position, String message, ParseError.ErrorPriority errorType) {
		return createParseError(position, message, errorType, null);
	}

	public static ParseError createParseError(int position, String message, ParseError.ErrorPriority errorType, Throwable throwable) {
		return new ParseErrorImpl(position, message, errorType, throwable);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	public static boolean isParseResultOfType(ParseOutcome parseOutcome, ParseResultType resultType) {
		return parseOutcome.getOutcomeType() == ParseOutcomeType.RESULT && ((ParseResult) parseOutcome).getResultType() == resultType;
	}

	public static boolean isCompiledParseResult(ParseOutcome parseOutcome) {
		return parseOutcome instanceof CompiledObjectParseResult;
	}

	private static class CompiledIdentityObjectParseResult extends AbstractCompiledParseResult
	{
		CompiledIdentityObjectParseResult(int position, ObjectInfo objectInfo) {
			super(position, objectInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return context;
		}
	}

	private static class CompiledConstantObjectParseResult extends AbstractCompiledParseResult
	{
		CompiledConstantObjectParseResult(int position, ObjectInfo objectInfo) {
			super(position, objectInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return getObjectInfo();
		}
	}

	private static class DerivedCompiledObjectParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledObjectParseResult;
		private final int						position;

		private DerivedCompiledObjectParseResult(CompiledObjectParseResult compiledObjectParseResult, int position) {
			super(position, compiledObjectParseResult.getObjectInfo());
			this.compiledObjectParseResult = compiledObjectParseResult;
			this.position = position;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			return compiledObjectParseResult.evaluate(thisInfo, context);
		}
	}

	private static class CompiledParseResultWithTail extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledTailParseResult;
		private final CompiledObjectParseResult	compiledNextParseResult;

		CompiledParseResultWithTail(CompiledObjectParseResult compiledTailParseResult, CompiledObjectParseResult compiledNextParseResult) {
			super(compiledTailParseResult.getPosition(), compiledTailParseResult.getObjectInfo());
			this.compiledTailParseResult = compiledTailParseResult;
			this.compiledNextParseResult = compiledNextParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			ObjectInfo nextObjectInfo = compiledNextParseResult.evaluate(thisInfo, context);
			return compiledTailParseResult.evaluate(thisInfo, nextObjectInfo);
		}
	}
}
