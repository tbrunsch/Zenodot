package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<field>},</li>
 *     <li>{@code <context class>.<field>}, or</li>
 *     <li>{@code <field>} (like {@code <context instance>.<field>} for {@code <context instance> = this})</li>
 * </ul>
 */
class JavaFieldParser extends AbstractJavaEntityParser
{
    private final boolean staticOnly;

    JavaFieldParser(JavaParserPool parserSettings, ObjectInfo thisInfo, boolean staticOnly) {
        super(parserSettings, thisInfo);
        this.staticOnly = staticOnly;
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Class<?> expectedResultClass) {
        int startPosition = tokenStream.getPosition();
        JavaToken fieldNameToken;
        try {
            fieldNameToken = tokenStream.readIdentifier();
        } catch (JavaTokenStream.JavaTokenParseException e) {
            return new ParseError(startPosition, "Expected an identifier");
        }
        String fieldName = fieldNameToken.getValue();
        int endPosition = tokenStream.getPosition();

        List<Field> fields = parserPool.getInspectionDataProvider().getFields(getClass(currentContextInfo), staticOnly);

        // check for code completion
        if (fieldNameToken.isContainsCaret()) {
            Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, startPosition, endPosition),
				ParseUtils.rateFieldByNameAndClassFunc(fieldName, expectedResultClass)
			);
            return new CompletionSuggestions(ratedSuggestions);
        }

        // no code completion requested => field name must exist
        Optional<Field> firstFieldMatch = fields.stream().filter(field -> field.getName().equals(fieldName)).findFirst();
        if (!firstFieldMatch.isPresent()) {
            return new ParseError(startPosition, "Unknown field '" + fieldName + "'");
        }

        Field matchingField = firstFieldMatch.get();
        ObjectInfo matchingFieldInfo = getFieldInfo(currentContextInfo, matchingField);

        return parserPool.getObjectTailParser().parse(tokenStream, matchingFieldInfo, expectedResultClass);
    }
}
