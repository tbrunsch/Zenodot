package dd.kms.zenodotx.tests.common;

/**
 * When specifying a test instance of a class that implements this interface, then this has
 * the following effects:
 * <ul>
 *     <li>
 *         The test instance is automatically reset after specifying the test, e.g., via
 *         {@link dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder#addTest(String, Object)}.
 *     </li>
 *     <li>
 *         The test instance is automatically reset after executing the test.
 *     </li>
 * </ul>
 * This is useful when writing tests with side effects that shall be executed independently of each
 * other, i.e., if the test instance shall be in initial state at the beginning of each test.<br>
 * Both calls of {@link #reset()} mentioned above are necessary to achieve this. Imagine that you want
 * to test the operator {@code +=}. A test call could look like
 * {@code builder.addTest("a += b", testInstance.a += testInstance.b}. Already by specifying the expected
 * value one triggers a side effect on {@code testInstance.a}, which is reverted by the first call of
 * {@code reset()} at the end of the implementation of {@code addTest()}. The call of {@code reset()}
 * at the end of the test executing reverts the side effect cause by the expression evaluation.
 */
public interface ResettableTestClass
{
	void reset();
}
