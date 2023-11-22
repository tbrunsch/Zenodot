package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFileDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<File>
{
	protected final FileDirectoryStructure	fileDirectoryStructure;

	protected AbstractFileDirectoryCompletionProvider(FileDirectoryStructure fileDirectoryStructure) {
		this.fileDirectoryStructure = fileDirectoryStructure;
	}

	@Override
	protected List<? extends CodeCompletion> doGetCodeCompletions(@Nullable File parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try {
			List<File> children = parent != null ? fileDirectoryStructure.getChildren(parent) : fileDirectoryStructure.getRootDirectories();
			for (File child : children) {
				String childName = child.getName();
				CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode);
				codeCompletions.add(codeCompletion);
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far*/
		}
		return codeCompletions;
	}
}
