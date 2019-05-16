package dd.kms.zenodot.common;

import java.lang.reflect.Modifier;
import java.util.function.IntPredicate;

class ModifierFilters
{
	static IntPredicate createMinimumAccessLevelFilter(AccessModifier minimumAccessLevel) {
		switch (minimumAccessLevel) {
			case PUBLIC:
				return modifiers -> Modifier.isPublic(modifiers);
			case PROTECTED:
				return modifiers -> Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
			case PACKAGE_PRIVATE:
				return modifiers -> !Modifier.isPrivate(modifiers);
			case PRIVATE:
				return modifiers -> true;
			default:
				throw new IllegalArgumentException("Unsupported access level " + minimumAccessLevel);
		}
	}
}
