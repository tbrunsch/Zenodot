package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.result.InstanceParseResult;

import java.util.ArrayList;
import java.util.List;

class ConstructorParseInfo
{
	private final Class<?>					constructorClass;
	private final List<InstanceParseResult>	parameters			= new ArrayList<>();

	ConstructorParseInfo(Class<?> constructorClass) {
		this.constructorClass = constructorClass;
	}

	Class<?> getConstructorClass() {
		return constructorClass;
	}

	List<InstanceParseResult> getParameters() {
		return parameters;
	}

	void addParameter(InstanceParseResult parameter) {
		parameters.add(parameter);
	}
}
