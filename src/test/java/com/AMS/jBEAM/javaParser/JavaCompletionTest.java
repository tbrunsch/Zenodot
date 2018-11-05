package com.AMS.jBEAM.javaParser;

import com.google.common.base.Joiner;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavaCompletionTest
{
    @Test
    public void testField() {
    	abstract class BasicTestClass
		{
			private int 	xy 		= 13;
			private char 	XYZ		= 'W';
			private float	X		= 1.0f;

			private short	other	= 0;
		}

		class TestClass extends BasicTestClass
		{
			private String 	XY 		= "27";
			private long	xy_z	= 13;
			private double	x		= 2.72;
		}

		Object testInstance = new TestClass();
        new TestBuilder(testInstance, EvaluationMode.NONE)
            .addTest("xy",   "xy", "XY", "xy_z", "XYZ", "x", "X")
			.addTest("XYZ",  "XYZ", "XY", "X", "x", "xy")
			.addTest("X", "X", "x", "XY", "XYZ", "xy_z", "xy")
			.addTest("XY", "XY", "xy", "XYZ", "xy_z", "X", "x")
			.addTest("xy_z", "xy_z", "x", "xy", "XY", "X")
			.addTest("x", "x", "X", "xy_z", "xy", "XY", "XYZ")
			.addTest("XYW", "XY", "X", "x", "xy")
            .performTests();

        new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("xy", -1, IllegalStateException.class)
			.addTest("bla", -1, JavaParseException.class)
			.addTest("xy,", 3, JavaParseException.class)
			.performTests();
    }

    @Test
    public void testFieldDotField() {
		class TestClass
		{
			private int 		xy 		= 13;
			private float		X		= 1.0f;
			private char 		XYZ		= 'W';

			private TestClass	member	= null;
		}

		TestClass testInstance = new TestClass();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("member.", "member", "X", "xy", "XYZ")
			.addTest("member.x", "X", "xy", "XYZ", "member")
			.addTest("member.xy", "xy", "XYZ", "X", "member")
			.addTest("member.xyz", "XYZ", "xy", "X", "member")
			.addTest("member.mem", "member", "X", "xy", "XYZ")
			.performTests();

		new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("membeR.", -1, JavaParseException.class)
			.addTest("MEMBER.xy", -1, JavaParseException.class)
			.addTest("member.xy.XY", -1, JavaParseException.class)
			.addTest("member.xy", -1, IllegalStateException.class)
			.performTests();
    }

    @Test
	public void testFieldDotFieldWithEvaluationMode() {
    	class BaseClass
		{
			private int x;
			private int xyz;
		}

		class DescendantClass extends BaseClass
		{
			private int	xy;
		}

    	class TestClass
		{
			private BaseClass member = new DescendantClass();
		}

		TestClass testInstance = new TestClass();
    	new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("member.", "x", "xyz")
			.addTest("member.x", "x", "xyz")
			.addTest("member.xy", "xyz", "x")
			.addTest("member.xyz", "xyz", "x")
			.performTests();

		new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("member.", "xy", "x", "xyz")
			.addTest("member.x", "x", "xy", "xyz")
			.addTest("member.xy", "xy", "xyz", "x")
			.addTest("member.xyz", "xyz", "xy", "x")
			.performTests();
	}

    @Test
    public void testFieldArray() {
    	class TestClass
		{
			private String 		xy		= "xy";
			private int 		xyz		= 7;
			private char		xyzw	= 'W';

			private TestClass[]	member	= null;
		}

		final String hashCode = "hashCode()";
		TestClass testInstance = new TestClass();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("member[", "xyz", hashCode, "xyzw", "member", "xy")
			.addTest("member[x", "xyz", "xyzw", "xy", hashCode, "member")
			.addTest("member[xy", "xy", "xyz", "xyzw", hashCode, "member")
			.addTest("member[xyz", "xyz", "xyzw", "xy", hashCode, "member")
			.addTest("member[xyzw", "xyzw", "xyz", "xy", hashCode, "member")
			.addTest("member[m", "member", "xyz", hashCode, "xyzw", "xy")
			.addTest("member[xyz].", "member", "xy", "xyz", "xyzw")
			.addTest("member[xyzw].x", "xy", "xyz", "xyzw", "member")
			.performTests();

    	new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("xy[", 3, JavaParseException.class)
			.addTest("xyz[", 4, JavaParseException.class)
			.addTest("xyzw[", 5, JavaParseException.class)
			.addTest("member[xy].", 11, JavaParseException.class)
			.addTest("member[xyz]", -1, IllegalStateException.class)
			.addTest("member[xyz)", 11, JavaParseException.class)
			.performTests();
    }

    @Test
	public void testFieldArrayWithEvaluationMode() {
    	class ElementClass0
		{
			private double value = 1.0;
		}

    	class ElementClass1
		{
			private int index = 3;
		}

    	class TestClass
		{
			private int 	i0		= 0;
			private int		i1		= 1;
			private int 	i2		= 2;

			private Object 	array	= new Object[] { new ElementClass0(), new ElementClass1() };
		}

		TestClass testInstance = new TestClass();
    	new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("array[", 6, JavaParseException.class)
			.performTests();

    	new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("array[", "i0", "i1", "i2")
			.addTest("array[i0].", "value")
			.addTest("array[i1].", "index")
			.performTests();

    	new ErrorTestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("array[i2].", 10, JavaParseException.class)
			.addTest("array[array[i1].index].", 23, JavaParseException.class)
			.performTests();
	}

	@Test
	public void testMethod() {
    	/*
    	 * Convention for this test: Methods whose names consist of n characters have n arguments.
    	 *
    	 * This allows for auto-generating the insertion text of a method:
    	 *
    	 * "x" -> "x()", "xy" -> "xy(, )", "xyz" -> "xyz(, , )" etc.
    	 *
    	 * Exception: Method "other" whose sole purpose is not to appear among the first suggestions,
    	 *            which makes this test stronger.
    	 */
		Function<String, String> methodFormatter = s -> s + "(" + Joiner.on(", ").join(IntStream.range(0, s.length()).mapToObj(i -> "").collect(Collectors.toList())) + ")";
		Function<String[], String[]> methodsFormatter = methods -> Arrays.stream(methods).map(methodFormatter::apply).toArray(size -> new String[size]);

		abstract class BasicTestClass
		{
			private int 	xy(char c, float f)						{ return 13; }
			private char 	XYZ(float f, int i, double d)			{ return 'W'; }
			private float	X(int i)								{ return 1.0f; }

			private short	other()									{ return 0; }
		}

		class TestClass extends BasicTestClass
		{
			private String 	XY(long l, double d)					{ return "27"; }
			private long	xy_z(double d, short s, int i, byte b)	{ return 13; }
			private double	x(double d)								{ return 2.72; }

			private byte	XYZ(char c, double d, boolean b)		{ return 1; }
		}

		Object testInstance = new TestClass();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("xy",		methodsFormatter.apply(new String[]{"xy", "XY", "xy_z", "XYZ", "XYZ", "x", "X"}))
			.addTest("XYZ",	methodsFormatter.apply(new String[]{"XYZ", "XYZ", "XY", "X", "x", "xy"}))
			.addTest("X",		methodsFormatter.apply(new String[]{"X", "x", "XY", "XYZ", "XYZ", "xy_z", "xy"}))
			.addTest("XY",		methodsFormatter.apply(new String[]{"XY", "xy", "XYZ", "XYZ", "xy_z", "X", "x"}))
			.addTest("xy_z",	methodsFormatter.apply(new String[]{"xy_z", "x", "xy", "XY", "X"}))
			.addTest("x",		methodsFormatter.apply(new String[]{"x", "X", "xy_z", "xy", "XY", "XYZ", "XYZ"}))
			.addTest("XYW",	methodsFormatter.apply(new String[]{"XY", "X", "x", "xy"}))
			.performTests();

		new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("other()", -1, IllegalStateException.class)
			.addTest("bla", -1, JavaParseException.class)
			.addTest("other(),", 8, JavaParseException.class)
			.performTests();
	}

	@Test
	public void testMethodArguments() {
    	class TestClass
		{
			private int 	prefixI	= 1;
			private double 	prefixD	= 1.0;
			private char	prefixC	= 'A';

			private int		prefixI(double arg)	{ return 1; }
			private double	prefixD(char arg)	{ return 1.0; }
			private char	prefixC(int arg)	{ return 'A'; }
		}

		Object testInstance = new TestClass();
    	new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("prefix", "prefixC", "prefixD", "prefixI", "prefixC()", "prefixD()", "prefixI()")
			.addTest("prefixI", "prefixI", "prefixI()", "prefixC", "prefixD")
			.addTest("prefixD", "prefixD", "prefixD()", "prefixC", "prefixI")
			.addTest("prefixC", "prefixC", "prefixC()", "prefixD", "prefixI")
			.addTest("prefixI(", "prefixD", "prefixD()", "prefixC", "prefixI", "prefixC()", "prefixI()")
			.addTest("prefixD(", "prefixC", "prefixC()", "prefixD", "prefixI", "prefixD()", "prefixI()")
			.addTest("prefixC(", "prefixI", "prefixI()", "hashCode()", "prefixC", "prefixC()", "prefixD", "prefixD()")
			.performTests();

    	new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("prefixI(prefixD)", -1, IllegalStateException.class)
			.addTest("prefixD(prefixI)", 16, JavaParseException.class)
			.addTest("prefixC(prefixI,", 16, JavaParseException.class)
			.addTest("prefixI(prefixD))", -1, JavaParseException.class)
			.performTests();
	}

	@Test
	public void testMethodArgumentsWithEvaluation() {
    	class TestClass
		{
			private Object getObject() { return new TestClass(); }
			private Object getTestClassObject(TestClass testClass) { return testClass.getObject(); }
		}

		Object testInstance = new TestClass();
    	new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getTestClassObject(getObject()).get", 35, JavaParseException.class)
			.performTests();

    	new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("getTestClassObject(getObject()).get", "getObject()", "getTestClassObject()", "getClass()")
			.performTests();
	}

	@Test
	public void testMethodDotFieldOrMethod() {
    	class TestClass1
		{
			private int 	i	= 1;
			private double	d	= 1.0;

			private Object getObject(double d) { return null; }
		}

		class TestClass2
		{
			private short	s	= 1;
			private char	c	= 'A';

			private TestClass1 getTestClass(char c) { return null; }
		}

		Object testInstance = new TestClass2();
    	new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getTestClass", "getTestClass()")
			.addTest("getTestClass(c).", "d", "i", "getObject()")
			.addTest("getTestClass(c).get", "getObject()")
			.addTest("getTestClass(c).getObject(", "c", "s")
			.addTest("getTestClass(c).getObject(getTestClass(c).d).", "clone()", "equals()")
			.performTests();

		new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
				.addTest("getTestClazz().", -1, JavaParseException.class)
				.addTest("getTestClazz().i", -1, JavaParseException.class)
				.addTest("getTestClass().i.d", -1, JavaParseException.class)
				.addTest("getTestClass(c).getObject(getTestClass(c).d)", -1, IllegalStateException.class)
				.performTests();
	}

	@Test
	public void testMethodDotFieldOrMethodWithEvaluation() {
		class BaseClass
		{
			private int x;
			private int xyz;

			public int getInt() { return 1; }
		}

		class DescendantClass extends BaseClass
		{
			private int	xy;

			public double getDouble() { return 1.0; }
		}

		class TestClass
		{
			private BaseClass getObject() { return new DescendantClass(); }
		}

		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getObject().", "x", "xyz", "getInt()")
			.addTest("getObject().x", "x", "xyz", "getInt()")
			.addTest("getObject().xy", "xyz", "x", "getInt()")
			.addTest("getObject().xyz", "xyz", "x", "getInt()")
			.addTest("getObject().get", "getInt()", getClass, "x", "xyz")
			.performTests();

		new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("getObject().", "xy", "x", "xyz", "getDouble()", "getInt()")
			.addTest("getObject().x", "x", "xy", "xyz", "getDouble()", "getInt()")
			.addTest("getObject().xy", "xy", "xyz", "x", "getDouble()", "getInt()")
			.addTest("getObject().xyz", "xyz", "xy", "x", "getDouble()", "getInt()")
			.addTest("getObject().get", "getDouble()", "getInt()", getClass, "xy", "x", "xyz")
			.performTests();
	}

	@Test
	public void testMethodArray() {
    	class TestClass
		{
			private String 		xy		= "xy";
			private int 		xyz		= 7;
			private char		xyzw	= 'W';

			private TestClass[] getTestClasses() { return new TestClass[0]; }
		}

		final String hashCode = "hashCode()";
    	final String getClass = "getClass()";
		TestClass testInstance = new TestClass();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getTestClasses()[", "xyz", hashCode, "xyzw", "xy")
			.addTest("getTestClasses()[x", "xyz", "xyzw", "xy", hashCode)
			.addTest("getTestClasses()[xy", "xy", "xyz", "xyzw", hashCode)
			.addTest("getTestClasses()[xyz", "xyz", "xyzw", "xy", hashCode)
			.addTest("getTestClasses()[xyzw", "xyzw", "xyz", "xy", hashCode)
			.addTest("getTestClasses()[g", "getTestClasses()", getClass, "xyz", hashCode, "xyzw", "xy")
			.addTest("getTestClasses()[xyz].", "xy", "xyz", "xyzw")
			.addTest("getTestClasses()[xyzw].x", "xy", "xyz", "xyzw")
			.performTests();

		new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("xy[", 3, JavaParseException.class)
			.addTest("xyz[", 4, JavaParseException.class)
			.addTest("xyzw[", 5, JavaParseException.class)
			.addTest("getTestClasses()[xy].", 21, JavaParseException.class)
			.addTest("getTestClasses()[xyz]", -1, IllegalStateException.class)
			.addTest("getTestClasses()[xyz)", 21, JavaParseException.class)
			.performTests();
	}

	@Test
	public void testMethodArrayWithEvaluation() {
    	class TestClass
		{
			private int index	= 1;
			private int size 	= 3;

			private Object getArray(int size) { return new TestClass[size]; }
		}

		TestClass testInstance = new TestClass();
    	new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getArray(size)[", 15, JavaParseException.class)
			.performTests();

    	new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("getArray(size)[", "index", "size")
			.addTest("getArray(size)[index].", "index", "size")
			.performTests();
	}

	@Test
	public void testMethodOverload() {
    	class TestClass1
		{
			int myInt;
		}

		class TestClass2
		{
			String myString;
		}

		class TestClass3
		{
			int intValue;
			String stringValue;

			TestClass1 getTestClass(int i, String s) { return null; }
			TestClass2 getTestClass(String s, int i) { return null; }
		}

		final String hashCode = "hashCode()";
    	final String toString = "toString()";
		TestClass3 testInstance = new TestClass3();
		new TestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getTestClass(", "intValue", "stringValue")
			.addTest("getTestClass(i", "intValue", "stringValue", hashCode)
			.addTest("getTestClass(s", "stringValue", "intValue")
			.addTest("getTestClass(intValue,", "stringValue", toString, "intValue")
			.addTest("getTestClass(stringValue,", "intValue", hashCode, "stringValue")
			.addTest("getTestClass(intValue,stringValue).", "myInt")
			.addTest("getTestClass(stringValue,intValue).", "myString")
			.performTests();
	}

	@Test
	public void testMethodOverloadWithEvaluation() {
		class TestClass1
		{
			int myInt;
		}

		class TestClass2
		{
			String myString;
		}

		class TestClass3
		{
			int i = 0;
			int j = 1;

			Object getTestClass(int i) { return i == 0 ? new TestClass1() : new TestClass2(); }

			TestClass1 getTestClass(TestClass1 testClass) { return testClass; }
			TestClass2 getTestClass(TestClass2 testClass) { return testClass; }
		}

		TestClass3 testInstance = new TestClass3();
		new ErrorTestBuilder(testInstance, EvaluationMode.NONE)
			.addTest("getTestClass(getTestClass(i)).", 30, JavaParseException.class)
			.performTests();

		new TestBuilder(testInstance, EvaluationMode.DUCK_TYPING)
			.addTest("getTestClass(getTestClass(i)).", "myInt")
			.addTest("getTestClass(getTestClass(j)).", "myString")
			.performTests();
	}

	private static List<String> extractSuggestions(List<CompletionSuggestionIF> completions) {
        return completions.stream()
            .map(completion -> completion.getTextToInsert())
            .collect(Collectors.toList());
    }

    /*
     * Class for creating tests with expected successful code completions
     */
    private static class TestBuilder
    {
    	private final Object					testInstance;
    	private final EvaluationMode 			evaluationMode;
        private final Map<String, List<String>> expectedResults = new LinkedHashMap<>();

        TestBuilder(Object testInstance, EvaluationMode evaluationMode) {
        	this.testInstance = testInstance;
        	this.evaluationMode = evaluationMode;
		}

        TestBuilder addTest(String javaExpression, String... expectedSuggestions) {
            expectedResults.put(javaExpression, Arrays.asList(expectedSuggestions));
            return this;
        }

        void performTests() {
            JavaParser parser = new JavaParser();
            for (Map.Entry<String, List<String>> inputOutputPair : expectedResults.entrySet()) {
                String javaExpression = inputOutputPair.getKey();
                List<String> expectedSuggestions = inputOutputPair.getValue();
                int caret = javaExpression.length();
                List<String> suggestions = null;
                try {
                    suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, evaluationMode, caret, testInstance));
                } catch (JavaParseException e) {
                    assertTrue("Exception during code completion: " + e.getMessage(), false);
                }
                assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
                        javaExpression,
                        expectedSuggestions,
                        suggestions),
                        suggestions.size() > expectedSuggestions.size());
                for (int i = 0; i < expectedSuggestions.size(); i++) {
                    assertEquals("Expression: " + javaExpression, expectedSuggestions.get(i), suggestions.get(i));
                }
            }
        }
    }

    /*
     * Class for creating tests with expected exceptions
     */
    private static class ErrorTestBuilder
	{
		private static class JavaExpressionCaretPair
		{
			private final String javaExpression;

			private final int caret;

			private JavaExpressionCaretPair(String javaExpression, int caret) {
				this.javaExpression = javaExpression;
				this.caret = caret;
			}

			String getJavaExpression() {
				return javaExpression;
			}

			int getCaret() {
				return caret;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				JavaExpressionCaretPair that = (JavaExpressionCaretPair) o;
				return caret == that.caret &&
						Objects.equals(javaExpression, that.javaExpression);
			}

			@Override
			public int hashCode() {
				return Objects.hash(javaExpression, caret);
			}
		}

		private final Object													testInstance;
		private final EvaluationMode 											evaluationMode;
		private final Map<JavaExpressionCaretPair, Class<? extends Exception>>	testDataToExpectedExceptionClass	= new LinkedHashMap<>();

		ErrorTestBuilder(Object testInstance, EvaluationMode evaluationMode) {
			this.testInstance = testInstance;
			this.evaluationMode = evaluationMode;
		}

		ErrorTestBuilder addTest(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
			testDataToExpectedExceptionClass.put(new JavaExpressionCaretPair(javaExpression, caret), expectedExceptionClass);
			return this;
		}

		void performTests() {
			JavaParser parser = new JavaParser();
			for (Map.Entry<JavaExpressionCaretPair, Class<? extends Exception>> inputOutputPair : testDataToExpectedExceptionClass.entrySet()) {
				JavaExpressionCaretPair testData = inputOutputPair.getKey();
				String javaExpression = testData.getJavaExpression();
				int caret = testData.getCaret();
				Class<? extends Exception> expectedExceptionClass = inputOutputPair.getValue();
				try {
					parser.suggestCodeCompletion(javaExpression, evaluationMode, caret, testInstance);
					assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
				} catch (JavaParseException | IllegalStateException e) {
					assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
				}
			}
		}
	}
}
