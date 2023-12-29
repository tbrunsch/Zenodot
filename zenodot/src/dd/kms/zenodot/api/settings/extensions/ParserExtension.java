package dd.kms.zenodot.api.settings.extensions;

import java.lang.reflect.Executable;
import java.util.List;

public interface ParserExtension
{
	List<AdditionalParserSettings> getParsers();
	List<CompletionProvider> getStringLiteralCompletionProviders(Executable executable, int parameterIndex);
}
