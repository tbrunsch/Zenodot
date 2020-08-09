package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.common.AbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class NoExceptionTest extends AbstractTest
{
	private static final Object TEST_INSTANCE = new TestClass();

	private final String expression;

	public NoExceptionTest(String expression) {
		super(TEST_INSTANCE);
		this.expression = expression;
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		return Arrays.asList(
			" ",
			"",
			"test()",
			" test ( ) ",
			"new int[i]",
			" new  int [ i ] ",
			"new double[]{ d, i }",
			" new  double [ ] {  d ,  i  } ",
			"\"String\" instanceof String"
		);
	}

	@Test
	public void testNoException() {
		for (int caretPosition = 0; caretPosition <= expression.length(); caretPosition++) {
			testNoException(caretPosition);
		}
	}

	private void testNoException(int caretPosition) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!testNoException(caretPosition, !repeatTestAtError) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			testNoException(caretPosition, true);
		}
	}

	private boolean testNoException(int caretPosition, boolean executeAssertions) {
		ParserSettings settings = settingsBuilder.build();

		try {
			ObjectInfo thisInfo = InfoProvider.createObjectInfo(testInstance);
			Parsers.createExpressionParser(settings).getCompletions(expression, caretPosition, thisInfo);
		} catch (Exception e) {
			if (executeAssertions) {
				fail("Exception during code completion for " + expression.substring(0, caretPosition) + "^" + expression.substring(caretPosition) + ": " + e.getMessage());
			}
			return false;
		}
		return true;
	}

	private static class TestClass
	{
		private double d = 2.72;
		private int i = 7;

		private String test() { return ""; }
	}
}
