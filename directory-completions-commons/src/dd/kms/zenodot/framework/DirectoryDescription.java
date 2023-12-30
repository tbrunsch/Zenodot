package dd.kms.zenodot.framework;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class DirectoryDescription extends DirectoryOrFileDescription
{
	private final List<DirectoryOrFileDescription> directoriesOrFiles;

	DirectoryDescription(String name, List<DirectoryOrFileDescription> directoriesOrFiles) {
		super(name);
		this.directoriesOrFiles = ImmutableList.copyOf(directoriesOrFiles);
	}

	public List<DirectoryOrFileDescription> getDirectoriesOrFiles() {
		return directoriesOrFiles;
	}
}
