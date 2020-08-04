package dd.kms.zenodot.completionTests;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.wrappers.ClassInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.classesForTest.visibility.VisibilityTestUtils;
import dd.kms.zenodot.classesForTest.visibility.VisibilityTestUtils.EntityType;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class VisibilityTest
{
	@Parameterized.Parameters(name = "{0} outer class, {1} {2}, minimum access level: {3}")
	public static Collection<Object[]> getTestData() {
		return VisibilityTestUtils.getTestData();
	}

	private final AccessModifier outerClassModifier;
	private final AccessModifier innerModifier;
	private final EntityType innerType;
	private final AccessModifier minimumAccessLevel;

	public VisibilityTest(AccessModifier outerClassModifier, AccessModifier innerModifier, EntityType innerType, AccessModifier minimumAccessLevel) {
		this.outerClassModifier = outerClassModifier;
		this.innerModifier = innerModifier;
		this.innerType = innerType;
		this.minimumAccessLevel = minimumAccessLevel;
	}

	@Test
	public void testVisibilityQualifiedClasses() throws ParseException {
		testVisibility(true);
	}

	@Test
	public void testVisibilityUnqualifiedClasses() throws ParseException {
		testVisibility(false);
	}

	private void testVisibility(boolean useQualifiedClass) throws ParseException {
		ParserSettingsBuilder builder = ParserSettingsUtils.createBuilder().minimumAccessLevel(minimumAccessLevel);
		if (!useQualifiedClass) {
			builder.importPackagesByName(Arrays.asList(VisibilityTestUtils.PACKAGE));
		}
		ParserSettings settings = builder.build();

		String outerEntityName = VisibilityTestUtils.getOuterEntityName(outerClassModifier);
		String innerEntityName = VisibilityTestUtils.getInnerEntityName(innerModifier, innerType);
		String unqualifiedEntityName = outerEntityName + "." + innerEntityName;
		String unqualifiedExpression = unqualifiedEntityName.substring(0, unqualifiedEntityName.length() - innerType.getInnerEntityName().length() / 2);

		String expression = useQualifiedClass ? VisibilityTestUtils.PACKAGE + "." + unqualifiedExpression : unqualifiedExpression;
		String completionString = innerType == EntityType.METHOD ? innerEntityName + "()" : innerEntityName;

		/*
		 * There seems to be no way to determine the access modifier of a class without
		 * loading it. Since we do not want to load all classes on the class path and
		 * there is not even an official way to determine whether a class has already
		 * been loaded, access modifiers of classes are not checked against the minimum
		 * access level.
		 */
		// boolean suggestEntity = outerClassModifier.compareTo(minimumAccessLevel) <= 0 && innerModifier.compareTo(minimumAccessLevel) <= 0;
		boolean suggestEntity = innerType == EntityType.CLASS || innerModifier.compareTo(minimumAccessLevel) <= 0;

		List<CodeCompletion> completions = Parsers.createExpressionParser(expression, settings).getCompletions(InfoProvider.NULL_LITERAL, expression.length());
		if (suggestEntity) {
			List<CodeCompletion> sortedCompletions = CompletionTest.getSortedCompletions(completions);
			CodeCompletion firstCompletion = Iterables.getFirst(sortedCompletions, null);
			Assert.assertFalse("Found no completions", firstCompletion == null);
			Assert.assertEquals("Unexpected first completion", completionString, firstCompletion.toString());
			Assert.assertEquals("Unexpected code completion type", getExpectedCompletionType(), firstCompletion.getType());
		} else {
			boolean suggestedEntity = completions.stream().anyMatch(completion -> completion.toString().equals(completionString));
			Assert.assertFalse("The completion '" + completionString + "' has been suggested although it is not visible", suggestedEntity);
		}
	}

	private CodeCompletionType getExpectedCompletionType() {
		switch (innerType) {
			case FIELD:
				return CodeCompletionType.FIELD;
			case METHOD:
				return CodeCompletionType.METHOD;
			case CLASS:
				return CodeCompletionType.CLASS;
			default:
				throw new IllegalArgumentException("Unsupported type: " + innerType);
		}
	}
}