package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class MethodArrayTestWithDynamicTyping extends CompletionTest
{
	public MethodArrayTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTestWithError("getArray(size)[", ParseException.class);

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("getArray(size)[",			"index", "size")
			.addTest("getArray(size)[index].",	"index", "size");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private int index	= 1;
		private int size 	= 3;

		private Object getArray(int size) {
			return IntStream.range(0, size).mapToObj(i -> new TestClass()).toArray(n -> new TestClass[n]);
		}
	}
}
