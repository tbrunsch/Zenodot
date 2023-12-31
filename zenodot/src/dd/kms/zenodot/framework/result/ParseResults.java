package dd.kms.zenodot.framework.result;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.GeneralizedExecutable;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import java.util.Map;

public class ParseResults
{
	public static ClassParseResult createClassParseResult(Class<?> type) {
		return new ClassParseResult(type);
	}

	public static PackageParseResult createPackageParseResult(String packageName) {
		return new PackageParseResult(packageName);
	}

	public static ObjectParseResult createCompiledIdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new IdentityObjectParseResult(objectInfo, tokenStream);
	}

	public static ObjectParseResult createCompiledConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new ConstantObjectParseResult(objectInfo, tokenStream);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<GeneralizedExecutable, Boolean> applicableExecutableOverloads) {
		return new dd.kms.zenodot.impl.result.ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	private static class IdentityObjectParseResult extends ObjectParseResult
	{
		IdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context, Variables variables) {
			return context;
		}
	}

	private static class ConstantObjectParseResult extends ObjectParseResult
	{
		ConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context, Variables variables) {
			return getObjectInfo();
		}
	}
}
