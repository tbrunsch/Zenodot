package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AbsoluteFileDirectoryCompletionProvider extends AbstractFileDirectoryCompletionProvider
{
	public AbsoluteFileDirectoryCompletionProvider(FileDirectoryStructure fileDirectoryStructure, List<String> favoritePaths) {
		super(fileDirectoryStructure, favoritePaths);
	}

	@Nullable
	@Override
	protected File doGetParent(@Nullable String parentPath, CallerContext callerContext) throws IOException {
		return parentPath != null ? fileDirectoryStructure.getFile(parentPath) : null;
	}
}
