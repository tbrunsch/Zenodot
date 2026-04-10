package $TEST_PACKAGE$;

import dd.kms.zenodotx.exception.SemanticException;
import $TEST_PACKAGE$.framework.EvaluationTest;
import $TEST_PACKAGE$.framework.EvaluationTestBuilder;
import $TEST_PACKAGE$.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.*;

/* Auto generated unit test */

@RunWith(Parameterized.class)
public class $CLASS_NAME$ extends EvaluationTest
{
	public $CLASS_NAME$(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(() -> testInstance);

		testBuilder
$SUCCESSFUL_TESTS$

		testBuilder
$TESTS_WITH_ERROR$

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final boolean	$BOOLEAN_VARIABLE$	= true;
		private final char		$CHAR_VARIABLE$		= 'c';
		private final byte		$BYTE_VARIABLE$		= 1;
		private final short		$SHORT_VARIABLE$		= 2;
		private final int		$INT_VARIABLE$		= 3;
		private final long		$LONG_VARIABLE$		= 4L;
		private final float		$FLOAT_VARIABLE$		= 5f;
		private final double	$DOUBLE_VARIABLE$		= 6d;
$OVERLOADS$
	}
}
