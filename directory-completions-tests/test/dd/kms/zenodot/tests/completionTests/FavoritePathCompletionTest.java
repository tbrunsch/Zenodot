package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.DirectoryCompletions;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
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
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class FavoritePathCompletionTest extends CompletionTest
{
	private static final FileSystem	DEFAULT_FILE_SYSTEM_REPLACEMENT	= DirectoryCompletionTestUtils.createFileSystem("path");

	public FavoritePathCompletionTest(TestData testData) {
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
		Path path1 = DEFAULT_FILE_SYSTEM_REPLACEMENT.getPath("/zenodot/");
		Path path2 = DEFAULT_FILE_SYSTEM_REPLACEMENT.getPath("/zenodot/api");
		return new CompletionTestBuilder()
			.configurator(test -> {
				addPathDirectoryStructure(test.getSettingsBuilder());
				test.importClasses("java.nio.file.Path", "java.nio.file.Paths");
				test.createVariable("path1", path1, true);
				test.createVariable("path2", path2, true);
				test.createVariable("fileSystem", DEFAULT_FILE_SYSTEM_REPLACEMENT, true);
			})
			.addTest("Paths.get(\"/x", "xyz/test")
			.addTest("Paths.get(\"/xyz/t", "test")
			.addTest("Paths.get(\"/zenodot/fr", "framework")	// no favorite
			.addTest("Paths.get(\"/zenodot/t", "test/test with spaces")
			.addTest("Paths.get(\"/zenodot/test/t", "test with spaces")

			.addTest("Paths.get(\"/xyz\", \"t", "test")
			.addTest("Paths.get(\"/zenodot\", \"fr", "framework")	// no favorite
			.addTest("Paths.get(\"/zenodot\", \"t", "test/test with spaces")
			.addTest("Paths.get(\"/zenodot\", \"test/t", "test with spaces")

			.addTest("fileSystem.getPath(\"/x", "xyz/test")
			.addTest("fileSystem.getPath(\"/xyz/t", "test")
			.addTest("fileSystem.getPath(\"/zenodot/fr", "framework")	// no favorite
			.addTest("fileSystem.getPath(\"/zenodot/t", "test/test with spaces")
			.addTest("fileSystem.getPath(\"/zenodot/test/t", "test with spaces")

			.addTest("path1.resolve(\"fr", "framework")	// no favorite
			.addTest("path1.resolve(\"t", "test/test with spaces")
			.addTest("path1.resolve(\"test/t", "test with spaces")

			.addTest("path2.resolveSibling(\"fr", "framework")	// no favorite
			.addTest("path2.resolveSibling(\"t", "test/test with spaces")
			.addTest("path2.resolveSibling(\"test/t", "test with spaces")

			.build();
	}

	private static void addPathDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		PathDirectoryStructure pathDirectoryStructure = new TestPathDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		List<String> favoritePaths = ImmutableList.of(
			"/xyz/test",
			"/zenodot/test/test with spaces"
		);
		DirectoryCompletions.create()
			.pathDirectoryStructure(pathDirectoryStructure)
			.completionTargets(DirectoryCompletions.CompletionTarget.PATH_CREATION, DirectoryCompletions.CompletionTarget.PATH_RESOLUTION)
			.favoritePaths(favoritePaths)
			.configure(parserSettingsBuilder);
	}
}
