package dd.kms.zenodot.tests.completionTests;

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

			.addTest("new File(\"/zenodot\", \"a", "api")
			.addTest("new File(\"/zenodot/api/\", \"Ex", "ExpressionParser")

			.addTest("new File(parent, \"Ex", "ExpressionParser")
			.addTest("new File(parent, \"common/Fi", "FieldScanner")

			.addInsertionTest("new File(\"/z^/framework/tokenizer/",	'^', "new File(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("new File(\"/zenodot/fr^/tokenizer/",		'^', "new File(\"/zenodot/framework/tokenizer/")
			.addInsertionTest("new File(\"/zenodot/framework/tok^/", 	'^', "new File(\"/zenodot/framework/tokenizer/")

			.addInsertionTest("new File(\"/zen^/api/\", \"Ex", 			'^', "new File(\"/zenodot/api/\", \"Ex")

			.addInsertionTest("new File(parent, \"com^/Fi", 			'^', "new File(parent, \"common/Fi")

			.build();
	}

	private static void addFileDirectoryStructure(ParserSettingsBuilder parserSettingsBuilder) {
		FileDirectoryStructure fileDirectoryStructure = new TestFileDirectoryStructure(DEFAULT_FILE_SYSTEM_REPLACEMENT);
		DirectoryCompletions.create()
			.fileDirectoryStructure(fileDirectoryStructure)
			.completionTargets(DirectoryCompletions.CompletionTarget.FILE_CREATION)
			.configure(parserSettingsBuilder);
	}
}
