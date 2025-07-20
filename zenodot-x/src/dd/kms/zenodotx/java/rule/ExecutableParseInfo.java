package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.wrappers.ExecutableInfo;
import dd.kms.zenodotx.java.result.InstanceParseResult;

import java.util.ArrayList;
import java.util.List;

public class ExecutableParseInfo
{
	private final List<ExecutableInfo>		executableInfos;
	private final InstanceParseResult		context;
	private final List<InstanceParseResult>	parameters	= new ArrayList<>();

	public ExecutableParseInfo(List<ExecutableInfo> executableInfos, InstanceParseResult context) {
		this.executableInfos = executableInfos;
		this.context = context;
	}

	public List<ExecutableInfo> getExecutableInfos() {
		return executableInfos;
	}

	public InstanceParseResult getContext() {
		return context;
	}

	public List<InstanceParseResult> getParameters() {
		return parameters;
	}

	public ExecutableParseInfo addParameter(InstanceParseResult parameter) {
		ExecutableParseInfo executableParseInfo = new ExecutableParseInfo(executableInfos, context);
		executableParseInfo.parameters.addAll(parameters);
		executableParseInfo.parameters.add(parameter);
		return executableParseInfo;
	}
}
