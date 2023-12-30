package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.CustomHierarchyParserExtension;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;
import dd.kms.zenodot.api.settings.extensions.ParserExtensionBuilder;
import dd.kms.zenodot.impl.settings.parsers.AdditionalCustomHierarchyParserSettings;

public class CustomHierarchyParserExtensionImpl implements CustomHierarchyParserExtension
{
	private final ObjectTreeNode	customHierarchyRoot;
	private char					hierarchyBegin		= '{';
	private char					hierarchySeparator	= '#';
	private char					hierarchyEnd		= '}';

	public CustomHierarchyParserExtensionImpl(ObjectTreeNode customHierarchyRoot) {
		this.customHierarchyRoot = customHierarchyRoot;
	}

	@Override
	public CustomHierarchyParserExtension hierarchyCharacters(char hierarchyBegin, char hierarchySeparator, char hierarchyEnd) {
		this.hierarchyBegin = hierarchyBegin;
		this.hierarchySeparator = hierarchySeparator;
		this.hierarchyEnd = hierarchyEnd;
		return this;
	}

	@Override
	public ParserSettingsBuilder configure(ParserSettingsBuilder parserSettingsBuilder) {
		ParserExtension parserExtension = ParserExtensionBuilder.create()
			.addParser(new AdditionalCustomHierarchyParserSettings(customHierarchyRoot, hierarchyBegin, hierarchySeparator, hierarchyEnd))
			.build();
		parserSettingsBuilder.setParserExtension(CustomHierarchyParserExtension.EXTENSION_NAME, parserExtension);
		return parserSettingsBuilder;
	}
}
