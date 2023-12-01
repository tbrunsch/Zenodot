package dd.kms.zenodot.impl.directories;

import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.impl.directories.CacheUtils.ExceptionalBiFunction;
import dd.kms.zenodot.impl.directories.CacheUtils.ExceptionalFunction;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public class CachedPathDirectoryStructure implements PathDirectoryStructure
{
	private final PathDirectoryStructure							pathDirectoryStructure;
	private final ExceptionalBiFunction<FileSystem, String, Path>	fileCache;
	private final ExceptionalBiFunction<Path, String, Path>			resolveCache;
	private final ExceptionalFunction<Path, Path>					parentCache;
	private final ExceptionalFunction<Path, List<Path>>				childCache;
	private final ExceptionalFunction<FileSystem, List<Path>>		rootDirectoryCache;
	private final ExceptionalFunction<URI, Path>					uriToPathCache;
	private final ExceptionalFunction<Path, URI>					pathToUriCache;

	public CachedPathDirectoryStructure(PathDirectoryStructure pathDirectoryStructure, long timeUntilEvictionMs) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		this.fileCache = CacheUtils.cacheDelegate(pathDirectoryStructure::getFile, timeUntilEvictionMs);
		this.resolveCache = CacheUtils.cacheDelegate(pathDirectoryStructure::resolve, timeUntilEvictionMs);
		this.parentCache = CacheUtils.cacheDelegate(pathDirectoryStructure::getParent, timeUntilEvictionMs);
		this.childCache = CacheUtils.cacheDelegate(pathDirectoryStructure::getChildren, timeUntilEvictionMs);
		this.rootDirectoryCache = CacheUtils.cacheDelegate(pathDirectoryStructure::getRootDirectories, timeUntilEvictionMs);
		this.uriToPathCache = CacheUtils.cacheDelegate(pathDirectoryStructure::toPath, timeUntilEvictionMs);
		this.pathToUriCache = CacheUtils.cacheDelegate(pathDirectoryStructure::toURI, timeUntilEvictionMs);
	}

	@Override
	public FileSystem getDefaultFileSystem() {
		return pathDirectoryStructure.getDefaultFileSystem();
	}

	@Override
	public Path getFile(FileSystem fileSystem, String path) throws IOException {
		return fileCache.apply(fileSystem, path);
	}

	@Override
	public Path resolve(Path parent, String childPath) throws IOException {
		return resolveCache.apply(parent, childPath);
	}

	@Override
	public Path getParent(Path child) throws IOException {
		return parentCache.apply(child);
	}

	@Override
	public List<Path> getChildren(Path parent) throws IOException {
		return childCache.apply(parent);
	}

	@Override
	public List<Path> getRootDirectories(FileSystem fileSystem) throws IOException {
		return rootDirectoryCache.apply(fileSystem);
	}

	@Override
	public URI toURI(Path path) throws IOException {
		return pathToUriCache.apply(path);
	}

	@Override
	public Path toPath(URI uri) throws IOException {
		return uriToPathCache.apply(uri);
	}
}
