package dd.kms.zenodot.impl.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.result.PackageParseResult;
import dd.kms.zenodot.impl.wrappers.ExecutableInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.lang.reflect.Executable;
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

	public static ObjectParseResult createCompiledConstantObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		return new ConstantObjectParseResult(objectInfo, tokenStream);
	}

	public static ExecutableArgumentInfo createExecutableArgumentInfo(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		ImmutableMap.Builder<Executable, Boolean> builder = ImmutableMap.builder();
		for (Map.Entry<ExecutableInfo, Boolean> entry : applicableExecutableOverloads.entrySet()) {
			builder.put(entry.getKey().getExecutable(), entry.getValue());
		}
		return new ExecutableArgumentInfoImpl(currentArgumentIndex, builder.build());
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
