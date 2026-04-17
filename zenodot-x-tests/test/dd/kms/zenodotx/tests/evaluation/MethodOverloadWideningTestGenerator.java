package dd.kms.zenodotx.tests.evaluation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class generates the test {@link MethodOverloadWideningTest} and writes it to the console.
 */
public class MethodOverloadWideningTestGenerator
{
	private static final String	TEST_CODE_TEMPLATE_FILE_NAME	= "MethodOverloadWideningTestTemplate.java";
	private static final String	TEST_CLASS_NAME					= "MethodOverloadWideningTest";

	public static void main(String[] args) throws IOException {
		List<String> successfulTestLines = new ArrayList<>();
		List<String> testWithErrorLines = new ArrayList<>();
		List<String> overloadLines = new ArrayList<>();

		List<Type> types = new ArrayList<>();
		types.addAll(Arrays.asList(Type.values()));
		Collections.reverse(types);
		int numTypes = types.size();
		for (int i = 0; i < numTypes - 1; i++) {
			Type type1 = types.get(i);
			String typeName1 = type1.getTypeName();
			for (int j = i + 1; j < numTypes; j++) {
				Type type2 = types.get(j);
				String typeName2 = type2.getTypeName();

				String methodName = "get" + startWithUpperCaseLetter(typeName1) + startWithUpperCaseLetter(typeName2);

				String overloadLine1 = createOverloadLine(methodName, type1);
				String overloadLine2 = createOverloadLine(methodName, type2);

				// Add two overloads with parameters type1 and type2
				overloadLines.add("");
				overloadLines.add(overloadLine1);
				overloadLines.add(overloadLine2);

				// Call these overloads once with every type
				for (Type type : types) {
					if (type.canBeConvertedTo(type1) || type.canBeConvertedTo(type2)) {
						// one of the overloads can be called, no ambiguity
						String testLine = createSuccessfulTestLine(methodName, type);
						successfulTestLines.add(testLine);
					} else {
						// none of the overloads can be called
						String testLine = createTestWithErrorLine(methodName, type);
						testWithErrorLines.add(testLine);
					}
				}
			}
		}

		String successfulTests = String.join("\r\n", successfulTestLines) + ";";
		String testsWithError = String.join("\r\n", testWithErrorLines) + ";";
		String overloads = String.join("\r\n", overloadLines);

		String testCode = createTestCode(successfulTests, testsWithError, overloads);

		System.out.println(testCode);
	}

	private static String startWithUpperCaseLetter(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static String createOverloadLine(String methodName, Type type) {
		String typeName = type.getTypeName();
		String variableName = type.getVariableName();
		return "\t\tprivate " + typeName + " " + methodName + "(" + typeName + " " + variableName + ") { return " + variableName + "; }";
	}

	private static String createSuccessfulTestLine(String methodName, Type type) {
		String variableName = type.getVariableName();
		return "\t\t\t.addTest(\"" + methodName + "(" + variableName + ")\",\ttestInstance." + methodName + "(testInstance." + variableName + "))";
	}

	private static String createTestWithErrorLine(String methodName, Type type) {
		String variableName = type.getVariableName();
		return "\t\t\t.addTestWithError(\"" + methodName + "(" + variableName + ")\",\tSemanticException.class)";
	}

	private static String createTestCode(String successfulTests, String testsWithError, String overloads) throws IOException {
		String testCodeTemplate = loadTestCodeTemplate();
		return testCodeTemplate
			.replace("$TEST_PACKAGE$",   MethodOverloadWideningTestGenerator.class.getPackage().getName())
			.replace("$CLASS_NAME$",		TEST_CLASS_NAME)
			.replace("$SUCCESSFUL_TESTS$",	successfulTests)
			.replace("$TESTS_WITH_ERROR$",	testsWithError)
			.replace("$OVERLOADS$",			overloads)
			.replace("$BOOLEAN_VARIABLE$",	Type.BOOLEAN.getVariableName())
			.replace("$CHAR_VARIABLE$",		Type.CHAR.getVariableName())
			.replace("$BYTE_VARIABLE$",		Type.BYTE.getVariableName())
			.replace("$SHORT_VARIABLE$",	Type.SHORT.getVariableName())
			.replace("$INT_VARIABLE$",		Type.INT.getVariableName())
			.replace("$LONG_VARIABLE$",		Type.LONG.getVariableName())
			.replace("$FLOAT_VARIABLE$",	Type.FLOAT.getVariableName())
			.replace("$DOUBLE_VARIABLE$",	Type.DOUBLE.getVariableName());
	}

	private static String loadTestCodeTemplate() throws IOException {
		URL resourceDirectoryUrl = MethodOverloadWideningTestGenerator.class.getResource("/");
		URI resourceDirectoryUri;
		try {
			resourceDirectoryUri = resourceDirectoryUrl.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Resource URL '" + resourceDirectoryUrl + "' is not valid URI");
		}
		Path resourceDirectory = Paths.get(resourceDirectoryUri);
		Path testCodeTemplateFile = resourceDirectory.resolve(TEST_CODE_TEMPLATE_FILE_NAME);
		return new String(Files.readAllBytes(testCodeTemplateFile));
	}

	private enum Type
	{
		DOUBLE	("double", "d", null),
		FLOAT	("float", "f", DOUBLE),
		LONG	("long", "l", FLOAT),
		INT		("int", "i", LONG),
		SHORT	("short", "s", INT),
		BYTE	("byte", "b", SHORT),
		CHAR	("char", "c", INT),
		BOOLEAN	("boolean", "bool", null);

		private final String	typeName;
		private final String	variableName;
		private final Type		nextWiderType;

		Type(String typeName, String variableName, Type nextWiderType) {
			this.typeName = typeName;
			this.variableName = variableName;
			this.nextWiderType = nextWiderType;
		}

		public String getTypeName() {
			return typeName;
		}

		public String getVariableName() {
			return variableName;
		}

		public Type getNextWiderType() {
			return nextWiderType;
		}

		public boolean canBeConvertedTo(Type other) {
			for (Type t = this; t != null; t = t.getNextWiderType()) {
				if (t == other) {
					return true;
				}
			}
			return false;
		}
	}
}
