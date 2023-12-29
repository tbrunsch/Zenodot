package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.DirectoryCompletionExtension;
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

@RunWith(Parameterized.class)
public class FileCompletionTest extends CompletionTest
{
	private static final FileSystem DEFAULT_FILE_SYSTEM_REPLACEMENT	= DirectoryCompletionTestUtils.createFileSystem("file");

	public FileCompletionTest(TestData testData) {
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
		File parent = TestFileDirectoryStructure.toFile(DEFAULT_FILE_SYSTEM_REPLACEMENT.getPath("/zenodot/api/"));
		return new CompletionTestBuilder()
			.configurator(test -> {
				addFileDirectoryStructure(test.getSettingsBuilder());
				test.importClasses("java.io.File");
				test.createVariable("parent", parent, true);
			})
			.addTest("new File(\"/zen", "zenodot")
			.addTest("new File(\"/zenodot/framework/tokenizer/", "TokenStream")
			.addTest("new File(\"/zenodot/framework", "framework")
			.addTest("new File(\"/zenodot/dir", "directory with spaces")

			.addTest("new File(\"/zenodot\", \"a", "api")
			.addTest("new File(\"/zenodot/api/\", \"Ex", "ExpressionParser")
			.addTest("new File(\"/zenodot/directory with spaces/\", \"fi", "file with spaces")

			.addTest("new File(parent, \"Ex", "ExpressionParser")
			.addTest("new File(parent, \"common/Fi", "FieldScanner")
			.addTest("new File(parent, \"../dir", "directory with spaces")
			.addTest("new File(parent, \"../directory with spaces/subdirectory ", "subdirectory with spaces")

			.addInsertionTest("new File(\"/z^/framework/tokenizer/",						'^', "new File(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("new File(\"/zenodot/fr^/tokenizer/",							'^', "new File(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("new File(\"/zenodot/framework/tok^/", 						'^', "new File(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("new File(\"/zenodot/framework/tok^)", 						'^', "new File(\"/zenodot/framework/tokenizer)")
			.addInsertionTest("new File(\"/zenodot/dir^/subdirectory_without_spaces/", 		'^', "new File(\"/zenodot/directory with spaces/subdirectory_without_spaces/")
			.addInsertionTest("new File(\"/zenodot/directory with spaces/subdirectory_^/", 	'^', "new File(\"/zenodot/directory with spaces/subdirectory_without_spaces/")

			.addInsertionTest("new File(\"/zen^/api/\", \"Ex", 											'^', "new File(\"/zenodot/api/\", \"Ex")
			.addInsertionTest("new File(\"/zenodot/api/\", \"co^/FieldScanner", 						'^', "new File(\"/zenodot/api/\", \"common/FieldScanner")
			.addInsertionTest("new File(\"/zenodot/api/\", \"common/FieldSca^", 						'^', "new File(\"/zenodot/api/\", \"common/FieldScanner")
			.addInsertionTest("new File(\"/zenodot/api/\", \"common/Fiel^dSca)", 						'^', "new File(\"/zenodot/api/\", \"common/FieldScanner)")
			.addInsertionTest("new File(\"/z^/directory with spaces/\", \"subdirectory_without_spaces",	'^', "new File(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")
			.addInsertionTest("new File(\"/zenodot/direct^/\", \"subdirectory_without_spaces",			'^', "new File(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")
			.addInsertionTest("new File(\"/zenodot/directory with spaces/\", \"subdirectory_^",			'^', "new File(\"/zenodot/directory with spaces/\", \"subdirectory_without_spaces")

			.addInsertionTest("new File(parent, \"com^/Fi", 							'^', "new File(parent, \"common/Fi")
			.addInsertionTest("new File(parent, \"common/Fi^)", 						'^', "new File(parent, \"common/FieldScanner)")
			.addInsertionTest("new File(parent, \"../directory wi^/file with spaces", 	'^', "new File(parent, \"../directory with spaces/file with spaces")
			.addInsertionTest("new File(parent, \"../directory with spaces/file w^", 	'^', "new File(parent, \"../directory with spaces/file with spaces")

			.build();
	}

	private static void addFileDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		FileDirectoryStructure fileDirectoryStructure = new TestFileDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		DirectoryCompletionExtension.create()
			.fileDirectoryStructure(fileDirectoryStructure)
			.completionTargets(DirectoryCompletionExtension.CompletionTarget.FILE_CREATION)
			.configure(parserSettingsBuilder);
	}
}
