package dd.kms.zenodot.tests.completionTests.framework;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.tests.common.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class CompletionTest extends AbstractTest<CompletionTest>
{
	public static List<CodeCompletion> getSortedCompletions(List<CodeCompletion> completions) {
		List<CodeCompletion> sortedCompletions = new ArrayList<>(completions);
		sortedCompletions.sort(Parsers.COMPLETION_COMPARATOR);
		return sortedCompletions;
	}

	private static List<String> extractCompletions(List<CodeCompletion> completions) {
		return getSortedCompletions(completions).stream()
			.map(completion -> completion.getTextToInsert())
			.collect(Collectors.toList());
	}

	private static String applyCompletion(String expression, CodeCompletion completion) {
		int insertionBegin = completion.getInsertionBegin();
		int insertionEnd = completion.getInsertionEnd();
		String textToInsert = completion.getTextToInsert();
		return expression.substring(0, insertionBegin)
			+ textToInsert
			+ expression.substring(insertionEnd);
	}

	private final TestExecutor	testExecutor;

	protected CompletionTest(TestData testData) {
		super(testData.getTestInstance());
		TestConfigurator testConfigurator = testData.getConfigureSettingsFunction();
		if (testConfigurator != null) {
			testConfigurator.configure(this);
		}
		this.testExecutor = testData.getTestExecutor();
	}

	@Test
	public void testCompletion() {
		testExecutor.executeTest(this);
	}

	void testCompletion(String expression, String[] expectedCompletions) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(expression, !repeatTestAtError, expectedCompletions) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(expression, true, expectedCompletions);
		}
	}

	void testInsertion(String expression, int caretPosition, String expectedResult) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runInsertionTest(expression, caretPosition, !repeatTestAtError, expectedResult) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runInsertionTest(expression, caretPosition, true, expectedResult);
		}
	}

	void testCompletionWithError(String expression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		try {
			ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(settings)
				.variables(variables)
				.createExpressionParser();
			expressionParser.getCompletions(expression, caretPosition, testInstance);
			fail("Expression: " + expression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			if (e.getClass() != expectedExceptionClass) {
				e.printStackTrace();
				Assert.assertEquals(expectedExceptionClass, e.getClass());
			}
		}
	}

	private boolean runTest(String expression, boolean executeAssertions, String... expectedCompletions) {
		ParserSettings settings = settingsBuilder.build();

		int caretPosition = expression.length();
		List<String> completions;
		try {
			ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(settings)
				.variables(variables)
				.createExpressionParser();
			completions = extractCompletions(expressionParser.getCompletions(expression, caretPosition, testInstance));
		} catch (ParseException e) {
			if (executeAssertions) {
				e.printStackTrace();
				fail("Exception during code completion: " + e.getMessage());
			}
			return false;
		}
		if (executeAssertions) {
			assertTrue(MessageFormat.format("expected completions: {1}, actual completions: {2}",
					expression,
					Arrays.asList(expectedCompletions),
					completions),
					completions.size() >= expectedCompletions.length);
		}
		if (completions.size() < expectedCompletions.length) {
			return false;
		}

		for (int i = 0; i < expectedCompletions.length; i++) {
			if (executeAssertions) {
				assertEquals(expectedCompletions[i], completions.get(i));
			}
			if (!Objects.equals(expectedCompletions[i], completions.get(i))) {
				return false;
			}
		}
		return true;
	}

	private boolean runInsertionTest(String expression, int caretPosition, boolean executeAssertions, String expectedResult) {
		ParserSettings settings = settingsBuilder.build();

		String expressionAfterInsertion = null;
		try {
			ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(settings)
				.variables(variables)
				.createExpressionParser();
			List<CodeCompletion> sortedCompletions = getSortedCompletions(expressionParser.getCompletions(expression, caretPosition, testInstance));
			if (!sortedCompletions.isEmpty()) {
				CodeCompletion bestCompletion = sortedCompletions.get(0);
				expressionAfterInsertion = applyCompletion(expression, bestCompletion);
			}
		} catch (ParseException e) {
			if (executeAssertions) {
				e.printStackTrace();
				fail("Exception during code completion: " + e.getMessage());
			}
			return false;
		}
		if (expressionAfterInsertion == null) {
			if (executeAssertions) {
				fail("No completions available");
			}
			return false;
		}

		if (executeAssertions) {
			assertEquals("Unexpected expression after insertion", expectedResult, expressionAfterInsertion);
		}
		return Objects.equals(expressionAfterInsertion, expectedResult);
	}
}
