package dd.kms.zenodot.impl.directories;

import com.google.common.collect.Lists;
import dd.kms.zenodot.api.directories.PathContainer;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.impl.common.JarUriHelper;
import dd.kms.zenodot.impl.common.PathContainerImpl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;

public class DefaultPathDirectoryStructure implements PathDirectoryStructure
{
	private final JarUriHelper	jarUriHelper	= new JarUriHelper();

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
	public URI toUri(Path path) {
		URI uri = path.toUri();
		String scheme = uri.getScheme();
		if (!JarUriHelper.isApplicable(scheme)) {
			return uri;
		}
		return jarUriHelper.correctJarUri(uri);
	}

	@Override
	public PathContainer toPath(URI uri) {
		String scheme = uri.getScheme();
		if (JarUriHelper.isApplicable(scheme)) {
			return jarUriHelper.toPath(uri);
		} else {
			Path path = Paths.get(uri);
			return new PathContainerImpl(path, null);
		}
	}
}
