package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.DirectoryCompletions;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.framework.FileSystemUtils;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class FavoriteFileCompletionTest extends CompletionTest
{
	private static final FileSystem DEFAULT_FILE_SYSTEM_REPLACEMENT	= DirectoryCompletionTestUtils.createFileSystem("favfile");

	public FavoriteFileCompletionTest(TestData testData) {
		super(testData);
	}

	@BeforeClass
	public static void setupFileSystems() throws IOException {
		FileSystemUtils.setupFileSystem(DEFAULT_FILE_SYSTEM_REPLACEMENT, DirectoryCompletionTestUtils.ROOT);
	}

	@AfterClass
	public static void closeFileSystems() throws IOException {
		DEFAULT_FILE_SYSTEM_REPLACEMENT.close();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		// see DirectoryCompletionTestUtils.ROOT for the directory hierarchy for which Zenodot provides completions
		File parent = TestFileDirectoryStructure.toFile(DEFAULT_FILE_SYSTEM_REPLACEMENT.getPath("/zenodot/"));
		return new CompletionTestBuilder()
			.configurator(test -> {
				addFileDirectoryStructure(test.getSettingsBuilder());
				test.importClasses("java.io.File");
				test.createVariable("parent", parent, true);
			})
			.addTest("new File(\"/x", "xyz/test")
			.addTest("new File(\"/xyz/t", "test")
			.addTest("new File(\"/zenodot/fr", "framework")	// no favorite
			.addTest("new File(\"/zenodot/t", "test/test with spaces")
			.addTest("new File(\"/zenodot/test/t", "test with spaces")

			.addTest("new File(\"/xyz\", \"t", "test")
			.addTest("new File(\"/zenodot\", \"fr", "framework")	// no favorite
			.addTest("new File(\"/zenodot\", \"t", "test/test with spaces")
			.addTest("new File(\"/zenodot\", \"test/t", "test with spaces")

			.addTest("new File(parent, \"fr", "framework")	// no favorite
			.addTest("new File(parent, \"t", "test/test with spaces")
			.addTest("new File(parent, \"test/t", "test with spaces")

			.build();
	}

	private static void addFileDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		FileDirectoryStructure fileDirectoryStructure = new TestFileDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		List<String> favoritePaths = ImmutableList.of(
			"/xyz/test",
			"/zenodot/test/test with spaces"
		);
		DirectoryCompletions.create()
			.fileDirectoryStructure(fileDirectoryStructure)
			.completionTargets(DirectoryCompletions.CompletionTarget.FILE_CREATION)
			.favoritePaths(favoritePaths)
			.configure(parserSettingsBuilder);
	}
}
