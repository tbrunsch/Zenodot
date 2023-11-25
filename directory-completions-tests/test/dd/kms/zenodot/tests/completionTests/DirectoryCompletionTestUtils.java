package dd.kms.zenodot.tests.completionTests;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dd.kms.zenodot.framework.DirectoryDescription;

import java.nio.file.FileSystem;

import static dd.kms.zenodot.framework.FileSystemUtils.dir;
import static dd.kms.zenodot.framework.FileSystemUtils.file;

class DirectoryCompletionTestUtils
{
	static final DirectoryDescription ROOT = dir(null,
		dir("zenodot",
			dir("api",
				dir("common",
					file("FieldScanner"),
					file("RegexUtils")
				),
				file("ExpressionParser")
			),
			dir("framework",
				dir("tokenizer",
					file("TokenStream")
				),
				dir("utils",
					file("ParseUtils")
				)
			),
			dir("directory with spaces",
				dir("subdirectory with spaces"),
				dir("subdirectory_without_spaces"),
				file("file with spaces")
			)
		)
	);

	static FileSystem createFileSystem(String name) {
		Configuration configuration = Configuration.unix().toBuilder()
			.setWorkingDirectory("/")	// prevents Jimfs from creating the directory "/work"
			.build();
		return Jimfs.newFileSystem(name, configuration);
	}
}
