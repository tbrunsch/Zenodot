package dd.kms.zenodot.impl.directories;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultFileDirectoryStructure implements FileDirectoryStructure
{
	@Override
	public File getFile(String path) {
		return new File(path);
	}

	@Override
	public File resolve(File parent, String childPath) {
		return new File(parent, childPath);
	}

	@Override
	public List<File> getRootDirectories() {
		return Arrays.asList(File.listRoots());
	}

	@Override
	public List<File> getChildren(File parent) {
		File[] childArray = parent.listFiles();
		return childArray != null ? Arrays.asList(childArray) : Collections.emptyList();
	}
}
