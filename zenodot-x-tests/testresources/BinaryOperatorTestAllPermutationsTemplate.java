package $TEST_PACKAGE$;

import dd.kms.zenodotx.exception.SemanticException;
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
		EvaluationTestBuilder $TEST_BUILDER_NAME$ = new EvaluationTestBuilder().testInstanceProvider(() -> $TEST_INSTANCE_NAME$);

$TEST_COLLECTION_METHOD_CALLS$

		return $TEST_BUILDER_NAME$.build();
	}
$TEST_COLLECTION_METHODS$

	private static class $TEST_INSTANCE_CLASS$
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
	}
}
