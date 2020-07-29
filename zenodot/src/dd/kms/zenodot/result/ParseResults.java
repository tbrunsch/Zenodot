package dd.kms.zenodot.result;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.EvaluationException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.parsers.AbstractParser;
import dd.kms.zenodot.parsers.ClassTailParser;
import dd.kms.zenodot.parsers.ObjectTailParser;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.tokenizer.TokenStream;
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

	public static ObjectParseResult createCompiledIdentityObjectParseResult(ObjectInfo objectInfo, int position) {
		return new IdentityObjectParseResult(objectInfo, position);
	}

	public static ObjectParseResult createCompiledConstantObjectParseResult(ObjectInfo objectInfo, int position) {
		return new ConstantObjectParseResult(objectInfo, position);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	public static ObjectParseResult parseTail(TokenStream tokenStream, ParseResult parseResult, ParserToolbox parserToolbox, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		if (parseResult instanceof ObjectParseResult) {
			ObjectParseResult objectParseResult = (ObjectParseResult) parseResult;
			ObjectInfo objectInfo = objectParseResult.getObjectInfo();
			AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> objectTailParser = parserToolbox.createParser(ObjectTailParser.class);
			ObjectParseResult tailParseResult = objectTailParser.parse(tokenStream, objectInfo, expectation);
			return new ParseResultWithTail(objectParseResult, tailParseResult, tokenStream.getPosition());
		} else if (parseResult instanceof ClassParseResult) {
			ClassParseResult classParseResult = (ClassParseResult) parseResult;
			TypeInfo type = classParseResult.getType();
			AbstractParser<TypeInfo, ObjectParseResult, ObjectParseResultExpectation> classTailParser = parserToolbox.createParser(ClassTailParser.class);
			return classTailParser.parse(tokenStream, type, expectation);
		} else {
			throw new InternalErrorException("Can only parse tails of objects and classes, but requested for " + parseResult.getClass().getSimpleName());
		}
	}

	private static class IdentityObjectParseResult extends AbstractObjectParseResult
	{
		IdentityObjectParseResult(ObjectInfo objectInfo, int position) {
			super(objectInfo, position);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return context;
		}
	}

	private static class ConstantObjectParseResult extends AbstractObjectParseResult
	{
		ConstantObjectParseResult(ObjectInfo objectInfo, int position) {
			super(objectInfo, position);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return getObjectInfo();
		}
	}

	private static class ParseResultWithTail extends AbstractObjectParseResult
	{
		private final ObjectParseResult parseResult;
		private final ObjectParseResult tailParseResult;

		ParseResultWithTail(ObjectParseResult parseResult, ObjectParseResult tailParseResult, int position) {
			super(tailParseResult.getObjectInfo(), position);
			this.parseResult = parseResult;
			this.tailParseResult = tailParseResult;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) throws ParseException {
			ObjectInfo nextObjectInfo = parseResult.evaluate(thisInfo, context);
			return tailParseResult.evaluate(thisInfo, nextObjectInfo);
		}
	}
}
