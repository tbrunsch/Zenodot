package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaState;

import java.util.regex.Pattern;

class JavaRuleSetUtils
{
	static final Pattern	IDENTIFIER_PATTERN	= Pattern.compile("[_\\$A-Za-z][_\\$A-Za-z0-9]*");

	static void executeMethod(JavaState state) {
		/*
		 * TODO: The top most element of the stack describes everything required to execute a method.
		 *       Pop it, execute the method, and push the result on the stack again.
		 */
	}

	static void invokeConstructor(JavaState state) {
		/*
		 * TODO: The top most element of the stack describes everything required to invoke a constructor.
		 *       Pop it, invoke the constructor, and push the result on the stack again.
		 */
	}

	static void checkArrayType(JavaState state) {
		// TODO: Check that the top most element of the stack is of an array type
	}

	static void accessArrayElement(JavaState state) {
		/*
		 * TODO: The top most element of the stack is an array index. Below is an array object. Remove both,
		 *       access the array element, and push it onto the stack.
		 */
	}

	static void performCast(JavaState state) {
		/*
		 * TODO: The top most element of the stack is an object. Below is a class. Remove both
		 *       perform the cast, and push it onto the stack.
		 */
	}

	static void checkType(JavaState state, Class<?> clazz) {
		// TODO: Check that the type of the top-most element of the stack is if the specified type
	}

	static void beginParsingLambda(JavaState state) {
		// TODO: Push something onto the stack for collecting lambda parameter names
	}
}
