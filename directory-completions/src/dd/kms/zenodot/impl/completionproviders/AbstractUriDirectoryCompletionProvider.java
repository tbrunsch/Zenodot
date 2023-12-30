package dd.kms.zenodot.impl.completionproviders;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.directories.PathContainer;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.impl.common.JarUriHelper;

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

public abstract class AbstractUriDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<URI>
{
	protected final PathDirectoryStructure	pathDirectoryStructure;
	private final List<URI>					favoriteUris;
	private final Function<URI, String>		uriRepresentationFunction;
	private final boolean					useRawRepresentation;

	protected AbstractUriDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, List<URI> favoriteUris, Function<URI, String> uriRepresentationFunction, boolean useRawRepresentation) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		this.favoriteUris = ImmutableList.copyOf(favoriteUris);
		this.uriRepresentationFunction = uriRepresentationFunction;
		this.useRawRepresentation = useRawRepresentation;
	}

	@Override
	protected List<CodeCompletion> doGetCodeCompletions(NullableOptional<URI> optParentUri, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!optParentUri.isPresent() || optParentUri.get() == null) {
			return Collections.emptyList();
		}
		URI parentUri = optParentUri.get();
		String childNamePrefix = "";
		if (JarUriHelper.isApplicable(parentUri.getScheme())) {
			String oldParentUriAsString = parentUri.toString();
			parentUri = JarUriHelper.getCompletableUri(parentUri);
			String newParentUriAsString = parentUri.toString();
			if (oldParentUriAsString.endsWith(newParentUriAsString)) {
				childNamePrefix = oldParentUriAsString.substring(0, oldParentUriAsString.length() - newParentUriAsString.length());
			}
		}

		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try (PathContainer parentContainer = pathDirectoryStructure.toPath(parentUri)) {
			Path parent = parentContainer.getPath();
			List<Path> children = pathDirectoryStructure.getChildren(parent);
			for (Path child : children) {
				URI childUri = pathDirectoryStructure.toUri(child);
				handleUri(childUri, childNamePrefix, childCompletionInfo, completionMode, codeCompletions);
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
			for (URI favoriteUri : favoriteUris) {
				handleUri(favoriteUri, "", childCompletionInfo, completionMode, favoriteCompletions);
			}
		} catch (Throwable ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return favoriteCompletions;
	}

	private void handleUri(URI uri, String childNamePrefix, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, List<CodeCompletion> codeCompletions) throws UnsupportedEncodingException {
		String parentPath = childCompletionInfo.getParentPath();
		String childName = getChildName(uri, childNamePrefix, parentPath);
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
	private String getChildName(URI child, String childNamePrefix, String parentPath) {
		String childRepresentation = childNamePrefix + uriRepresentationFunction.apply(child);
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
