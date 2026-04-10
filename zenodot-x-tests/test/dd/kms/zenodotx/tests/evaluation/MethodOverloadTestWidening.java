package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.*;

/* Auto generated unit test */

@RunWith(Parameterized.class)
public class MethodOverloadTestWidening extends EvaluationTest
{
	public MethodOverloadTestWidening(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(() -> testInstance);

		testBuilder
			.addTest("getBooleanChar(bool)",	testInstance.getBooleanChar(testInstance.bool))
			.addTest("getBooleanChar(c)",	testInstance.getBooleanChar(testInstance.c))
			.addTest("getBooleanByte(bool)",	testInstance.getBooleanByte(testInstance.bool))
			.addTest("getBooleanByte(b)",	testInstance.getBooleanByte(testInstance.b))
			.addTest("getBooleanShort(bool)",	testInstance.getBooleanShort(testInstance.bool))
			.addTest("getBooleanShort(b)",	testInstance.getBooleanShort(testInstance.b))
			.addTest("getBooleanShort(s)",	testInstance.getBooleanShort(testInstance.s))
			.addTest("getBooleanInt(bool)",	testInstance.getBooleanInt(testInstance.bool))
			.addTest("getBooleanInt(c)",	testInstance.getBooleanInt(testInstance.c))
			.addTest("getBooleanInt(b)",	testInstance.getBooleanInt(testInstance.b))
			.addTest("getBooleanInt(s)",	testInstance.getBooleanInt(testInstance.s))
			.addTest("getBooleanInt(i)",	testInstance.getBooleanInt(testInstance.i))
			.addTest("getBooleanLong(bool)",	testInstance.getBooleanLong(testInstance.bool))
			.addTest("getBooleanLong(c)",	testInstance.getBooleanLong(testInstance.c))
			.addTest("getBooleanLong(b)",	testInstance.getBooleanLong(testInstance.b))
			.addTest("getBooleanLong(s)",	testInstance.getBooleanLong(testInstance.s))
			.addTest("getBooleanLong(i)",	testInstance.getBooleanLong(testInstance.i))
			.addTest("getBooleanLong(l)",	testInstance.getBooleanLong(testInstance.l))
			.addTest("getBooleanFloat(bool)",	testInstance.getBooleanFloat(testInstance.bool))
			.addTest("getBooleanFloat(c)",	testInstance.getBooleanFloat(testInstance.c))
			.addTest("getBooleanFloat(b)",	testInstance.getBooleanFloat(testInstance.b))
			.addTest("getBooleanFloat(s)",	testInstance.getBooleanFloat(testInstance.s))
			.addTest("getBooleanFloat(i)",	testInstance.getBooleanFloat(testInstance.i))
			.addTest("getBooleanFloat(l)",	testInstance.getBooleanFloat(testInstance.l))
			.addTest("getBooleanFloat(f)",	testInstance.getBooleanFloat(testInstance.f))
			.addTest("getBooleanDouble(bool)",	testInstance.getBooleanDouble(testInstance.bool))
			.addTest("getBooleanDouble(c)",	testInstance.getBooleanDouble(testInstance.c))
			.addTest("getBooleanDouble(b)",	testInstance.getBooleanDouble(testInstance.b))
			.addTest("getBooleanDouble(s)",	testInstance.getBooleanDouble(testInstance.s))
			.addTest("getBooleanDouble(i)",	testInstance.getBooleanDouble(testInstance.i))
			.addTest("getBooleanDouble(l)",	testInstance.getBooleanDouble(testInstance.l))
			.addTest("getBooleanDouble(f)",	testInstance.getBooleanDouble(testInstance.f))
			.addTest("getBooleanDouble(d)",	testInstance.getBooleanDouble(testInstance.d))
			.addTest("getCharByte(c)",	testInstance.getCharByte(testInstance.c))
			.addTest("getCharByte(b)",	testInstance.getCharByte(testInstance.b))
			.addTest("getCharShort(c)",	testInstance.getCharShort(testInstance.c))
			.addTest("getCharShort(b)",	testInstance.getCharShort(testInstance.b))
			.addTest("getCharShort(s)",	testInstance.getCharShort(testInstance.s))
			.addTest("getCharInt(c)",	testInstance.getCharInt(testInstance.c))
			.addTest("getCharInt(b)",	testInstance.getCharInt(testInstance.b))
			.addTest("getCharInt(s)",	testInstance.getCharInt(testInstance.s))
			.addTest("getCharInt(i)",	testInstance.getCharInt(testInstance.i))
			.addTest("getCharLong(c)",	testInstance.getCharLong(testInstance.c))
			.addTest("getCharLong(b)",	testInstance.getCharLong(testInstance.b))
			.addTest("getCharLong(s)",	testInstance.getCharLong(testInstance.s))
			.addTest("getCharLong(i)",	testInstance.getCharLong(testInstance.i))
			.addTest("getCharLong(l)",	testInstance.getCharLong(testInstance.l))
			.addTest("getCharFloat(c)",	testInstance.getCharFloat(testInstance.c))
			.addTest("getCharFloat(b)",	testInstance.getCharFloat(testInstance.b))
			.addTest("getCharFloat(s)",	testInstance.getCharFloat(testInstance.s))
			.addTest("getCharFloat(i)",	testInstance.getCharFloat(testInstance.i))
			.addTest("getCharFloat(l)",	testInstance.getCharFloat(testInstance.l))
			.addTest("getCharFloat(f)",	testInstance.getCharFloat(testInstance.f))
			.addTest("getCharDouble(c)",	testInstance.getCharDouble(testInstance.c))
			.addTest("getCharDouble(b)",	testInstance.getCharDouble(testInstance.b))
			.addTest("getCharDouble(s)",	testInstance.getCharDouble(testInstance.s))
			.addTest("getCharDouble(i)",	testInstance.getCharDouble(testInstance.i))
			.addTest("getCharDouble(l)",	testInstance.getCharDouble(testInstance.l))
			.addTest("getCharDouble(f)",	testInstance.getCharDouble(testInstance.f))
			.addTest("getCharDouble(d)",	testInstance.getCharDouble(testInstance.d))
			.addTest("getByteShort(b)",	testInstance.getByteShort(testInstance.b))
			.addTest("getByteShort(s)",	testInstance.getByteShort(testInstance.s))
			.addTest("getByteInt(c)",	testInstance.getByteInt(testInstance.c))
			.addTest("getByteInt(b)",	testInstance.getByteInt(testInstance.b))
			.addTest("getByteInt(s)",	testInstance.getByteInt(testInstance.s))
			.addTest("getByteInt(i)",	testInstance.getByteInt(testInstance.i))
			.addTest("getByteLong(c)",	testInstance.getByteLong(testInstance.c))
			.addTest("getByteLong(b)",	testInstance.getByteLong(testInstance.b))
			.addTest("getByteLong(s)",	testInstance.getByteLong(testInstance.s))
			.addTest("getByteLong(i)",	testInstance.getByteLong(testInstance.i))
			.addTest("getByteLong(l)",	testInstance.getByteLong(testInstance.l))
			.addTest("getByteFloat(c)",	testInstance.getByteFloat(testInstance.c))
			.addTest("getByteFloat(b)",	testInstance.getByteFloat(testInstance.b))
			.addTest("getByteFloat(s)",	testInstance.getByteFloat(testInstance.s))
			.addTest("getByteFloat(i)",	testInstance.getByteFloat(testInstance.i))
			.addTest("getByteFloat(l)",	testInstance.getByteFloat(testInstance.l))
			.addTest("getByteFloat(f)",	testInstance.getByteFloat(testInstance.f))
			.addTest("getByteDouble(c)",	testInstance.getByteDouble(testInstance.c))
			.addTest("getByteDouble(b)",	testInstance.getByteDouble(testInstance.b))
			.addTest("getByteDouble(s)",	testInstance.getByteDouble(testInstance.s))
			.addTest("getByteDouble(i)",	testInstance.getByteDouble(testInstance.i))
			.addTest("getByteDouble(l)",	testInstance.getByteDouble(testInstance.l))
			.addTest("getByteDouble(f)",	testInstance.getByteDouble(testInstance.f))
			.addTest("getByteDouble(d)",	testInstance.getByteDouble(testInstance.d))
			.addTest("getShortInt(c)",	testInstance.getShortInt(testInstance.c))
			.addTest("getShortInt(b)",	testInstance.getShortInt(testInstance.b))
			.addTest("getShortInt(s)",	testInstance.getShortInt(testInstance.s))
			.addTest("getShortInt(i)",	testInstance.getShortInt(testInstance.i))
			.addTest("getShortLong(c)",	testInstance.getShortLong(testInstance.c))
			.addTest("getShortLong(b)",	testInstance.getShortLong(testInstance.b))
			.addTest("getShortLong(s)",	testInstance.getShortLong(testInstance.s))
			.addTest("getShortLong(i)",	testInstance.getShortLong(testInstance.i))
			.addTest("getShortLong(l)",	testInstance.getShortLong(testInstance.l))
			.addTest("getShortFloat(c)",	testInstance.getShortFloat(testInstance.c))
			.addTest("getShortFloat(b)",	testInstance.getShortFloat(testInstance.b))
			.addTest("getShortFloat(s)",	testInstance.getShortFloat(testInstance.s))
			.addTest("getShortFloat(i)",	testInstance.getShortFloat(testInstance.i))
			.addTest("getShortFloat(l)",	testInstance.getShortFloat(testInstance.l))
			.addTest("getShortFloat(f)",	testInstance.getShortFloat(testInstance.f))
			.addTest("getShortDouble(c)",	testInstance.getShortDouble(testInstance.c))
			.addTest("getShortDouble(b)",	testInstance.getShortDouble(testInstance.b))
			.addTest("getShortDouble(s)",	testInstance.getShortDouble(testInstance.s))
			.addTest("getShortDouble(i)",	testInstance.getShortDouble(testInstance.i))
			.addTest("getShortDouble(l)",	testInstance.getShortDouble(testInstance.l))
			.addTest("getShortDouble(f)",	testInstance.getShortDouble(testInstance.f))
			.addTest("getShortDouble(d)",	testInstance.getShortDouble(testInstance.d))
			.addTest("getIntLong(c)",	testInstance.getIntLong(testInstance.c))
			.addTest("getIntLong(b)",	testInstance.getIntLong(testInstance.b))
			.addTest("getIntLong(s)",	testInstance.getIntLong(testInstance.s))
			.addTest("getIntLong(i)",	testInstance.getIntLong(testInstance.i))
			.addTest("getIntLong(l)",	testInstance.getIntLong(testInstance.l))
			.addTest("getIntFloat(c)",	testInstance.getIntFloat(testInstance.c))
			.addTest("getIntFloat(b)",	testInstance.getIntFloat(testInstance.b))
			.addTest("getIntFloat(s)",	testInstance.getIntFloat(testInstance.s))
			.addTest("getIntFloat(i)",	testInstance.getIntFloat(testInstance.i))
			.addTest("getIntFloat(l)",	testInstance.getIntFloat(testInstance.l))
			.addTest("getIntFloat(f)",	testInstance.getIntFloat(testInstance.f))
			.addTest("getIntDouble(c)",	testInstance.getIntDouble(testInstance.c))
			.addTest("getIntDouble(b)",	testInstance.getIntDouble(testInstance.b))
			.addTest("getIntDouble(s)",	testInstance.getIntDouble(testInstance.s))
			.addTest("getIntDouble(i)",	testInstance.getIntDouble(testInstance.i))
			.addTest("getIntDouble(l)",	testInstance.getIntDouble(testInstance.l))
			.addTest("getIntDouble(f)",	testInstance.getIntDouble(testInstance.f))
			.addTest("getIntDouble(d)",	testInstance.getIntDouble(testInstance.d))
			.addTest("getLongFloat(c)",	testInstance.getLongFloat(testInstance.c))
			.addTest("getLongFloat(b)",	testInstance.getLongFloat(testInstance.b))
			.addTest("getLongFloat(s)",	testInstance.getLongFloat(testInstance.s))
			.addTest("getLongFloat(i)",	testInstance.getLongFloat(testInstance.i))
			.addTest("getLongFloat(l)",	testInstance.getLongFloat(testInstance.l))
			.addTest("getLongFloat(f)",	testInstance.getLongFloat(testInstance.f))
			.addTest("getLongDouble(c)",	testInstance.getLongDouble(testInstance.c))
			.addTest("getLongDouble(b)",	testInstance.getLongDouble(testInstance.b))
			.addTest("getLongDouble(s)",	testInstance.getLongDouble(testInstance.s))
			.addTest("getLongDouble(i)",	testInstance.getLongDouble(testInstance.i))
			.addTest("getLongDouble(l)",	testInstance.getLongDouble(testInstance.l))
			.addTest("getLongDouble(f)",	testInstance.getLongDouble(testInstance.f))
			.addTest("getLongDouble(d)",	testInstance.getLongDouble(testInstance.d))
			.addTest("getFloatDouble(c)",	testInstance.getFloatDouble(testInstance.c))
			.addTest("getFloatDouble(b)",	testInstance.getFloatDouble(testInstance.b))
			.addTest("getFloatDouble(s)",	testInstance.getFloatDouble(testInstance.s))
			.addTest("getFloatDouble(i)",	testInstance.getFloatDouble(testInstance.i))
			.addTest("getFloatDouble(l)",	testInstance.getFloatDouble(testInstance.l))
			.addTest("getFloatDouble(f)",	testInstance.getFloatDouble(testInstance.f))
			.addTest("getFloatDouble(d)",	testInstance.getFloatDouble(testInstance.d));

		testBuilder
			.addTestWithError("getBooleanChar(b)",	SemanticException.class)
			.addTestWithError("getBooleanChar(s)",	SemanticException.class)
			.addTestWithError("getBooleanChar(i)",	SemanticException.class)
			.addTestWithError("getBooleanChar(l)",	SemanticException.class)
			.addTestWithError("getBooleanChar(f)",	SemanticException.class)
			.addTestWithError("getBooleanChar(d)",	SemanticException.class)
			.addTestWithError("getBooleanByte(c)",	SemanticException.class)
			.addTestWithError("getBooleanByte(s)",	SemanticException.class)
			.addTestWithError("getBooleanByte(i)",	SemanticException.class)
			.addTestWithError("getBooleanByte(l)",	SemanticException.class)
			.addTestWithError("getBooleanByte(f)",	SemanticException.class)
			.addTestWithError("getBooleanByte(d)",	SemanticException.class)
			.addTestWithError("getBooleanShort(c)",	SemanticException.class)
			.addTestWithError("getBooleanShort(i)",	SemanticException.class)
			.addTestWithError("getBooleanShort(l)",	SemanticException.class)
			.addTestWithError("getBooleanShort(f)",	SemanticException.class)
			.addTestWithError("getBooleanShort(d)",	SemanticException.class)
			.addTestWithError("getBooleanInt(l)",	SemanticException.class)
			.addTestWithError("getBooleanInt(f)",	SemanticException.class)
			.addTestWithError("getBooleanInt(d)",	SemanticException.class)
			.addTestWithError("getBooleanLong(f)",	SemanticException.class)
			.addTestWithError("getBooleanLong(d)",	SemanticException.class)
			.addTestWithError("getBooleanFloat(d)",	SemanticException.class)
			.addTestWithError("getCharByte(bool)",	SemanticException.class)
			.addTestWithError("getCharByte(s)",	SemanticException.class)
			.addTestWithError("getCharByte(i)",	SemanticException.class)
			.addTestWithError("getCharByte(l)",	SemanticException.class)
			.addTestWithError("getCharByte(f)",	SemanticException.class)
			.addTestWithError("getCharByte(d)",	SemanticException.class)
			.addTestWithError("getCharShort(bool)",	SemanticException.class)
			.addTestWithError("getCharShort(i)",	SemanticException.class)
			.addTestWithError("getCharShort(l)",	SemanticException.class)
			.addTestWithError("getCharShort(f)",	SemanticException.class)
			.addTestWithError("getCharShort(d)",	SemanticException.class)
			.addTestWithError("getCharInt(bool)",	SemanticException.class)
			.addTestWithError("getCharInt(l)",	SemanticException.class)
			.addTestWithError("getCharInt(f)",	SemanticException.class)
			.addTestWithError("getCharInt(d)",	SemanticException.class)
			.addTestWithError("getCharLong(bool)",	SemanticException.class)
			.addTestWithError("getCharLong(f)",	SemanticException.class)
			.addTestWithError("getCharLong(d)",	SemanticException.class)
			.addTestWithError("getCharFloat(bool)",	SemanticException.class)
			.addTestWithError("getCharFloat(d)",	SemanticException.class)
			.addTestWithError("getCharDouble(bool)",	SemanticException.class)
			.addTestWithError("getByteShort(bool)",	SemanticException.class)
			.addTestWithError("getByteShort(c)",	SemanticException.class)
			.addTestWithError("getByteShort(i)",	SemanticException.class)
			.addTestWithError("getByteShort(l)",	SemanticException.class)
			.addTestWithError("getByteShort(f)",	SemanticException.class)
			.addTestWithError("getByteShort(d)",	SemanticException.class)
			.addTestWithError("getByteInt(bool)",	SemanticException.class)
			.addTestWithError("getByteInt(l)",	SemanticException.class)
			.addTestWithError("getByteInt(f)",	SemanticException.class)
			.addTestWithError("getByteInt(d)",	SemanticException.class)
			.addTestWithError("getByteLong(bool)",	SemanticException.class)
			.addTestWithError("getByteLong(f)",	SemanticException.class)
			.addTestWithError("getByteLong(d)",	SemanticException.class)
			.addTestWithError("getByteFloat(bool)",	SemanticException.class)
			.addTestWithError("getByteFloat(d)",	SemanticException.class)
			.addTestWithError("getByteDouble(bool)",	SemanticException.class)
			.addTestWithError("getShortInt(bool)",	SemanticException.class)
			.addTestWithError("getShortInt(l)",	SemanticException.class)
			.addTestWithError("getShortInt(f)",	SemanticException.class)
			.addTestWithError("getShortInt(d)",	SemanticException.class)
			.addTestWithError("getShortLong(bool)",	SemanticException.class)
			.addTestWithError("getShortLong(f)",	SemanticException.class)
			.addTestWithError("getShortLong(d)",	SemanticException.class)
			.addTestWithError("getShortFloat(bool)",	SemanticException.class)
			.addTestWithError("getShortFloat(d)",	SemanticException.class)
			.addTestWithError("getShortDouble(bool)",	SemanticException.class)
			.addTestWithError("getIntLong(bool)",	SemanticException.class)
			.addTestWithError("getIntLong(f)",	SemanticException.class)
			.addTestWithError("getIntLong(d)",	SemanticException.class)
			.addTestWithError("getIntFloat(bool)",	SemanticException.class)
			.addTestWithError("getIntFloat(d)",	SemanticException.class)
			.addTestWithError("getIntDouble(bool)",	SemanticException.class)
			.addTestWithError("getLongFloat(bool)",	SemanticException.class)
			.addTestWithError("getLongFloat(d)",	SemanticException.class)
			.addTestWithError("getLongDouble(bool)",	SemanticException.class)
			.addTestWithError("getFloatDouble(bool)",	SemanticException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final boolean	bool	= true;
		private final char		c		= 'c';
		private final byte		b		= 1;
		private final short		s		= 2;
		private final int		i		= 3;
		private final long		l		= 4L;
		private final float		f		= 5f;
		private final double	d		= 6d;

		private boolean getBooleanChar(boolean bool) { return bool; }
		private char getBooleanChar(char c) { return c; }

		private boolean getBooleanByte(boolean bool) { return bool; }
		private byte getBooleanByte(byte b) { return b; }

		private boolean getBooleanShort(boolean bool) { return bool; }
		private short getBooleanShort(short s) { return s; }

		private boolean getBooleanInt(boolean bool) { return bool; }
		private int getBooleanInt(int i) { return i; }

		private boolean getBooleanLong(boolean bool) { return bool; }
		private long getBooleanLong(long l) { return l; }

		private boolean getBooleanFloat(boolean bool) { return bool; }
		private float getBooleanFloat(float f) { return f; }

		private boolean getBooleanDouble(boolean bool) { return bool; }
		private double getBooleanDouble(double d) { return d; }

		private char getCharByte(char c) { return c; }
		private byte getCharByte(byte b) { return b; }

		private char getCharShort(char c) { return c; }
		private short getCharShort(short s) { return s; }

		private char getCharInt(char c) { return c; }
		private int getCharInt(int i) { return i; }

		private char getCharLong(char c) { return c; }
		private long getCharLong(long l) { return l; }

		private char getCharFloat(char c) { return c; }
		private float getCharFloat(float f) { return f; }

		private char getCharDouble(char c) { return c; }
		private double getCharDouble(double d) { return d; }

		private byte getByteShort(byte b) { return b; }
		private short getByteShort(short s) { return s; }

		private byte getByteInt(byte b) { return b; }
		private int getByteInt(int i) { return i; }

		private byte getByteLong(byte b) { return b; }
		private long getByteLong(long l) { return l; }

		private byte getByteFloat(byte b) { return b; }
		private float getByteFloat(float f) { return f; }

		private byte getByteDouble(byte b) { return b; }
		private double getByteDouble(double d) { return d; }

		private short getShortInt(short s) { return s; }
		private int getShortInt(int i) { return i; }

		private short getShortLong(short s) { return s; }
		private long getShortLong(long l) { return l; }

		private short getShortFloat(short s) { return s; }
		private float getShortFloat(float f) { return f; }

		private short getShortDouble(short s) { return s; }
		private double getShortDouble(double d) { return d; }

		private int getIntLong(int i) { return i; }
		private long getIntLong(long l) { return l; }

		private int getIntFloat(int i) { return i; }
		private float getIntFloat(float f) { return f; }

		private int getIntDouble(int i) { return i; }
		private double getIntDouble(double d) { return d; }

		private long getLongFloat(long l) { return l; }
		private float getLongFloat(float f) { return f; }

		private long getLongDouble(long l) { return l; }
		private double getLongDouble(double d) { return d; }

		private float getFloatDouble(float f) { return f; }
		private double getFloatDouble(double d) { return d; }
	}
}
