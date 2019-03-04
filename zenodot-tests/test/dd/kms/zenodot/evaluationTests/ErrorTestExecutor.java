package dd.kms.zenodot.evaluationTests;

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

	public ErrorTestExecutor test(String javaExpression) {
		ParserSettings settings = settingsBuilder.build();

		Class<? extends Exception> expectedExceptionClass = ParseException.class;
		JavaParser parser = new JavaParser();
		try {
			parser.evaluate(javaExpression, settings, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
		return this;
	}
}