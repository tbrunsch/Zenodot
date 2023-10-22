package dd.kms.zenodot.framework.result;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

public abstract class ObjectParseResult implements ParseResult
{
	private final ObjectInfo 	objectInfo;
	private final String		expression;
	private final int			position;

	public ObjectParseResult(ObjectInfo objectInfo, TokenStream tokenStream) {
		this.objectInfo = objectInfo;
		this.expression = tokenStream.getExpression();
		this.position = tokenStream.getPosition();
	}

	protected abstract ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context, Variables variables) throws Exception;

	public String getExpression() {
		return expression;
	}

	public int getPosition() {
		return position;
	}

	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	public final ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context, Variables variables) throws ParseException {
		try {
			return doEvaluate(thisInfo, context, variables);
		} catch (ParseException e) {
			throw e;
		} catch (Throwable t) {
			String error = ParseUtils.formatException(t, new StringBuilder()).toString();
			throw new ParseException(expression, position, error, t);
		}
	}

	@Override
	public String toString() {
		return objectInfo.toString();
	}
}
