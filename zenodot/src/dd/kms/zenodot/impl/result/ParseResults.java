package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.result.PackageParseResult;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.PackageInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;

import java.util.Map;

public class ParseResults
{
	public static ClassParseResult createClassParseResult(TypeInfo type) {
		return new ClassParseResultImpl(type);
	}

	public static PackageParseResult createPackageParseResult(PackageInfo packageInfo) {
		return new PackageParseResultImpl(packageInfo);
	}

	public static ObjectParseResult createCompiledIdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new IdentityObjectParseResult(objectInfo, tokenStream);
	}

	public static ObjectParseResult createCompiledConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new ConstantObjectParseResult(objectInfo, tokenStream);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	private static class IdentityObjectParseResult extends AbstractObjectParseResult
	{
		IdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return context;
		}
	}

	private static class ConstantObjectParseResult extends AbstractObjectParseResult
	{
		ConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return getObjectInfo();
		}
	}
}
