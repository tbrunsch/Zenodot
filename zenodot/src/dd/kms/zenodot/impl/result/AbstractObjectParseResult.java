package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.dataproviders.ObjectInfoProvider;
import dd.kms.zenodot.impl.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public abstract class AbstractObjectParseResult implements ObjectParseResult
{
	protected static final ObjectInfoProvider		OBJECT_INFO_PROVIDER		= new ObjectInfoProvider(true);
	protected static final OperatorResultProvider	OPERATOR_RESULT_PROVIDER	= new OperatorResultProvider(OBJECT_INFO_PROVIDER, true);

	private final ObjectInfo 	objectInfo;
	private final int			position;

	public AbstractObjectParseResult(ObjectInfo objectInfo, int position) {
		this.objectInfo = objectInfo;
		this.position = position;
	}

	protected abstract ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception;

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	@Override
	public final ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws ParseException {
		try {
			return doEvaluate(thisInfo, context);
		} catch (ParseException e) {
			throw e;
		} catch (Throwable t) {
			String error = ParseUtils.formatException(t, new StringBuilder()).toString();
			throw new ParseException(position, error, t);
		}
	}

	@Override
	public String toString() {
		return objectInfo.toString();
	}
}
