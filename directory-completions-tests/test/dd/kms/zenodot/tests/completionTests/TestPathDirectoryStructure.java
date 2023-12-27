package dd.kms.zenodot.tests.completionTests;

import com.google.common.collect.Lists;
import dd.kms.zenodot.api.directories.PathContainer;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;

class TestPathDirectoryStructure implements PathDirectoryStructure
{
	private final FileSystem	replacementForDefaultFileSystem;

	TestPathDirectoryStructure(FileSystem replacementForDefaultFileSystem) {
		this.replacementForDefaultFileSystem = replacementForDefaultFileSystem;
	}

	@Override
	public FileSystem getDefaultFileSystem() {
		return replacementForDefaultFileSystem;
	}

	@Override
	public Path getFile(FileSystem fileSystem, String path) {
		return getTestFileSystem(fileSystem).getPath(path);
	}

	@Override
	public Path resolve(Path parent, String childPath) {
		return parent.resolve(childPath);
	}

	@Override
	public Path getParent(Path child) {
		return child.getParent();
	}

	@Override
	public List<Path> getChildren(Path parent) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
			return Lists.newArrayList(stream);
		}
	}

	@Override
	public List<Path> getRootDirectories(FileSystem fileSystem) {
		return Lists.newArrayList(getTestFileSystem(fileSystem).getRootDirectories());
	}

	@Override
	public URI toUri(Path path) {
		return path.toUri();
	}

	@Override
	public PathContainer toPath(URI uri) throws IOException {
		return PathDirectoryStructure.DEFAULT.toPath(uri);
	}

	private FileSystem getTestFileSystem(FileSystem fileSystem) {
		return Objects.equals(fileSystem, FileSystems.getDefault())
			? replacementForDefaultFileSystem
			: fileSystem;
	}
}
