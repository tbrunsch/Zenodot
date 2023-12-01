package dd.kms.zenodot.api.directories;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public interface PathDirectoryStructure
{
	PathDirectoryStructure DEFAULT	= new dd.kms.zenodot.impl.directories.DefaultPathDirectoryStructure();

	static PathDirectoryStructure cache(PathDirectoryStructure pathDirectoryStructure, long timeUntilEvictionMs) {
		return new dd.kms.zenodot.impl.directories.CachedPathDirectoryStructure(pathDirectoryStructure, timeUntilEvictionMs);
	}

	FileSystem getDefaultFileSystem();
	Path getFile(FileSystem fileSystem, String path) throws IOException;
	Path resolve(Path parent, String childPath) throws IOException;
	Path getParent(Path child) throws IOException;
	List<Path> getChildren(Path parent) throws IOException;
	List<Path> getRootDirectories(FileSystem fileSystem) throws IOException;

	URI toURI(Path path) throws IOException;
	Path toPath(URI uri) throws IOException;
}
