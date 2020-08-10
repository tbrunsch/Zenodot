package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ClassObjectTest extends EvaluationTest
{
	public ClassObjectTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		return new EvaluationTestBuilder()
			.addTest("Object.class",					Object.class)
			.addTest("Object.class.getSimpleName()",	Object.class.getSimpleName())
			.addTest("String.class",					String.class)
			.addTest("String.class.getSimpleName()",	String.class.getSimpleName())
			.build();
	}
}
