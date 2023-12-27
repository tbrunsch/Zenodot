package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.ImmutableList;
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
import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class FavoriteUriCompletionTest extends CompletionTest
{
	private static FileSystem NONE_DEFAULT_FILE_SYSTEM;

	public FavoriteUriCompletionTest(TestData testData) {
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
			.addTest("new URI(\"h", "http://github.com")
			.addTest("new URI(\"http:", "http://github.com")
			.addTest("new URI(\"jimfs://x", "xyz/test")
			.addTest("new URI(\"jimfs://xyz/", "test")
			.addTest("new URI(\"jimfs://uri/zen", "zenodot")
			.addTest("new URI(\"jimfs://uri/zenodot/fr", "framework")					// no favorite
			.addTest("new URI(\"jimfs://uri/zenodot/te", "test/test%20with%20spaces")

			.addTest("new URI(\"http\", \"", "//github.com")
			.addTest("new URI(\"jimfs\", \"//x", "xyz/test")
			.addTest("new URI(\"jimfs\", \"//xyz/", "test")
			.addTest("new URI(\"jimfs\", \"//uri/zen", "zenodot")
			.addTest("new URI(\"jimfs\", \"//uri/zenodot/fr", "framework")	// no favorite
			.addTest("new URI(\"jimfs\", \"//uri/zenodot/te", "test/test with spaces")

			.addTest("new URI(\"jimfs\", \"xyz\", \"/t", "test")
			.addTest("new URI(\"jimfs\", \"uri\", \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", \"uri\", \"/zenodot/fr", "framework")	// no favorite
			.addTest("new URI(\"jimfs\", \"uri\", \"/zenodot/te", "test/test with spaces")

			.addTest("new URI(\"jimfs\", null, \"xyz\", -1, \"/t", "test")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zen", "zenodot")
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/fr", "framework")	// no favorite
			.addTest("new URI(\"jimfs\", null, \"uri\", -1, \"/zenodot/te", "test/test with spaces")

			.addTest("URI.create(\"h", "http://github.com")
			.addTest("URI.create(\"http:", "http://github.com")
			.addTest("URI.create(\"jimfs://x", "xyz/test")
			.addTest("URI.create(\"jimfs://xyz/", "test")
			.addTest("URI.create(\"jimfs://uri/zen", "zenodot")
			.addTest("URI.create(\"jimfs://uri/zenodot/fr", "framework")	// no favorite
			.addTest("URI.create(\"jimfs://uri/zenodot/te", "test/test%20with%20spaces")

			.build();
	}

	private static void registerUriCompletions(ParserSettingsBuilder parserSettingsBuilder) {
		List<URI> favoriteUris = ImmutableList.of(
			URI.create("http://github.com"),
			URI.create("jimfs://xyz/test"),
			URI.create("jimfs://uri/zenodot/test/test%20with%20spaces")
		);
		DirectoryCompletions.create()
			.completionTargets(DirectoryCompletions.CompletionTarget.URI_CREATION)
			.favoriteUris(favoriteUris)
			.configure(parserSettingsBuilder);
	}
}
