package dd.kms.zenodot.impl.settings.parsers;

import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.extensions.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.extensions.ParserType;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.impl.parsers.CustomHierarchyParser;
import dd.kms.zenodot.impl.settings.CustomHierarchyParserSettings;

public class AdditionalCustomHierarchyParserSettings implements AdditionalParserSettings
{
	private final CustomHierarchyParserSettings	settings;

	public AdditionalCustomHierarchyParserSettings(ObjectTreeNode customHierarchyRoot, char hierarchyBegin, char hierarchySeparator, char hierarchyEnd) {
		this.settings = new CustomHierarchyParserSettings(customHierarchyRoot, hierarchyBegin, hierarchySeparator, hierarchyEnd);
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
