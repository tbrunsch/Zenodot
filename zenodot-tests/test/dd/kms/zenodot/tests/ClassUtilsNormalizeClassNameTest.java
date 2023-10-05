package dd.kms.zenodot.tests;

import dd.kms.zenodot.impl.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Test;

public class ClassUtilsNormalizeClassNameTest
{
	@Test(expected = ClassNotFoundException.class)
	public void testUnexpectedSyntax() throws ClassNotFoundException {
		ClassUtils.normalizeClassName("dd.kms.zenodot.tests.classesForTest.visibility$PublicOuterClass.PublicInnerClass");
	}

	@Test(expected = ClassNotFoundException.class)
	public void testNonExistingTopLevelClass() throws ClassNotFoundException {
		ClassUtils.normalizeClassName("dd.kms.zenodot.tests.classesForTest.visibility.Whatever");
	}

	@Test(expected = ClassNotFoundException.class)
	public void testNormalizedClassNameNonExistingTopLevelClass() throws ClassNotFoundException {
		ClassUtils.normalizeClassName("dd.kms.zenodot.tests.classesForTest.visibility.Whatever$InnerClass");
	}

	@Test(expected = ClassNotFoundException.class)
	public void testRegularClassNameNonExistingTopLevelClass() throws ClassNotFoundException {
		ClassUtils.normalizeClassName("dd.kms.zenodot.tests.classesForTest.visibility.Whatever.InnerClass");
	}

	@Test
	public void testTopLevelClassName() throws ClassNotFoundException {
		String topLevelClassName = "dd.kms.zenodot.tests.classesForTest.visibility.PublicOuterClass";
		String actual = ClassUtils.normalizeClassName(topLevelClassName);
		Assert.assertEquals(topLevelClassName, actual);
	}

	@Test
	public void testNormalizedInnerClassName() throws ClassNotFoundException {
		String normalizedClassName = "dd.kms.zenodot.tests.classesForTest.visibility.PublicOuterClass$PublicInnerClass";
		String actual = ClassUtils.normalizeClassName(normalizedClassName);
		Assert.assertEquals(normalizedClassName, actual);
	}

	@Test
	public void testRegularInnerClassName() throws ClassNotFoundException {
		String regularClassName = "dd.kms.zenodot.tests.classesForTest.visibility.PublicOuterClass.PublicInnerClass";
		String actual = ClassUtils.normalizeClassName(regularClassName);
		String normalizedClassName = regularClassName.replace(".PublicInnerClass", "$PublicInnerClass");
		Assert.assertEquals(normalizedClassName, actual);

	}
}
