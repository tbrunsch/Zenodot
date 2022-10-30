package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.impl.common.ObjectInfoProvider;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.Variables;
import dd.kms.zenodot.impl.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

public abstract class ObjectParseResult implements ParseResult
{
	protected static final ObjectInfoProvider		OBJECT_INFO_PROVIDER		= new ObjectInfoProvider(EvaluationMode.DYNAMIC_TYPING);
	protected static final OperatorResultProvider	OPERATOR_RESULT_PROVIDER	= new OperatorResultProvider(OBJECT_INFO_PROVIDER, EvaluationMode.DYNAMIC_TYPING);

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