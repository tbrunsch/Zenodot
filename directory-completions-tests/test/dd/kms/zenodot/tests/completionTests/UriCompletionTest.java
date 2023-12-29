package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.DirectoryCompletionExtension;
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
public class UriCompletionTest extends CompletionTest
{
	private static FileSystem NONE_DEFAULT_FILE_SYSTEM;

	public UriCompletionTest(TestData testData) {
		super(testData);
	}

	@BeforeClass
	public static void setupFileSystems() throws IOException {
		NONE_DEFAULT_FILE_SYSTEM = DirectoryCompletionTestUtils.createFileSystem("uri");
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
				test.stopAtError();
				test.importClasses("java.net.URI");
				registerUriCompletions(test.getSettingsBuilder());
			})
			.addTest("new URI(\"jimfs://uri/", "zenodot")
			.addTest("new URI(\"jimfs://uri/zen", "zenodot")
			.addTest("new URI(\"jimfs://uri/zenodot/fr", "framework")
			.addTest("new URI(\"jimfs://uri/zenodot/dir", "directory%20with%20spaces")
			.addTest("new URI(\"jimfs://uri/zenodot/directory%20with%20spaces/file", "file%20with%20spaces")

			.addTest("new URI(\"jimfs\", \"//uri/zenodot/a", "api")
			.addTest("new URI(\"jimfs\", \"//uri/zenodot/api/Ex", "ExpressionParser")
			.addTest("new URI(\"jimfs\", \"//uri/zenodot/directory with spaces/f", "file with spaces")

			.addTest("new URI(\"jimfs\", \"uri\", \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", \"uri\", \"/zenodot/fr", "framework")
			.addTest("new URI(\"jimfs\", \"uri\", \"/zenodot/directory with spaces/subdirectory with", "subdirectory with spaces")

			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/a", "api")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/dir", "directory with spaces")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/directory with spaces/subdirectory_", "subdirectory_without_spaces")

			.addTest("URI.create(\"jimfs://uri/", "zenodot")
			.addTest("URI.create(\"jimfs://uri/zen", "zenodot")
			.addTest("URI.create(\"jimfs://uri/zenodot/fr", "framework")
			.addTest("URI.create(\"jimfs://uri/zenodot/direct", "directory%20with%20spaces")
			.addTest("URI.create(\"jimfs://uri/zenodot/directory%20with%20spaces/subdirectory_", "subdirectory_without_spaces")

			.addInsertionTest("new URI(\"jimfs://uri/zen^/fr",					'^', "new URI(\"jimfs://uri/zenodot/fr")
			.addInsertionTest("new URI(\"jimfs://uri/zenodot/fr^)",				'^', "new URI(\"jimfs://uri/zenodot/framework)")
			.addInsertionTest("new URI(\"jimfs://uri/zenodot/dir^ectory/file",	'^', "new URI(\"jimfs://uri/zenodot/directory%20with%20spaces/file")

			.addInsertionTest("new URI(\"jimfs\", \"//uri/z^/a", 					'^', "new URI(\"jimfs\", \"//uri/zenodot/a")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenod^/api/Ex",			'^', "new URI(\"jimfs\", \"//uri/zenodot/api/Ex")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenodot/api/Ex^)",		'^', "new URI(\"jimfs\", \"//uri/zenodot/api/ExpressionParser)")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenodot/a^/Ex",			'^', "new URI(\"jimfs\", \"//uri/zenodot/api/Ex")
			.addInsertionTest("new URI(\"jimfs\", \"//uri/zenodot/dir^ectory/sub",	'^', "new URI(\"jimfs\", \"//uri/zenodot/directory with spaces/sub")

			.addInsertionTest("new URI(\"jimfs\", \"uri\", \"/ze^/fr",				'^', "new URI(\"jimfs\", \"uri\", \"/zenodot/fr")
			.addInsertionTest("new URI(\"jimfs\", \"uri\", \"/zenodot/fr^)",		'^', "new URI(\"jimfs\", \"uri\", \"/zenodot/framework)")
			.addInsertionTest("new URI(\"jimfs\", \"uri\", \"/zenodot/dir^/sub",	'^', "new URI(\"jimfs\", \"uri\", \"/zenodot/directory with spaces/sub")

			.addInsertionTest("new URI(\"jimfs\", null, \"uri\", -1, \"/z^/a",				'^', "new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/a")
			.addInsertionTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/a^)",		'^', "new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/api)")
			.addInsertionTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/direc^t/s",	'^', "new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/directory with spaces/s")

			.addInsertionTest("URI.create(\"jimfs://uri/zen^/fr",					'^', "URI.create(\"jimfs://uri/zenodot/fr")
			.addInsertionTest("URI.create(\"jimfs://uri/zenodot/fr^)",				'^', "URI.create(\"jimfs://uri/zenodot/framework)")
			.addInsertionTest("URI.create(\"jimfs://uri/zenodot/dir^ectory/file",	'^', "URI.create(\"jimfs://uri/zenodot/directory%20with%20spaces/file")

			.build();
	}

	private static void registerUriCompletions(ParserSettingsBuilder parserSettingsBuilder) {
		DirectoryCompletionExtension.create()
			.completionTargets(DirectoryCompletionExtension.CompletionTarget.URI_CREATION)
			.configure(parserSettingsBuilder);
	}
}
