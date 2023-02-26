package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class LambdaParserTest
{
	@Parameterized.Parameters(name = "Evaluation mode: {0}")
	public static Collection<Object> getEvaluationModes() {
		return Arrays.asList(EvaluationMode.values());
	}

	private final EvaluationMode	evaluationMode;

	public LambdaParserTest(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	@Test
	public void testSupplier() throws Exception {
		final long seed = 1234567890L;
		Random random = new Random(seed);
		String completion = getFirstCompletion(Supplier.class, "() -> this.nextDou", random);
		Assert.assertEquals("nextDouble()", completion);
	}

	@Test
	public void testConsumer() throws Exception {
		String completion = getFirstCompletion(Consumer.class, "s -> System.out.println(s.equalsI");
		/*
		 * Since it is not clear, that the lambda represents a Consumer<String>, code completion
		 * cannot suggest equalsIgnoreCase().
		 */
		Assert.assertEquals("equals()", completion);
	}

	@Test
	public void testTypedConsumer() throws Exception {
		String completion = getFirstCompletion(Consumer.class, "s -> System.out.println(s.equalsI", null, String.class);
		/*
		 * In contrast to testConsumer() we specify that the lambda represents a Consumer<String>. Hence,
		 * code completion should suggest equalsIgnoreCase().
		 */
		Assert.assertEquals("equalsIgnoreCase()", completion);
	}

	@Test
	public void testBiConsumer() throws Exception {
		String completion = getFirstCompletion(BiConsumer.class, "(var1, var2) -> Collections.sort(var");
		/*
		 * Since we did not specify the types of var1 and var2, code completion will return the
		 * first best parameter matching "var".
		 */
		Assert.assertEquals("var1", completion);
	}

	@Test
	public void testTypedBiConsumer() throws Exception {
		String completion = getFirstCompletion(BiConsumer.class, "(var1, var2) -> Collections.sort(var", null, Set.class, List.class);
		/*
		 * In contrast to testBiConsumer(), we specify that the lambda represents a BiConsumer<Set, List>.
		 * Hence, code completion should prefer "var2" over "var1" because Collections.sort() only accepts List.
		 */
		Assert.assertEquals("var2", completion);
	}

	private String getFirstCompletion(Class<?> functionalInterface, String lambdaExpression) throws Exception {
		return getFirstCompletion(functionalInterface, lambdaExpression, null);
	}

	private String getFirstCompletion(Class<?> functionalInterface, String lambdaExpression, Object thisValue, Class<?>... parameterTypes) throws Exception {
		ParserSettings parserSettings = ParserSettingsBuilder.create()
			.importClasses(ImmutableList.of(java.util.Collections.class))
			.evaluationMode(evaluationMode)
			.build();
		ExpressionParser lambdaParser = Parsers.createExpressionParserBuilder(parserSettings).createLambdaParser(functionalInterface, parameterTypes);
		List<CodeCompletion> completions = lambdaParser.getCompletions(lambdaExpression, lambdaExpression.length(), thisValue);
		List<CodeCompletion> sortedCompletions = CompletionTest.getSortedCompletions(completions);
		CodeCompletion firstCompletion = Iterables.getFirst(sortedCompletions, null);
		Assert.assertNotNull(firstCompletion);
		return firstCompletion.getTextToInsert();
	}
}
