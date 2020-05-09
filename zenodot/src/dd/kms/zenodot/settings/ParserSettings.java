package dd.kms.zenodot.settings;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.debug.ParserLogger;

import java.util.List;

/**
 * Immutable settings for the parsing process. Can only be created with a {@link ParserSettingsBuilder}.<br/>
 * <br/>
 * You can either create a new builder or derive one from existing settings via {@link ParserSettings#builder()}.
 */
public interface ParserSettings
{
	Imports getImports();
	List<Variable> getVariables();
	AccessModifier getMinimumAccessLevel();
	boolean isEnableDynamicTyping();
	boolean isConsiderAllClassesForClassCompletions();
	ObjectTreeNode getCustomHierarchyRoot();
	ParserLogger getLogger();
	ParserSettingsBuilder builder();
}
