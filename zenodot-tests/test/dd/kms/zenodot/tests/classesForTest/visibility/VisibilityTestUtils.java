package dd.kms.zenodot.tests.classesForTest.visibility;

import com.google.common.base.CaseFormat;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.impl.utils.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VisibilityTestUtils
{
	public static final String	PACKAGE	= ClassUtils.getParentPath(PublicOuterClass.class.getName());

	private static String getModifierName(AccessModifier modifier, VisibilityTestUtils.EntityType type) {
		String modifierLowerUnderscore = modifier.toString().replace(" ", "_");
		CaseFormat caseFormat = type.getCaseFormat();
		return CaseFormat.LOWER_UNDERSCORE.to(caseFormat, modifierLowerUnderscore);
	}

	public static String getOuterEntityName(AccessModifier modifier) {
		return getModifierName(modifier, VisibilityTestUtils.EntityType.CLASS) + "OuterClass";
	}

	public static String getInnerEntityName(AccessModifier modifier, VisibilityTestUtils.EntityType type) {
		String modifierName = getModifierName(modifier, type);
		String innerEntityNameWithoutModifier = type.getInnerEntityName();
		return modifierName + innerEntityNameWithoutModifier;
	}

	public static Collection<Object[]> getTestData() {
		List<Object[]> testData = new ArrayList<>();
		for (AccessModifier outerClassModifier : Arrays.asList(AccessModifier.PUBLIC, AccessModifier.PACKAGE_PRIVATE)) {
			for (AccessModifier innerModifier : AccessModifier.values()) {
				for (EntityType innerType : EntityType.values()) {
					for (AccessModifier minimumAccessLevel : AccessModifier.values()) {
						Object[] parameters = { outerClassModifier, innerModifier, innerType, minimumAccessLevel };
						testData.add(parameters);
					}
				}
			}
		}
		return testData;
	}

	public enum EntityType
	{
		FIELD	(CaseFormat.LOWER_CAMEL, "Field"),
		METHOD	(CaseFormat.LOWER_CAMEL, "Method"),
		CLASS	(CaseFormat.UPPER_CAMEL, "InnerClass");

		private final CaseFormat	caseFormat;
		private final String		innerEntityName;

		EntityType(CaseFormat caseFormat, String innerEntityName) {
			this.caseFormat = caseFormat;
			this.innerEntityName = innerEntityName;
		}

		CaseFormat getCaseFormat() {
			return caseFormat;
		}

		public String getInnerEntityName() {
			return innerEntityName;
		}
	}
}
