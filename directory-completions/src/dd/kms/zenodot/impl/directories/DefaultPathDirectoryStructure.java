package dd.kms.zenodot.impl.directories;

import com.google.common.collect.Lists;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;

public class DefaultPathDirectoryStructure implements PathDirectoryStructure
{
	@Override
	public FileSystem getDefaultFileSystem() {
		return FileSystems.getDefault();
	}

	@Override
	public Path getFile(FileSystem fileSystem, String path) {
		return fileSystem.getPath(path);
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
		return Lists.newArrayList(fileSystem.getRootDirectories());
	}

	@Override
	public URI toURI(Path path) {
		return path.toUri();
	}

	@Override
	public Path toPath(URI uri) {
		return Paths.get(uri);
	}
}
