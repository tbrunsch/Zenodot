package dd.kms.zenodot.settings;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Describes a value that can be referenced in an expression by its name.<br/>
 * <br/>
 * You can specify whether the value should be referenced with a hard or a weak reference
 * by the {@link ParserSettings}. If you decide for a weak reference, then the framework
 * does not prolong the life time of the variable's value to allow for garbage collection.<br/>
 * <br/>
 * A new instance can be created via {@link ParserSettingsUtils#createVariable(String, ObjectInfo, boolean)}.
 */
public interface Variable
{
	String getName();
	ObjectInfo getValue();
	boolean isUseHardReference();
}
