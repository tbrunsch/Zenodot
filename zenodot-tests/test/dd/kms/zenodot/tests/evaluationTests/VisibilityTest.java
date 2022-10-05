package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.wrappers.ClassInfo;
import dd.kms.zenodot.tests.classesForTest.visibility.VisibilityTestUtils;
import dd.kms.zenodot.tests.classesForTest.visibility.VisibilityTestUtils.EntityType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class VisibilityTest
{
	@Parameters(name = "{0} outer class, {1} {2}, minimum access modifier: {3}")
	public static Collection<Object[]> getTestData() {
		return VisibilityTestUtils.getTestData();
	}

	private final AccessModifier	outerClassModifier;
	private final AccessModifier	innerModifier;
	private final EntityType 		innerType;
	private final AccessModifier	minimumAccessModifier;

	public VisibilityTest(AccessModifier outerClassModifier, AccessModifier innerModifier, EntityType innerType, AccessModifier minimumAccessModifier) {
		this.outerClassModifier = outerClassModifier;
		this.innerModifier = innerModifier;
		this.innerType = innerType;
		this.minimumAccessModifier = minimumAccessModifier;
	}

	@Test
	public void testVisibilityQualifiedClasses() {
		testVisibility(true);
	}

	@Test
	public void testVisibilityUnqualifiedClasses() {
		testVisibility(false);
	}

	private void testVisibility(boolean useQualifiedClass) {
		ParserSettingsBuilder builder = ParserSettingsBuilder.create().minimumAccessModifier(minimumAccessModifier);
		if (!useQualifiedClass) {
			builder.importPackagesByName(Arrays.asList(VisibilityTestUtils.PACKAGE));
		}
		ParserSettings settings = builder.build();

		String outerEntityName = VisibilityTestUtils.getOuterEntityName(outerClassModifier);
		String innerEntityName = VisibilityTestUtils.getInnerEntityName(innerModifier, innerType);
		String unqualifiedEntityName = outerEntityName + "." + innerEntityName;
		String entityName = useQualifiedClass ? VisibilityTestUtils.PACKAGE + "." + unqualifiedEntityName : unqualifiedEntityName;
		String expression = innerType == EntityType.METHOD ? entityName + "()" : entityName;

		/*
		 * There seems to be no way to determine the access modifier of a class without
		 * loading it. Since we do not want to load all classes on the class path and
		 * there is not even an official way to determine whether a class has already
		 * been loaded, access modifiers of classes are not checked against the minimum
		 * access modifier.
		 */
		// boolean entityAccessible = outerClassModifier.compareTo(minimumAccessModifier) <= 0 && innerModifier.compareTo(minimumAccessModifier) <= 0;
		boolean entityAccessible = innerType == EntityType.CLASS || innerModifier.compareTo(minimumAccessModifier) <= 0;

		try {
			if (innerType == EntityType.CLASS) {
				Class<?> clazz = Parsers.createClassParser(settings).evaluate(expression);
				Assert.assertTrue("Should not be able to access class '" + entityName + "'", entityAccessible);
			} else {
				Object result = Parsers.createExpressionParser(settings).evaluate(expression, null);
				Assert.assertTrue("Should not be able to access '" + expression + "'", entityAccessible);
				Assert.assertEquals("Unexpected expression result of '" + expression + "'", innerModifier.toString(), result);
			}
		} catch (ParseException e) {
			Assert.assertFalse("Could not access '" + expression + "'", entityAccessible);
		}
	}
}
