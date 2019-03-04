package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTestExecutor;
import dd.kms.zenodot.settings.ParserSettings;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class for creating tests with expected exceptions
 */
public class ErrorTestExecutor extends AbstractTestExecutor<ErrorTestExecutor>
{
	public ErrorTestExecutor(Object testInstance) {
		super(testInstance);
	}

	public ErrorTestExecutor test(String javaExpression, Class<? extends Exception> expectedExceptionClass) {
		return test(javaExpression, javaExpression.length(), expectedExceptionClass);
	}

	public ErrorTestExecutor test(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		JavaParser parser = new JavaParser();
		try {
			parser.suggestCodeCompletion(javaExpression, caret, settings, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
		return this;
	}
}