package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.dataproviders.ObjectInfoProvider;
import dd.kms.zenodot.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public abstract class AbstractCompiledParseResult extends ObjectParseResultImpl implements CompiledObjectParseResult
{
	protected static final ObjectInfoProvider		OBJECT_INFO_PROVIDER		= new ObjectInfoProvider(EvaluationMode.STATICALLY_TYPED);
	protected static final OperatorResultProvider	OPERATOR_RESULT_PROVIDER	= new OperatorResultProvider(OBJECT_INFO_PROVIDER, EvaluationMode.STATICALLY_TYPED);

	public AbstractCompiledParseResult(int position, ObjectInfo objectInfo) {
		super(position, objectInfo);
	}

	@Override
	public boolean isCompiled() {
		return true;
	}
}
