package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

public abstract class LambdaParseResult extends ObjectParseResult
{
	private final Class<?>	lambdaResultType;

	public LambdaParseResult(ObjectInfo objectInfo, TokenStream tokenStream, Class<?> lambdaResultType) {
		super(objectInfo, tokenStream);
		this.lambdaResultType = lambdaResultType;
	}

	public Class<?> getLambdaResultType() {
		return lambdaResultType;
	}
}
