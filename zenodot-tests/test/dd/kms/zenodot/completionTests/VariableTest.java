package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class VariableTest extends CompletionTest
{
	public VariableTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		Variable variable1 = ParserSettingsUtils.createVariable("xyz", 13.0, true);
		Variable variable2 = ParserSettingsUtils.createVariable("abc", "Test", true);
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.variables(variable1, variable2))
			.addTest("x",			"x", "xyz", "xy", "xyzw", "abc")
			.addTest("xy",			"xy", "xyz", "xyzw", "x", "abc")
			.addTest("xyz",			"xyz", "xyzw", "x", "xy", "abc")
			.addTest("xyzw",		"xyzw", "xyz", "x", "xy", "abc")
			.addTest("abc",			"abc", "xyz", "x", "xy", "xyzw")
			.addTest("test(",		"xyzw", "abc", "xyz", "x", "xy")
			.addTest("test(x",		"x", "xyzw", "xyz", "xy", "abc")
			.addTest("test(xy",		"xy", "xyzw", "xyz", "x", "abc")
			.addTest("test(xyz",	"xyz", "xyzw", "x", "xy", "abc")
			.addTest("test(xyzw",	"xyzw", "xyz", "x", "xy", "abc")
			.addTest("test(abc",	"abc", "xyzw", "xyz", "x", "xy")
			.build();
	}

	private static class TestClass
	{
		private int xy;
		private byte xyzw;
		private float x;

		void test(byte b) {}
	}
}
