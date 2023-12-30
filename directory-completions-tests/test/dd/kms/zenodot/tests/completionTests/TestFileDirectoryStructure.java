package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class TestFileDirectoryStructure implements FileDirectoryStructure
{
	private final FileSystem replacementForDefaultFileSystem;

	TestFileDirectoryStructure(FileSystem replacementForDefaultFileSystem) {
		this.replacementForDefaultFileSystem = replacementForDefaultFileSystem;
	}

	@Override
	public File getFile(String path) {
		return toFile(replacementForDefaultFileSystem.getPath(path));
	}

	@Override
	public File resolve(File parent, String childPath) {
		return toFile(parent.toPath().resolve(childPath));
	}

	@Override
	public List<File> getChildren(File parent) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent.toPath())) {
			return StreamSupport.stream(stream.spliterator(), false)
				.map(TestFileDirectoryStructure::toFile)
				.collect(Collectors.toList());
		}
	}

	@Override
	public List<File> getRootDirectories() {
		return StreamSupport.stream(replacementForDefaultFileSystem.getRootDirectories().spliterator(), false)
			.map(TestFileDirectoryStructure::toFile)
			.collect(Collectors.toList());
	}

	static File toFile(Path path) {
		return new PathToFileAdapter(path);
	}

	private static class PathToFileAdapter extends File
	{
		private final Path	path;

		PathToFileAdapter(Path path) {
			super("");
			this.path = path;
		}

		@Override
		public String getName() {
			return path.getFileName().toString();
		}

		@Override
		public Path toPath() {
			return path;
		}

		@Override
		public String toString() {
			return path.toString();
		}
	}
}
