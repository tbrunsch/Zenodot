package dd.kms.zenodot.impl.directories;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import dd.kms.zenodot.api.directories.PathContainer;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class DefaultPathDirectoryStructure implements PathDirectoryStructure
{
	private static final Map<String, Object>	OPEN_ZIP_FILE_SYSTEM_PARAMETERS	= ImmutableMap.of("create", "true");

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
	public PathContainer toPath(URI uri) {
		Path _path;
		ExceptionalRunnable _onClose = null;
		try {
			_path = Paths.get(uri);
		} catch (FileSystemNotFoundException e) {
			String scheme = uri.getScheme();
			if ("jar".equalsIgnoreCase(scheme) || "zip".equalsIgnoreCase(scheme)) {
				// We have to open the ZipFileSystem manually and ensure that it will be closed later
				try {
					FileSystem fileSystem = FileSystems.newFileSystem(uri, OPEN_ZIP_FILE_SYSTEM_PARAMETERS);
					_onClose = fileSystem::close;
				} catch (IOException ioException) {
					throw e;
				}
				_path = Paths.get(uri);
			} else {
				throw e;
			}
		}
		Path path = _path;
		ExceptionalRunnable onClose = _onClose;
		return new PathContainer() {
			@Override
			public Path getPath() {
				return path;
			}

			@Override
			public boolean mustBeClosed() {
				return onClose != null;
			}

			@Override
			public void close() throws IOException {
				if (onClose != null) {
					onClose.run();
				}
			}
		};
	}
}
