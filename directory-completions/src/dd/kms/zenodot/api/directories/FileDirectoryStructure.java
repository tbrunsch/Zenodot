package dd.kms.zenodot.api.directories;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileDirectoryStructure
{
	FileDirectoryStructure DEFAULT	= new dd.kms.zenodot.impl.directories.DefaultFileDirectoryStructure();

	static FileDirectoryStructure cache(FileDirectoryStructure fileDirectoryStructure, CacheConfigurator cacheConfigurator) {
		return new dd.kms.zenodot.impl.directories.CachedFileDirectoryStructure(fileDirectoryStructure, cacheConfigurator);
	}

	File getFile(String path) throws IOException;
	File resolve(File parent, String childPath) throws IOException;
	List<File> getChildren(File parent) throws IOException;
	List<File> getRootDirectories() throws IOException;
}
