package dd.kms.zenodot.impl.directories;

import dd.kms.zenodot.api.directories.CacheConfigurator;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.impl.directories.CacheUtils.ExceptionalBiFunction;
import dd.kms.zenodot.impl.directories.CacheUtils.ExceptionalFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CachedFileDirectoryStructure implements FileDirectoryStructure
{
	private final ExceptionalFunction<String, File> 		fileCache;
	private final ExceptionalBiFunction<File, String, File>	resolveFileCache;
	private final ExceptionalFunction<Void, List<File>>		rootDirectoryCache;
	private final ExceptionalFunction<File, List<File>>		childCache;

	public CachedFileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure, CacheConfigurator cacheConfigurator) {
		this.fileCache = CacheUtils.cacheDelegate(fileDirectoryStructure::getFile, cacheConfigurator);
		this.resolveFileCache = CacheUtils.cacheDelegate(fileDirectoryStructure::resolve, cacheConfigurator);
		this.rootDirectoryCache = CacheUtils.cacheDelegate(nothing -> fileDirectoryStructure.getRootDirectories(), cacheConfigurator);
		this.childCache = CacheUtils.cacheDelegate(fileDirectoryStructure::getChildren, cacheConfigurator);
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
		return rootDirectoryCache.apply(null);
	}
}