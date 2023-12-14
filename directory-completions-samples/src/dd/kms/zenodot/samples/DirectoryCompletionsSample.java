package dd.kms.zenodot.samples;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dd.kms.zenodot.api.DirectoryCompletions;
import dd.kms.zenodot.api.DirectoryCompletions.CompletionTarget;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.framework.DirectoryDescription;
import dd.kms.zenodot.framework.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dd.kms.zenodot.framework.FileSystemUtils.dir;
import static dd.kms.zenodot.framework.FileSystemUtils.file;

/**
 * This sample demonstrates how to parse a {@link java.nio.file.Path} or {@link java.net.URI} with Zenodot.
 */
public class DirectoryCompletionsSample
{
	private static final DirectoryDescription ROOT	= dir(null,
		dir("zenodot",
			dir("samples",
				file("completion_sample")),
			dir("other",
				dir("directory with spaces"),
				file("file with spaces")
			),
			file("some_file"),
			file("another_file")
		)
	);

	public static void main(String[] args) throws IOException {
		try (FileSystem fs = setupFileSystem()){
			ParserSettingsBuilder parserSettingsBuilder = ParserSettingsBuilder.create();
			DirectoryCompletions.create()
				.completionTargets(
					CompletionTarget.FILE_CREATION,
					CompletionTarget.PATH_CREATION,
					CompletionTarget.PATH_RESOLUTION,
					CompletionTarget.URI_CREATION)
				.configure(parserSettingsBuilder);
			ParserSettings settings = parserSettingsBuilder
				.importClasses(Arrays.asList(File.class, Path.class, Paths.class, URI.class))
				.build();
			ExpressionParser parser = Parsers.createExpressionParser(settings);

			/*
			 * On most Windows machines, the completion "Program Files" will be printed
			 * for the following partial expressions:
			 */
			printBestCompletion(parser, "new File(\"C:\\\\Program Fi", null);
			printBestCompletion(parser, "new File(\"C:\", \"Program Fi", null);
			printBestCompletion(parser, "new File(this, \"Program Fi", "C:");
			printBestCompletion(parser, "new File(this, \"Program Fi", new File("C:"));
			printBestCompletion(parser, "Paths.get(\"C:\\\\Program Fi", null);
			printBestCompletion(parser, "Paths.get(\"C:\", \"Program Fi", null);
			printBestCompletion(parser, "Paths.get(this, \"Program Fi", "C:");
			printBestCompletion(parser, "this.getPath(\"C:\\\\Program Fi", FileSystems.getDefault());
			printBestCompletion(parser, "this.getPath(\"C:\", \"Program Fi", FileSystems.getDefault());

			System.out.println();

			/*
			 * The following examples are machine independent because they reference
			 * files and directories on Jimfs. The structure is defined by ROOT.
			 */
			printBestCompletion(parser, "this.getPath(\"/zen", fs);						// prints "zenodot"
			printBestCompletion(parser, "this.getPath(\"/zenodot/other/dir", fs);		// prints "directory with spaces"
			printBestCompletion(parser, "this.getPath(\"/zenodot\", \"other/dir", fs);	// prints "directory with spaces"

			Path other = fs.getPath("/zenodot/other");
			printBestCompletion(parser, "this.resolve(\"fi", other);		// prints "file with spaces"
			printBestCompletion(parser, "this.resolveSibling(\"so", other);	// prints "some_file"

			printBestCompletion(parser, "new URI(\"jimfs://sample/zenodot/other/dir", null);						// prints "directory%20with%20spaces/"
			printBestCompletion(parser, "new URI(\"jimfs\", \"//sample/zenodot/other/fi", null);					// prints "file with spaces"
			printBestCompletion(parser, "new URI(\"jimfs\", \"sample\", \"/zenodot/an", null);						// prints "another_file"
			printBestCompletion(parser, "new URI(\"jimfs\", null, \"sample\", -1, \"/zenodot/samples/co", null);	// prints "completion_sample"
		}
	}

	private static void printBestCompletion(ExpressionParser parser, String text, Object thisValue) {
		try {
			List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), thisValue));
			completions.sort(Parsers.COMPLETION_COMPARATOR);
			System.out.print("completion for '" + text + "': ");
			if (completions.isEmpty()) {
				System.out.println("-");
			} else {
				CodeCompletion bestCompletion = completions.get(0);
				String textToInsert = bestCompletion.getTextToInsert();
				String textToDisplay = bestCompletion.toString();
				if (Objects.equals(textToInsert, textToDisplay)) {
					System.out.println(textToInsert);
				} else {
					System.out.println(textToInsert + " (displayed as \"" + textToDisplay + "\")");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static FileSystem setupFileSystem() throws IOException {
		FileSystem fs = Jimfs.newFileSystem("sample", Configuration.unix());
		try {
			FileSystemUtils.setupFileSystem(fs, ROOT);
		} catch (Throwable t) {
			fs.close();
			throw t;
		}
		return fs;
	}
}
