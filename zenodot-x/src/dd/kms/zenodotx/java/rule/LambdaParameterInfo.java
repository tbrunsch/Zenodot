package dd.kms.zenodotx.java.rule;

import java.util.ArrayList;
import java.util.List;

public class LambdaParameterInfo
{
	private final List<String>	parameterNames	= new ArrayList<>();

	public void addParameter(String parameterName) {
		parameterNames.add(parameterName);
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}
}
