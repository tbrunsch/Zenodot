package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractURIDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<URI>
{
	protected final PathDirectoryStructure	pathDirectoryStructure;

	protected AbstractURIDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure) {
		this.pathDirectoryStructure = pathDirectoryStructure;
	}

	protected abstract String doExtractChildRepresentation(URI childURI);

	@Override
	protected List<? extends CodeCompletion> doGetCodeCompletions(@Nullable URI parentURI, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (parentURI == null) {
			return Collections.emptyList();
		}
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try {
			Path parent = pathDirectoryStructure.toPath(parentURI);
			List<Path> children = pathDirectoryStructure.getChildren(parent);
			String parentPath = childCompletionInfo.getParentPath();
			try {
				for (Path child : children) {
					URI childURI = pathDirectoryStructure.toURI(child);
					String childRepresentation = doExtractChildRepresentation(childURI);
					if (childRepresentation != null && childRepresentation.startsWith(parentPath)) {
						String childName = childRepresentation.substring(parentPath.length());
						if (childName.endsWith("/")) {
							childName = childName.substring(0, childName.length() - 1);
						}
						String childNameToDisplay = URLDecoder.decode(childName, StandardCharsets.UTF_8.toString());
						CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode, childNameToDisplay);
						codeCompletions.add(codeCompletion);
					}
				}
			} finally {
				if (children instanceof Closeable) {
					((Closeable) children).close();
				}
			}
		} catch (Throwable ignored) {
			/* fallthrough to return the code completions gathered so far*/
		}
		return codeCompletions;
	}
}
