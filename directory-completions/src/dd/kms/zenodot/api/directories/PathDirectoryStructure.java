package dd.kms.zenodot.api.directories;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	/**
	 * Essentially returns a {@link Path} for a given {@link URI}. The reason why this method
	 * does not simply return a {@code Path} instance is that some file system implementations
	 * like {@code ZipFileSystem} have to be opened explicitly before {@link Paths#get(URI)} can
	 * succeed. These file systems usually have to be closed afterwards. It is the responsibility
	 * of the creator of the returned {@link PathContainer} to configure it in such a way that
	 * {@link PathContainer#close()} will close the underlying file system in such cases.
	 */
	PathContainer toPath(URI uri) throws IOException;
}
