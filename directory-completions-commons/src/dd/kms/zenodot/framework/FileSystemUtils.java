package dd.kms.zenodot.framework;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemUtils
{
	public static DirectoryDescription dir(String name, DirectoryOrFileDescription... directoriesAndFiles) {
		return new DirectoryDescription(name, ImmutableList.copyOf(directoriesAndFiles));
	}

	public static FileDescription file(String name) {
		return new FileDescription(name);
	}

	public static void setupFileSystem(FileSystem fileSystem, DirectoryDescription rootDirectory) throws IOException {
		try {
			Path root = fileSystem.getRootDirectories().iterator().next();
			addDirectoryContent(rootDirectory, root);
		} catch (Throwable t) {
			fileSystem.close();
			throw t;
		}
	}

	private static void addDirectoryContent(DirectoryDescription dir, Path dirPath) throws IOException {
		for (DirectoryOrFileDescription directoryOrFile : dir.getDirectoriesOrFiles()) {
			if (directoryOrFile instanceof DirectoryDescription) {
				DirectoryDescription subDir = (DirectoryDescription) directoryOrFile;
				Path subDirPath = dirPath.resolve(subDir.getName());
				Files.createDirectory(subDirPath);
				addDirectoryContent(subDir, subDirPath);
			} else {
				Path filePath = dirPath.resolve(directoryOrFile.getName());
				Files.createFile(filePath);
			}
		}
	}
}
