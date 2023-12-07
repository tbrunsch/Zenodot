package dd.kms.zenodot.api.directories;

import dd.kms.zenodot.api.directories.common.OptionalCloseable;

import java.nio.file.Path;

public interface PathContainer extends OptionalCloseable
{
	Path getPath();
}
