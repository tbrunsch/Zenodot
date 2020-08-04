package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldArrayTestWithDynamicTyping extends CompletionTest
{
	public FieldArrayTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTestWithError("array[", ParseException.class);

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("array[",		"i0", "i1", "i2")
			.addTest("array[i0].",	"value")
			.addTest("array[i1].",	"index");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTestWithError("array[i2].",					ParseException.class)
			.addTestWithError("array[array[i1].index].",	ParseException.class);

		return testBuilder.build();
	}

	private static class ElementClass0
	{
		private double value = 1.0;
	}

	private static class ElementClass1
	{
		private int index = 3;
	}

	private static class TestClass
	{
		private int 	i0		= 0;
		private int		i1		= 1;
		private int 	i2		= 2;

		private Object 	array	= new Object[] { new ElementClass0(), new ElementClass1() };
	}
}
