package dd.kms.zenodot.impl.settings.extensions;

import dd.kms.zenodot.api.settings.extensions.ExtensionMethodDescription;

import java.util.List;

public class ExtensionMethodDescriptionImpl implements ExtensionMethodDescription
{
	private final String		declaringClassName;
	private final String		name;
	private final int			modifiers;
	private final String		returnTypeName;
	private final List<String>	parameterTypeNames;
	private final List<String>	parameterNames;
	private final boolean		varArgs;
	private final String		implementationExpression;

	public ExtensionMethodDescriptionImpl(String declaringClassName, String name, int modifiers, String returnTypeName, List<String> parameterTypeNames, List<String> parameterNames, boolean varArgs, String implementationExpression) {
		this.declaringClassName = declaringClassName;
		this.name = name;
		this.modifiers = modifiers;
		this.returnTypeName = returnTypeName;
		this.parameterTypeNames = parameterTypeNames;
		this.parameterNames = parameterNames;
		this.varArgs = varArgs;
		this.implementationExpression = implementationExpression;
	}

	@Override
	public String getDeclaringClassName() {
		return declaringClassName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public String getReturnTypeName() {
		return returnTypeName;
	}

	@Override
	public List<String> getParameterTypeNames() {
		return parameterTypeNames;
	}

	@Override
	public List<String> getParameterNames() {
		return parameterNames;
	}

	@Override
	public boolean isVarArgs() {
		return varArgs;
	}

	@Override
	public String getImplementationExpression() {
		return implementationExpression;
	}
}
