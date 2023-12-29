package dd.kms.zenodot.api.settings.extensions;

import dd.kms.zenodot.framework.parsers.AbstractParser;

public interface AdditionalParserSettings
{
	ParserType getParserType();
	Class<? extends AbstractParser<?, ?, ?>> getParserClass();
	Object getSettings();
}
