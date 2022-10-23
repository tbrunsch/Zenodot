package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.result.PackageParseResult;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.util.Map;

public class ParseResults
{
	public static ClassParseResult createClassParseResult(Class<?> type) {
		return new ClassParseResultImpl(type);
	}

	public static PackageParseResult createPackageParseResult(String packageName) {
		return new PackageParseResultImpl(packageName);
	}

	public static ObjectParseResult createCompiledIdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new IdentityObjectParseResult(objectInfo, tokenStream);
	}

	public static ObjectParseResult createCompiledConstantObjectParseResult(Object object, TokenStream tokenStream) {
		return new ConstantObjectParseResult(object, tokenStream);
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
		ConstantObjectParseResult(Object object, TokenStream tokenStream) {
			super(InfoProvider.createObjectInfo(object), tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) {
			return getObjectInfo();
		}
	}
}
