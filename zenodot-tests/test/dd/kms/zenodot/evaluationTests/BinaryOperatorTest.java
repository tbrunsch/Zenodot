package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class BinaryOperatorTest extends EvaluationTest
{
	public BinaryOperatorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		return new EvaluationTestBuilder()
			.addTest("5 *7- 8 / 3*2 + 4 * 2",	5*7 - 8/3*2 + 4*2)
			.addTest("5 + 7 * 8",				5 + 7 * 8)
			.addTest("(5 + 7) * 8",				(5 + 7) * 8)
			.addTest(" 5%3 -7 / 2.0",			5 % 3 - 7/2.0)
			.addTest("5 + 4 + \"Test\"",		5 + 4 + "Test")
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
			.addTest("9*3 != 4*7",				9*3 != 4*7)
			.addTest("9*4 != 3.0*12",			9*4 != 3.0*12)
			.addTest("5*5 != 4*6",				5*5 != 4*6)
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
			.addTest("true || true",			true || true)
			.build();
	}
}
