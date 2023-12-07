package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.ImmutableMap;
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
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;

@RunWith(Parameterized.class)
public class JarURICompletionTest extends CompletionTest
{
	private static FileSystem NONE_DEFAULT_FILE_SYSTEM;

	public JarURICompletionTest(TestData testData) {
		super(testData);
	}

	@BeforeClass
	public static void setupFileSystems() throws IOException {
		NONE_DEFAULT_FILE_SYSTEM = DirectoryCompletionTestUtils.createFileSystem("jaruri");
		Path archiveFile = NONE_DEFAULT_FILE_SYSTEM.getPath("/").resolve("archive.jar");
		try (OutputStream os = Files.newOutputStream(archiveFile, StandardOpenOption.CREATE);
			ZipOutputStream zos = new ZipOutputStream(os)) {
			zos.close();
			URI archiveRootURI = URI.create("jar:jimfs://jaruri/archive.jar!/");
			try (FileSystem jarFileSystem = FileSystems.newFileSystem(archiveRootURI, ImmutableMap.of("create", "true"))) {
				FileSystemUtils.setupFileSystem(jarFileSystem, DirectoryCompletionTestUtils.ROOT);
			}
		}
	}

	@AfterClass
	public static void closeFileSystems() throws IOException {
		NONE_DEFAULT_FILE_SYSTEM.close();
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		// see DirectoryCompletionTestUtils.ROOT for the directory hierarchy for which Zenodot provides completions
		URI uri = URI.create("jar:jimfs://jaruri/archive.jar!/zenodot");
		System.out.println(uri.getSchemeSpecificPart());
		return new CompletionTestBuilder()
			.configurator(test -> {
				test.stopAtError();
				test.importClasses("java.net.URI");
				registerURICompletions(test.getSettingsBuilder());
			})
			.addTest("new URI(\"jar:jimfs://jaruri/archive.jar!/", "zenodot")
			.addTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zen", "zenodot")
			.addTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/fr", "framework")
			.addTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/dir", "directory%20with%20spaces")
			.addTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/directory%20with%20spaces/file", "file%20with%20spaces")

			.addTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/a", "api")
			.addTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/api/Ex", "ExpressionParser")
			.addTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/directory with spaces/f", "file with spaces")

			.addTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/", "zenodot")
			.addTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zen", "zenodot")
			.addTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/fr", "framework")
			.addTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/direct", "directory%20with%20spaces")
			.addTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/directory%20with%20spaces/subdirectory_", "subdirectory_without_spaces")

			.addInsertionTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zen^/fr",					'^', "new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/fr")
			.addInsertionTest("new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/dir^ectory/file",	'^', "new URI(\"jar:jimfs://jaruri/archive.jar!/zenodot/directory%20with%20spaces/file")

			.addInsertionTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/z^/a", 					'^', "new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/a")
			.addInsertionTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenod^/api/Ex",			'^', "new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/api/Ex")
			.addInsertionTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/a^/Ex",			'^', "new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/api/Ex")
			.addInsertionTest("new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/dir^ectory/sub",	'^', "new URI(\"jar\", \"jimfs://jaruri/archive.jar!/zenodot/directory with spaces/sub")

			.addInsertionTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zen^/fr",					'^', "URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/fr")
			.addInsertionTest("URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/dir^ectory/file",	'^', "URI.create(\"jar:jimfs://jaruri/archive.jar!/zenodot/directory%20with%20spaces/file")

			.build();
	}

	private static void registerURICompletions(ParserSettingsBuilder parserSettingsBuilder) {
		DirectoryCompletions.create()
			.completionTargets(DirectoryCompletions.CompletionTarget.URI_CREATION)
			.configure(parserSettingsBuilder);
	}
}
