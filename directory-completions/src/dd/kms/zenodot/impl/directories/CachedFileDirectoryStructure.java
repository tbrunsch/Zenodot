package dd.kms.zenodot.impl.directories;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.impl.common.ExceptionalBiFunction;
import dd.kms.zenodot.impl.common.ExceptionalFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CachedFileDirectoryStructure implements FileDirectoryStructure
{
	private static final Object	NOTHING	= new Object();	// caches don't support null keys

	private final ExceptionalFunction<String, File>			fileCache;
	private final ExceptionalBiFunction<File, String, File> resolveFileCache;
	private final ExceptionalFunction<Object, List<File>>	rootDirectoryCache;	// Caches don't support null keys
	private final ExceptionalFunction<File, List<File>>		childCache;

	public CachedFileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure, long timeUntilEvictionMs) {
		this.fileCache = CacheUtils.cacheDelegate(fileDirectoryStructure::getFile, timeUntilEvictionMs);
		this.resolveFileCache = CacheUtils.cacheDelegate(fileDirectoryStructure::resolve, timeUntilEvictionMs);
		this.rootDirectoryCache = CacheUtils.cacheDelegate(nothing -> fileDirectoryStructure.getRootDirectories(), timeUntilEvictionMs);
		this.childCache = CacheUtils.cacheDelegate(fileDirectoryStructure::getChildren, timeUntilEvictionMs);
	}

	@Override
	public File getFile(String path) throws IOException {
		return fileCache.apply(path);
	}

	@Override
	public File resolve(File parent, String childPath) throws IOException {
		return resolveFileCache.apply(parent, childPath);
	}

	@Override
	public List<File> getChildren(File parent) throws IOException {
		return childCache.apply(parent);
	}

	@Override
	public List<File> getRootDirectories() throws IOException {
		return rootDirectoryCache.apply(NOTHING);
	}
}
