package $TEST_PACKAGE$;

import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.common.ResettableTestClass;
import $TEST_PACKAGE$.framework.EvaluationTest;
import $TEST_PACKAGE$.framework.EvaluationTestBuilder;
import $TEST_PACKAGE$.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

/* Auto generated unit test */

@RunWith(Parameterized.class)
public class $CLASS_NAME$ extends EvaluationTest
{
	public $CLASS_NAME$(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		$TEST_INSTANCE_CLASS$ $TEST_INSTANCE_NAME$ = new TestClass();
		EvaluationTestBuilder $TEST_BUILDER_NAME$ = new EvaluationTestBuilder().testInstance($TEST_INSTANCE_NAME$);

$TEST_COLLECTION_METHOD_CALLS$

		return $TEST_BUILDER_NAME$.build();
	}
$TEST_COLLECTION_METHODS$

	private static class $TEST_INSTANCE_CLASS$ implements ResettableTestClass
	{
		final boolean	$BOOLEAN_VARIABLE_1$ = false;
		final boolean	$BOOLEAN_VARIABLE_2$ = true;
		final char		$CHAR_VARIABLE_1$ = 51;
		final char		$CHAR_VARIABLE_2$ = 3;
		final byte		$BYTE_VARIABLE_1$ = 43;
		final byte		$BYTE_VARIABLE_2$ = 7;
		final short		$SHORT_VARIABLE_1$ = 21;
		final short		$SHORT_VARIABLE_2$ = 8;
		final int		$INT_VARIABLE_1$ = 67;
		final int		$INT_VARIABLE_2$ = 6;
		final long		$LONG_VARIABLE_1$ = 35;
		final long		$LONG_VARIABLE_2$ = 4;
		final float		$FLOAT_VARIABLE_1$ = 27.3f;
		final float		$FLOAT_VARIABLE_2$ = 3.6f;
		final double	$DOUBLE_VARIABLE_1$ = 42.7;
		final double	$DOUBLE_VARIABLE_2$ = 7.2;
		final Boolean	$BOOLEAN_VARIABLE_BOXED_1$ = $BOOLEAN_VARIABLE_1$;
		final Boolean	$BOOLEAN_VARIABLE_BOXED_2$ = $BOOLEAN_VARIABLE_2$;
		final Character	$CHAR_VARIABLE_BOXED_1$ = $CHAR_VARIABLE_1$;
		final Character	$CHAR_VARIABLE_BOXED_2$ = $CHAR_VARIABLE_2$;
		final Byte		$BYTE_VARIABLE_BOXED_1$ = $BYTE_VARIABLE_1$;
		final Byte		$BYTE_VARIABLE_BOXED_2$ = $BYTE_VARIABLE_2$;
		final Short		$SHORT_VARIABLE_BOXED_1$ = $SHORT_VARIABLE_1$;
		final Short		$SHORT_VARIABLE_BOXED_2$ = $SHORT_VARIABLE_2$;
		final Integer	$INT_VARIABLE_BOXED_1$ = $INT_VARIABLE_1$;
		final Integer	$INT_VARIABLE_BOXED_2$ = $INT_VARIABLE_2$;
		final Long		$LONG_VARIABLE_BOXED_1$ = $LONG_VARIABLE_1$;
		final Long		$LONG_VARIABLE_BOXED_2$ = $LONG_VARIABLE_2$;
		final Float		$FLOAT_VARIABLE_BOXED_1$ = $FLOAT_VARIABLE_1$;
		final Float		$FLOAT_VARIABLE_BOXED_2$ = $FLOAT_VARIABLE_2$;
		final Double	$DOUBLE_VARIABLE_BOXED_1$ = $DOUBLE_VARIABLE_1$;
		final Double	$DOUBLE_VARIABLE_BOXED_2$ = $DOUBLE_VARIABLE_2$;
		final String	$STRING_VARIABLE_1$ = "s1";
		final String	$STRING_VARIABLE_2$ = "s2";
		final Object	$OBJECT_VARIABLE_1$ = new ArrayList<>();
		final Object	$OBJECT_VARIABLE_2$ = new RuntimeException();

		boolean	$BOOLEAN_VARIABLE_FOR_ASSIGNMENT$;
		char		$CHAR_VARIABLE_FOR_ASSIGNMENT$;
		byte		$BYTE_VARIABLE_FOR_ASSIGNMENT$;
		short		$SHORT_VARIABLE_FOR_ASSIGNMENT$;
		int		$INT_VARIABLE_FOR_ASSIGNMENT$;
		long		$LONG_VARIABLE_FOR_ASSIGNMENT$;
		float		$FLOAT_VARIABLE_FOR_ASSIGNMENT$;
		double	$DOUBLE_VARIABLE_FOR_ASSIGNMENT$;
		Boolean	$BOOLEAN_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Character	$CHAR_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Byte		$BYTE_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Short		$SHORT_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Integer	$INT_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Long		$LONG_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Float		$FLOAT_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		Double	$DOUBLE_VARIABLE_BOXED_FOR_ASSIGNMENT$;
		String	$STRING_VARIABLE_FOR_ASSIGNMENT$;
		Object	$OBJECT_VARIABLE_FOR_ASSIGNMENT$;

		$TEST_INSTANCE_CLASS$() {
			reset();
		}

		@Override
		public void reset() {
			$BOOLEAN_VARIABLE_FOR_ASSIGNMENT$ = false;
			$CHAR_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$BYTE_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$SHORT_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$INT_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$LONG_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$FLOAT_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$DOUBLE_VARIABLE_FOR_ASSIGNMENT$ = 0;
			$BOOLEAN_VARIABLE_BOXED_FOR_ASSIGNMENT$ = false;
			$CHAR_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0;
			$BYTE_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0;
			$SHORT_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0;
			$INT_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0;
			$LONG_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0L;
			$FLOAT_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0f;
			$DOUBLE_VARIABLE_BOXED_FOR_ASSIGNMENT$ = 0d;
			$STRING_VARIABLE_FOR_ASSIGNMENT$ = null;
			$OBJECT_VARIABLE_FOR_ASSIGNMENT$ = null;
		}
	}
}
