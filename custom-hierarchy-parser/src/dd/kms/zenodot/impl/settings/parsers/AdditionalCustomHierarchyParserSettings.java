package dd.kms.zenodot.impl.settings.parsers;

import dd.kms.zenodot.api.settings.CustomHierarchyParserSettings;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.parsers.ParserType;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.impl.parsers.CustomHierarchyParser;

public class AdditionalCustomHierarchyParserSettings implements AdditionalParserSettings
{
	private final CustomHierarchyParserSettings	settings;

	public AdditionalCustomHierarchyParserSettings(ObjectTreeNode customHierarchyRoot) {
		this.settings = new dd.kms.zenodot.impl.settings.CustomHierarchyParserSettingsImpl(customHierarchyRoot);
	}

	@Override
	public ParserType getParserType() {
		return ParserType.ROOT_OBJECT_PARSER;
	}

	@Override
	public Class<? extends AbstractParser<?, ?, ?>> getParserClass() {
		return CustomHierarchyParser.class;
	}

	@Override
	public CustomHierarchyParserSettings getSettings() {
		return settings;
	}
}
