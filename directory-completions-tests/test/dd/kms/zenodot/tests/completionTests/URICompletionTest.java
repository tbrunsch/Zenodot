package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.DirectoryCompletions;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.framework.FileSystemUtils;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collection;

@RunWith(Parameterized.class)
public class URICompletionTest extends CompletionTest
{
	private static final FileSystem NONE_DEFAULT_FILE_SYSTEM	= DirectoryCompletionTestUtils.createFileSystem("uri");

	public URICompletionTest(TestData testData) {
		super(testData);
	}

	@BeforeClass
	public static void setupFileSystems() throws IOException {
		FileSystemUtils.setupFileSystem(NONE_DEFAULT_FILE_SYSTEM, DirectoryCompletionTestUtils.ROOT);
	}

	@AfterClass
	public static void closeFileSystems() throws IOException {
		NONE_DEFAULT_FILE_SYSTEM.close();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		// see DirectoryCompletionTestUtils.ROOT for the directory hierarchy for which Zenodot provides completions
		return new CompletionTestBuilder()
			.configurator(test -> {
				test.importClasses("java.net.URI");
				registerURICompletions(test.getSettingsBuilder());
			})
			.addTest("new URI(\"jimfs://uri/", "zenodot")
			.addTest("new URI(\"jimfs://uri/zen", "zenodot")
			.addTest("new URI(\"jimfs://uri/zenodot/fr", "framework")

			.addTest("new URI(\"jimfs\", \"//uri/zenodot/a", "api")
			.addTest("new URI(\"jimfs\", \"//uri/zenodot/api/Ex", "ExpressionParser")

			.addTest("new URI(\"jimfs\", \"uri\", \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", \"uri\", \"/zenodot/fr", "framework")

			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/a", "api")

			.addTest("URI.create(\"jimfs://uri/", "zenodot")
			.addTest("URI.create(\"jimfs://uri/zen", "zenodot")
			.addTest("URI.create(\"jimfs://uri/zenodot/fr", "framework")

			.addInsertionTest("new URI(\"jimfs://uri/zen^/fr",	'^', "new URI(\"jimfs://uri/zenodot/fr")

			.addInsertionTest("new URI(\"jimfs\", \"//uri/z^/a", 				'^', "new URI(\"jimfs\", \"//uri/zenodot/a")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenod^/api/Ex",		'^', "new URI(\"jimfs\", \"//uri/zenodot/api/Ex")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenodot/a^/Ex",		'^', "new URI(\"jimfs\", \"//uri/zenodot/api/Ex")

			.addInsertionTest("new URI(\"jimfs\", \"uri\", \"/ze^/fr",			'^', "new URI(\"jimfs\", \"uri\", \"/zenodot/fr")

			.addInsertionTest("new URI(\"jimfs\", null, \"uri\", -1, \"/z^/a",	'^', "new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/a")

			.addInsertionTest("URI.create(\"jimfs://uri/zen^/fr",				'^', "URI.create(\"jimfs://uri/zenodot/fr")

			.build();
	}

	private static void registerURICompletions(ParserSettingsBuilder parserSettingsBuilder) {
		DirectoryCompletions.create()
			.completionTargets(DirectoryCompletions.CompletionTarget.URI_CREATION)
			.configure(parserSettingsBuilder);
	}
}
