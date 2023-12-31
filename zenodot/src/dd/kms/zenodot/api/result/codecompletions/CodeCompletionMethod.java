package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.result.CodeCompletion;

import java.lang.reflect.Method;

public interface CodeCompletionMethod extends CodeCompletion
{
	GeneralizedMethod getMethod();
}
