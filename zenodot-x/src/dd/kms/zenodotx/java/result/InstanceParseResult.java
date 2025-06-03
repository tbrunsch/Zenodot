package dd.kms.zenodotx.java.result;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.java.JavaSettings;

public interface InstanceParseResult
{
	ObjectInfo getEvaluatedResult();

	/**
	 * TODO: It is not nice that the caller has to ensure that the evaluation mode
	 *       of the settings is {@link EvaluationMode#DYNAMIC_TYPING}. (Otherwise,
	 *       the result could be {@link InfoProvider#INDETERMINATE_VALUE}.
	 */
	ObjectInfo evaluate(JavaSettings settings) throws EvaluationException;
}
