package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.AccessModifier;

import java.lang.reflect.Modifier;
import java.util.function.IntPredicate;

class ModifierFilters
{
	static IntPredicate createMinimumAccessModifierFilter(AccessModifier minimumAccessModifier) {
		switch (minimumAccessModifier) {
			case PUBLIC:
				return modifiers -> Modifier.isPublic(modifiers);
			case PROTECTED:
				return modifiers -> Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
			case PACKAGE_PRIVATE:
				return modifiers -> !Modifier.isPrivate(modifiers);
			case PRIVATE:
				return modifiers -> true;
			default:
				throw new IllegalArgumentException("Unsupported access modifier " + minimumAccessModifier);
		}
	}
}
