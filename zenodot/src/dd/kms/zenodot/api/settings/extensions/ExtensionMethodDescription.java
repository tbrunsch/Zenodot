package dd.kms.zenodot.api.settings.extensions;

import dd.kms.zenodot.impl.settings.extensions.ExtensionMethodDescriptionImpl;

import java.util.List;

/**
 * Interface that contains all information to create an extension method whose implementation
 * can be described by a single expression (that Zenodot can parse).
 */
public interface ExtensionMethodDescription
{
	/**
	 * Describes an extension method whose implementation is given by the specified {@code implementationExpression}.
	 * @param declaringClassName	The name of the class or interface that should be extended by this extension method
	 * @param name					The name of the extension method
	 * @param modifiers				The modifiers of the extension method. Only modifiers that describe the accessibility
	 *                              (i.e., {@code public}, {@code protected}, {@code private}) and whether the method is
	 *                              {@code static} will be accepted.
	 * @param returnTypeName		The fully qualified name of the class that is returned by this extension method.
	 * @param parameterTypeNames	A list of fully qualified names of the parameter types of this extension method.
	 * @param parameterNames		A list of the names of the parameters of this extension method. The parameters can
	 *                              be accessed via these names in the {@code implementationExpression}.
	 * @param varArgs				A Boolean flag whether the extension method is variadic. In this case, the last
	 *                              parameter type must be an array type.
	 * @param implementationExpression	An expression that represents the implementation of the extension method. It can
	 *                                  reference the parameters via the given {@code parameterNames}. If the extension
	 *                                  method is not {@code static}, then it can use the keyword {@code this} to refer
	 *                                  to the instance the extension method is called for.
	 * @apiNote No checks will be performed on whether the specified classes exist. This will be tested when trying to
	 *          create an extension method from this description.
	 */
	static ExtensionMethodDescription create(String declaringClassName, String name, int modifiers, String returnTypeName, List<String> parameterTypeNames, List<String> parameterNames, boolean varArgs, String implementationExpression) {
		return new ExtensionMethodDescriptionImpl(declaringClassName, name, modifiers, returnTypeName, parameterTypeNames, parameterNames, varArgs, implementationExpression);
	}

	String getDeclaringClassName();
	String getName();
	int getModifiers();
	String getReturnTypeName();
	List<String> getParameterTypeNames();
	List<String> getParameterNames();
	boolean isVarArgs();
	String getImplementationExpression();
}
