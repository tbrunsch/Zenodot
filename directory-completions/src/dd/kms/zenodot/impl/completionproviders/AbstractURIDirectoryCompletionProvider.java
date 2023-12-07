package dd.kms.zenodot.impl.completionproviders;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.directories.PathContainer;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractURIDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<URI>
{
	protected final PathDirectoryStructure	pathDirectoryStructure;
	private final List<URI>					favoriteURIs;
	private final Function<URI, String>		uriRepresentationFunction;
	private final boolean					useRawRepresentation;

	protected AbstractURIDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, List<URI> favoriteURIs, Function<URI, String> uriRepresentationFunction, boolean useRawRepresentation) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		this.favoriteURIs = ImmutableList.copyOf(favoriteURIs);
		this.uriRepresentationFunction = uriRepresentationFunction;
		this.useRawRepresentation = useRawRepresentation;
	}

	@Override
	protected List<CodeCompletion> doGetCodeCompletions(NullableOptional<URI> parentURI, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!parentURI.isPresent() || parentURI.get() == null) {
			return Collections.emptyList();
		}
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try (PathContainer parentContainer = pathDirectoryStructure.toPath(parentURI.get())) {
			Path parent = parentContainer.getPath();
			List<Path> children = pathDirectoryStructure.getChildren(parent);
			for (Path child : children) {
				URI childURI = pathDirectoryStructure.toURI(child);
				handleURI(childURI, childCompletionInfo, completionMode, codeCompletions);
			}
		} catch (Throwable ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return codeCompletions;
	}

	@Override
	protected List<CodeCompletion> doGetFavoriteCompletions(NullableOptional<URI> parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		List<CodeCompletion> favoriteCompletions = new ArrayList<>();
		try {
			for (URI favoriteURI : favoriteURIs) {
				handleURI(favoriteURI, childCompletionInfo, completionMode, favoriteCompletions);
			}
		} catch (Throwable ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return favoriteCompletions;
	}

	private void handleURI(URI uri, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, List<CodeCompletion> codeCompletions) throws UnsupportedEncodingException {
		String parentPath = childCompletionInfo.getParentPath();
		String childName = getChildName(uri, parentPath);
		if (childName == null) {
			return;
		}
		if (childName.endsWith("/")) {
			childName = childName.substring(0, childName.length() - 1);
		}
		String childNameToDisplay = useRawRepresentation
			? URLDecoder.decode(childName, StandardCharsets.UTF_8.toString())
			: childName;
		CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode, childNameToDisplay);
		codeCompletions.add(codeCompletion);
	}

	@Nullable
	private String getChildName(URI child, String parentPath) {
		String childRepresentation = uriRepresentationFunction.apply(child);
		if (childRepresentation == null) {
			return null;
		}
		if (parentPath == null) {
			return childRepresentation;
		}
		return childRepresentation.startsWith(parentPath) && !Objects.equals(childRepresentation, parentPath)
			? childRepresentation.substring(parentPath.length())
			: null;
	}
}
