package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorTest extends EvaluationTest
{
	public BinaryOperatorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder();

		testBuilder
			.addTest("5 *7- 8 / 3*2 + 4 * 2",	5*7 - 8/3*2 + 4*2)
			.addTest("5 + 7 * 8",				5 + 7 * 8)
			.addTest("(5 + 7) * 8",				(5 + 7) * 8)
			.addTest(" 5%3 -7 / 2.0",			5 % 3 - 7/2.0)
			.addTest("5 + 4 + \"Test\"",		5 + 4 + "Test")
			.addTest("5 + \"Test\" + 4",		5 + "Test" + 4)
			.addTest("\"Test\" + 5 + 4",		"Test" + 5 + 4)
			.addTest("\"Test\" + 'y'",			"Test" + 'y')
			.addTest("'y' + \"Test\"",			'y' +"Test")
			.addTest("-27 >> 2 << 2",			-27 >> 2 << 2)
			.addTest("-23456 >>> 3 << 1",		-23456 >>> 3 << 1)
			.addTest("(byte) 23 << 2",			(byte) 23 << 2)
			.addTest("9*3 < 4*7",				9*3 < 4*7)
			.addTest("9*4 < 3.0*12",			9*4 < 3.0*12)
			.addTest("9*3 <= 4*7",				9*3 <= 4*7)
			.addTest("9*4 <= 3.0*12",			9*4 <= 3.0*12)
			.addTest("5*5 <= 4*6",				5*5 <= 4*6)
			.addTest("4*7 > 9*3",				4*7 > 9*3)
			.addTest("3.0*12 > 9*4",			3.0*12 > 9*4)
			.addTest("4*7 >= 9*3",				4*7 >= 9*3)
			.addTest("3.0*12 >= 9*4",			3.0*12 >= 9*4)
			.addTest("4*6 >= 5*5",				4*6 >= 5*5)
			.addTest("9*3 == 4*7",				9*3 == 4*7)
			.addTest("9*4 == 3.0*12",			9*4 == 3.0*12)
			.addTest("5*5 == 4*6",				5*5 == 4*6)
			.addTest("true == false",			true == false)
			.addTest("false == false",			false == false)
			.addTest("false == Boolean.FALSE",	false == Boolean.FALSE)
			.addTest("'c' == 'c'",				'c' == 'c')
			.addTest("'c' == 'd'",				'c' == 'd')
			.addTest("'c' == 1",				'c' == 1)
			.addTest("9*3 != 4*7",				9*3 != 4*7)
			.addTest("9*4 != 3.0*12",			9*4 != 3.0*12)
			.addTest("5*5 != 4*6",				5*5 != 4*6)
			.addTest("true != false",			true != false)
			.addTest("false != false",			false != false)
			.addTest("false != Boolean.FALSE",	false != Boolean.FALSE)
			.addTest("'c' != 'c'",				'c' != 'c')
			.addTest("'c' != 'd'",				'c' != 'd')
			.addTest("'c' != 1",				'c' != 1)
			.addTest("123 & 234",				123 & 234)
			.addTest("123 ^ 234",				123 ^ 234)
			.addTest("123 | 234",				123 | 234)
			.addTest("false && false",			false && false)
			.addTest("false && true",			false && true)
			.addTest("true && false",			true && false)
			.addTest("true && true",			true && true)
			.addTest("false || false",			false || false)
			.addTest("false || true",			false || true)
			.addTest("true || false",			true || false)
			.addTest("true || true",			true || true);

		testBuilder
			.testInstance(testInstance)
			.addTest("s instanceof String",												testInstance.s instanceof String)
			.addTest("s instanceof Double",												false)
			.addTest("o instanceof Object",												testInstance.o instanceof Object)
			.addTest("o instanceof Float",												testInstance.o instanceof Float)
			.addTest("o instanceof Float && ((Float) o).floatValue() == 123.0f",		testInstance.o instanceof Float && ((Float) testInstance.o).floatValue() == 123.0f)
			.addTest("o instanceof Float && (Float) o == 123.0f",						testInstance.o instanceof Float && (Float) testInstance.o == 123.0f)
			.addTest("d instanceof Double && i instanceof Integer",						testInstance.d instanceof Double && testInstance.i instanceof Integer)
			.addTest("n instanceof Object",												testInstance.n instanceof Object)
			.addTest("n instanceof Object && n.toString().length() == 4",				testInstance.n instanceof Object && testInstance.n.toString().length() == 4)
			.addTest("o instanceof String && ((String) o).length() == 3",				testInstance.o instanceof String && ((String) testInstance.o).length() == 3)
			.addTest("\"abc\" instanceof String && Boolean.TRUE instanceof Boolean",	"abc" instanceof String && Boolean.TRUE instanceof Boolean)
			.addTest("5 + \"3\" instanceof String",										5 + "3" instanceof String);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private String	s	= "123";
		private Double	d	= 123.0;
		private Integer	i	= 123;
		private Object	o	= 123.0f;
		private Object	n	= null;
	}
}
