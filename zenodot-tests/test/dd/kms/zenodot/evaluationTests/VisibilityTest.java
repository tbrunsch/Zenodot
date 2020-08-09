package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.wrappers.ClassInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.classesForTest.visibility.VisibilityTestUtils;
import dd.kms.zenodot.classesForTest.visibility.VisibilityTestUtils.EntityType;
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
	@Parameters(name = "{0} outer class, {1} {2}, minimum access level: {3}")
	public static Collection<Object[]> getTestData() {
		return VisibilityTestUtils.getTestData();
	}

	private final AccessModifier	outerClassModifier;
	private final AccessModifier	innerModifier;
	private final EntityType 		innerType;
	private final AccessModifier	minimumAccessLevel;

	public VisibilityTest(AccessModifier outerClassModifier, AccessModifier innerModifier, EntityType innerType, AccessModifier minimumAccessLevel) {
		this.outerClassModifier = outerClassModifier;
		this.innerModifier = innerModifier;
		this.innerType = innerType;
		this.minimumAccessLevel = minimumAccessLevel;
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
		ParserSettingsBuilder builder = ParserSettingsUtils.createBuilder().minimumAccessLevel(minimumAccessLevel);
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
		 * access level.
		 */
		// boolean entityAccessible = outerClassModifier.compareTo(minimumAccessLevel) <= 0 && innerModifier.compareTo(minimumAccessLevel) <= 0;
		boolean entityAccessible = innerType == EntityType.CLASS || innerModifier.compareTo(minimumAccessLevel) <= 0;

		try {
			if (innerType == EntityType.CLASS) {
				ClassInfo classInfo = Parsers.createClassParser(settings).evaluate(expression);
				Assert.assertTrue("Should not be able to access class '" + entityName + "'", entityAccessible);
			} else {
				ObjectInfo result = Parsers.createExpressionParser(settings).evaluate(expression, InfoProvider.NULL_LITERAL);
				Assert.assertTrue("Should not be able to access '" + expression + "'", entityAccessible);
				Assert.assertEquals("Unexpected expression result of '" + expression + "'", innerModifier.toString(), result.getObject());
			}
		} catch (ParseException e) {
			Assert.assertFalse("Could not access '" + expression + "'", entityAccessible);
		}
	}
}
