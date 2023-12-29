package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.DirectoryCompletionExtension;
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
			.addTest("Paths.get(\"/zenodot/dir", "directory with spaces")

			.addTest("Paths.get(\"/zenodot\", \"a", "api")
			.addTest("Paths.get(\"/zenodot/api/\", \"Ex", "ExpressionParser")
			.addTest("Paths.get(\"/zenodot/directory with spaces/\", \"fi", "file with spaces")

			.addTest("fileSystem.getPath(\"/zenodot/fr", "framework")
			.addTest("fileSystem.getPath(\"/zenodot/framework/u", "utils")
			.addTest("fileSystem.getPath(\"/zenodot\", \"api/com", "common")
			.addTest("fileSystem.getPath(\"/zenodot/dir", "directory with spaces")
			.addTest("fileSystem.getPath(\"/zenodot/directory with spaces/fi", "file with spaces")
			.addTest("fileSystem.getPath(\"/zenodot\", \"directory with spaces/subdirectory w", "subdirectory with spaces")

			.addTest("path.resolve(\"Ex", "ExpressionParser")
			.addTest("path.resolve(\"common/Fi", "FieldScanner")
			.addTest("path.resolve(\"../dir", "directory with spaces")
			.addTest("path.resolve(\"../directory with spaces/subdirectory ", "subdirectory with spaces")

			.addTest("path.resolveSibling(\"fr", "framework")
			.addTest("path.resolveSibling(\"framework/tok", "tokenizer")
			.addTest("path.resolveSibling(\"di", "directory with spaces")
			.addTest("path.resolveSibling(\"directory with spaces/subdirectory_", "subdirectory_without_spaces")

			.addInsertionTest("Paths.get(\"/z^/framework/tokenizer/",						'^', "Paths.get(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("Paths.get(\"/zenodot/fr^/tokenizer/",						'^', "Paths.get(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("Paths.get(\"/zenodot/framework/tok^/",						'^', "Paths.get(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("Paths.get(\"/zenodot/framework/tok^)",						'^', "Paths.get(\"/zenodot/framework/tokenizer)")
			.addInsertionTest("Paths.get(\"/zenodot/dir^/subdirectory_without_spaces/", 	'^', "Paths.get(\"/zenodot/directory with spaces/subdirectory_without_spaces/")
			.addInsertionTest("Paths.get(\"/zenodot/directory with spaces/subdirectory_^/", '^', "Paths.get(\"/zenodot/directory with spaces/subdirectory_without_spaces/")

			.addInsertionTest("Paths.get(\"/zen^/api/\", \"Ex",												'^', "Paths.get(\"/zenodot/api/\", \"Ex")
			.addInsertionTest("Paths.get(\"/zenodot/api/\", \"co^/FieldScanner", 							'^', "Paths.get(\"/zenodot/api/\", \"common/FieldScanner")
			.addInsertionTest("Paths.get(\"/zenodot/api/\", \"common/FieldSca^", 							'^', "Paths.get(\"/zenodot/api/\", \"common/FieldScanner")
			.addInsertionTest("Paths.get(\"/zenodot/api/\", \"common/Fiel^dSca)", 							'^', "Paths.get(\"/zenodot/api/\", \"common/FieldScanner)")
			.addInsertionTest("Paths.get(\"/z^/directory with spaces/\", \"subdirectory_without_spaces",	'^', "Paths.get(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")
			.addInsertionTest("Paths.get(\"/zenodot/direct^/\", \"subdirectory_without_spaces",				'^', "Paths.get(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")
			.addInsertionTest("Paths.get(\"/zenodot/directory with spaces/\", \"subdirectory_^",			'^', "Paths.get(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")

			.addInsertionTest("fileSystem.getPath(\"/zeno^/fr",					'^', "fileSystem.getPath(\"/zenodot/fr")
			.addInsertionTest("fileSystem.getPath(\"/ze^/framework/u",			'^', "fileSystem.getPath(\"/zenodot/framework/u")
			.addInsertionTest("fileSystem.getPath(\"/zenodot/frame^/u", 		'^', "fileSystem.getPath(\"/zenodot/framework/u")
			.addInsertionTest("fileSystem.getPath(\"/zenodot/framework/u^)", 	'^', "fileSystem.getPath(\"/zenodot/framework/utils)")
			.addInsertionTest("fileSystem.getPath(\"/zenodot/dir^/f", 			'^', "fileSystem.getPath(\"/zenodot/directory with spaces/f")

			.addInsertionTest("fileSystem.getPath(\"/zenodot\", \"a^/com",		'^', "fileSystem.getPath(\"/zenodot\", \"api/com")
			.addInsertionTest("fileSystem.getPath(\"/zenodot\", \"api/com^)",	'^', "fileSystem.getPath(\"/zenodot\", \"api/common)")
			.addInsertionTest("fileSystem.getPath(\"/zenodot\", \"dir^/f",		'^', "fileSystem.getPath(\"/zenodot\", \"directory with spaces/f")

			.addInsertionTest("path.resolve(\"com^/Fi",						'^', "path.resolve(\"common/Fi")
			.addInsertionTest("path.resolve(\"common/Fi^)",					'^', "path.resolve(\"common/FieldScanner)")
			.addInsertionTest("path.resolve(\"../dir^ect/sub",				'^', "path.resolve(\"../directory with spaces/sub")

			.addInsertionTest("path.resolveSibling(\"fra^/tok",				'^', "path.resolveSibling(\"framework/tok")
			.addInsertionTest("path.resolveSibling(\"framework/tok^)",		'^', "path.resolveSibling(\"framework/tokenizer)")
			.addInsertionTest("path.resolveSibling(\"dir^/fi",				'^', "path.resolveSibling(\"directory with spaces/fi")

			.build();
	}

	private static void addPathDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		PathDirectoryStructure pathDirectoryStructure = new TestPathDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		DirectoryCompletionExtension.create()
			.pathDirectoryStructure(pathDirectoryStructure)
			.completionTargets(DirectoryCompletionExtension.CompletionTarget.PATH_CREATION, DirectoryCompletionExtension.CompletionTarget.PATH_RESOLUTION)
			.configure(parserSettingsBuilder);
	}
}
