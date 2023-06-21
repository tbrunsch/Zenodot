package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.lang.reflect.Executable;
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

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<Executable, Boolean> applicableExecutableOverloads) {
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, applicableExecutableOverloads);
	}

	private static class IdentityObjectParseResult extends ObjectParseResult
	{
		IdentityObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context, VariablesImpl variables) {
			return context;
		}
	}

	private static class ConstantObjectParseResult extends ObjectParseResult
	{
		ConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context, VariablesImpl variables) {
			return getObjectInfo();
		}
	}
}
