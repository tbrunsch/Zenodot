package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.result.InstanceParseResult;

import java.util.ArrayList;
import java.util.List;

public class ConstructorParseInfo
{
	private final Class<?>					constructorClass;
	private final List<InstanceParseResult>	parameters			= new ArrayList<>();

	public ConstructorParseInfo(Class<?> constructorClass) {
		this.constructorClass = constructorClass;
	}

	public Class<?> getConstructorClass() {
		return constructorClass;
	}

	public List<InstanceParseResult> getParameters() {
		return parameters;
	}

	public ConstructorParseInfo addParameter(InstanceParseResult parameter) {
		ConstructorParseInfo constructorParseInfo = new ConstructorParseInfo(constructorClass);
		constructorParseInfo.parameters.addAll(parameters);
		constructorParseInfo.parameters.add(parameter);
		return constructorParseInfo;
	}
}
