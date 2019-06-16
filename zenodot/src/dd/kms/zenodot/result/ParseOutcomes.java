package dd.kms.zenodot.result;

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
}
