package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Map;

public class ParseResults
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

	public static ParseError createParseError(int position, String message, ParseError.ErrorPriority errorType) {
		return createParseError(position, message, errorType, null);
	}

	public static ParseError createParseError(int position, String message, ParseError.ErrorPriority errorType, Throwable throwable) {
		return new ParseErrorImpl(position, message, errorType, throwable);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}
}
