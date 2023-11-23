package dd.kms.zenodot.tests.completionTests;

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

@RunWith(Parameterized.class)
public class PathCompletionTest extends CompletionTest
{
	private static final FileSystem	DEFAULT_FILE_SYSTEM_REPLACEMENT	= DirectoryCompletionTestUtils.createFileSystem("path1");
	private static final FileSystem	NONE_DEFAULT_FILE_SYSTEM		= DirectoryCompletionTestUtils.createFileSystem("path2");

	public PathCompletionTest(TestData testData) {
		super(testData);
	}

	@BeforeClass
	public static void setupFileSystems() throws IOException {
		FileSystemUtils.setupFileSystem(DEFAULT_FILE_SYSTEM_REPLACEMENT, DirectoryCompletionTestUtils.ROOT);
		FileSystemUtils.setupFileSystem(NONE_DEFAULT_FILE_SYSTEM, DirectoryCompletionTestUtils.ROOT);
	}

	@AfterClass
	public static void closeFileSystems() throws IOException {
		DEFAULT_FILE_SYSTEM_REPLACEMENT.close();
		NONE_DEFAULT_FILE_SYSTEM.close();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		// see DirectoryCompletionTestUtils.ROOT for the directory hierarchy for which Zenodot provides completions
		Path path = NONE_DEFAULT_FILE_SYSTEM.getPath("/zenodot/api/");
		return new CompletionTestBuilder()
			.configurator(test -> {
				addPathDirectoryStructure(test.getSettingsBuilder());
				test.importClasses("java.nio.file.Path", "java.nio.file.Paths");
				test.createVariable("path", path, true);
				test.createVariable("fileSystem", NONE_DEFAULT_FILE_SYSTEM, true);
			})
			.addTest("Paths.get(\"/zen", "zenodot")
			.addTest("Paths.get(\"/zenodot/framework/tokenizer/", "TokenStream")
			.addTest("Paths.get(\"/zenodot/framework", "framework")

			.addTest("Paths.get(\"/zenodot\", \"a", "api")
			.addTest("Paths.get(\"/zenodot/api/\", \"Ex", "ExpressionParser")

			.addTest("fileSystem.getPath(\"/zenodot/fr", "framework")
			.addTest("fileSystem.getPath(\"/zenodot/framework/u", "utils")
			.addTest("fileSystem.getPath(\"/zenodot\", \"api/com", "common")

			.addTest("path.resolve(\"Ex", "ExpressionParser")
			.addTest("path.resolve(\"common/Fi", "FieldScanner")

			.addTest("path.resolveSibling(\"fr", "framework")
			.addTest("path.resolveSibling(\"framework/tok", "tokenizer")

			.addInsertionTest("Paths.get(\"/z^/framework/tokenizer/",		'^', "Paths.get(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("Paths.get(\"/zenodot/fr^/tokenizer/",		'^', "Paths.get(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("Paths.get(\"/zenodot/framework/tok^/",		'^', "Paths.get(\"/zenodot/framework/tokenizer/")

			.addInsertionTest("Paths.get(\"/zen^/api/\", \"Ex",				'^', "Paths.get(\"/zenodot/api/\", \"Ex")

			.addInsertionTest("fileSystem.getPath(\"/zeno^/fr",				'^', "fileSystem.getPath(\"/zenodot/fr")
			.addInsertionTest("fileSystem.getPath(\"/ze^/framework/u",		'^', "fileSystem.getPath(\"/zenodot/framework/u")
			.addInsertionTest("fileSystem.getPath(\"/zenodot/frame^/u", 	'^', "fileSystem.getPath(\"/zenodot/framework/u")

			.addInsertionTest("fileSystem.getPath(\"/zenodot\", \"a^/com",	'^', "fileSystem.getPath(\"/zenodot\", \"api/com")

			.addInsertionTest("path.resolve(\"com^/Fi",						'^', "path.resolve(\"common/Fi")

			.addInsertionTest("path.resolveSibling(\"fra^/tok",				'^', "path.resolveSibling(\"framework/tok")

			.build();
	}

	private static void addPathDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		PathDirectoryStructure pathDirectoryStructure = new TestPathDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		DirectoryCompletions.create()
			.pathDirectoryStructure(pathDirectoryStructure)
			.completionTargets(DirectoryCompletions.CompletionTarget.PATH_CREATION, DirectoryCompletions.CompletionTarget.PATH_RESOLUTION)
			.configure(parserSettingsBuilder);
	}
}
