package dd.kms.zenodot.impl.common;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.directories.PathContainer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains methods for handling jar URIs, i.e., URIs of the form
 * {@code "jar:file://sample.zip!/zipped.txt"}.
 */
public class JarUriHelper
{
	private static final Map<String, Object>	OPEN_ZIP_FILE_SYSTEM_PARAMETERS	= ImmutableMap.of("create", "true");
	private static final String					JAR_SEPARATOR					= "!/";
	private static final Splitter				JAR_URI_SPLITTER				= Splitter.on(JAR_SEPARATOR);

	public static boolean isApplicable(String scheme) {
		return "jar".equalsIgnoreCase(scheme) || "zip".equalsIgnoreCase(scheme);
	}

	/**
	 * A full jar URI consists of a wrapped URI that references the zip file and a part that
	 * references a file within the zip file: {@code "jar:file://sample.zip!/zipped.txt"}.
	 * When typing this URI, the second part is missing for some time. Until then, the
	 * framework shall complete the reference to the zip file instead of the file within the
	 * zip file.<br>
	 * <br>
	 * <b>Examples:</b>
	 * <ul>
	 *     <li>
	 *         Let us assume that the current incomplete jar URI is {@code "jar:file://sampl"}.
	 *         In that case, the URI that the framework should actually complete is
	 *         {@code "file://sampl"}.
	 *     </li>
	 *     <li>
	 *         Let us assume that the current incomplete jar URI is {@code "jar:file://sample.zip!/zipped.txt"}.
	 *         In that case, this is also the URI that the framework should complete.
	 *     </li>
	 * </ul>
	 * Given a possibly incomplete URI, this method returns the one the framework should
	 * complete.
	 */
	public static URI getCompletableUri(URI uri) {
		if (!isApplicable(uri.getScheme())) {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		String rawSchemeSpecificPart = uri.getRawSchemeSpecificPart();
		int lastJarSeparatorIndex = rawSchemeSpecificPart.lastIndexOf(JAR_SEPARATOR);
		if (lastJarSeparatorIndex >= 0) {
			return uri;
		}
		try {
			return new URI(rawSchemeSpecificPart);
		} catch (Exception e) {
			// no way to handle this
			return uri;
		}
	}

	/**
	 * The call {@link Path#toUri()} does not return a correct {@link URI} for {@link Path}
	 * instances that reference an entry in a zip file in all Java versions (see
	 * <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8131067">this bug report</a>).
	 * The method encodes parts of the URI twice in the affected versions. This method takes
	 * the result of {@code Path.toUri()} as argument and checks whether it is valid. If not,
	 * then it tries to construct a valid URI by decoding the affected part of the URI.
	 */
	public URI correctJarUri(URI uri) {
		String scheme = uri.getScheme();
		if (!isApplicable(scheme)) {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		String uriAsString = uri.toString();
		if (!uriAsString.contains("%")) {
			// URI does not contain escaped characters => no problem
			return uri;
		}

		if (workaroundState == WorkaroundState.UNDEFINED) {
			synchronized (this) {
				if (workaroundState == WorkaroundState.UNDEFINED) {
					boolean correct = checkUriToPathToUri(uri, uri);
					if (correct) {
						// no workaround seems to be necessary (probably a newer Java version)
						workaroundState = WorkaroundState.NOT_REQUIRED;
						return uri;
					}
					URI correctedUri = correctUri(uri);
					if (Objects.equals(correctedUri, uri)) {
						// correction did not work
						return uri;
					}
					correct = checkUriToPathToUri(correctedUri, uri);
					if (correct) {
						// workaround seems to work
						workaroundState = WorkaroundState.REQUIRED;
						return correctedUri;
					}
					// both, original and corrected URI, do not pass the URI -> Path -> URI test => prefer original URI
					return uri;
				}
			}
		}
		switch (workaroundState) {
			case NOT_REQUIRED:
				return uri;
			case REQUIRED: {
				return correctUri(uri);
			}
			default:
				throw new IllegalStateException("Unexpected workaround state: " + workaroundState);
		}
	}

	/**
	 * The call {@link Paths#get(URI)} does only succeed for a jar URI if the {@link FileSystem}
	 * that represents the zip file has already been opened. Since the caller should not be
	 * bothered with opening that file system, this method opens the file system if it is not
	 * already open. In that case, the caller is responsible for closing the file system. To
	 * make this as simple as possible for the caller, the method returns a {@link PathContainer},
	 * which extends {@link java.io.Closeable}. The method {@link PathContainer#close()} will
	 * only close the file system if this method has opened it. Otherwise, {@code close()}
	 * won't do anything with the file system.
	 */
	public PathContainer toPath(URI uri) {
		if (!isApplicable(uri.getScheme())) {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		try {
			Path path = Paths.get(uri);
			return new PathContainerImpl(path, null);
		} catch (FileSystemNotFoundException e) {
			if (!archiveUriExists(uri)) {
				throw e;
			}
			// We have to open the ZipFileSystem manually and ensure that it will be closed later
			FileSystem fileSystem;
			try {
				fileSystem = FileSystems.newFileSystem(uri, OPEN_ZIP_FILE_SYSTEM_PARAMETERS);
			} catch (IOException ignored) {
				throw e;
			}
			try {
				Path path = Paths.get(uri);
				return new PathContainerImpl(path, fileSystem::close);
			} catch (Exception internalException) {
				try {
					fileSystem.close();
				} catch (Exception ignored) {}
			}
			throw e;
		}
	}

	private boolean archiveUriExists(URI uri) {
		URI archiveUri = getArchiveUri(uri);
		if (archiveUri == null) {
			return false;
		}
		try {
			Path path = Paths.get(archiveUri);
			return Files.exists(path);
		} catch (Exception e) {
			return false;
		}
	}

	@Nullable
	private URI getArchiveUri(URI uri) {
		String rawSchemeSpecificPart = uri.getRawSchemeSpecificPart();
		List<String> rawSubUriParts = JAR_URI_SPLITTER.splitToList(rawSchemeSpecificPart);
		if (rawSubUriParts.size() != 2) {
			// We don't know how to handle this.
			return null;
		}
		try {
			return new URI(rawSubUriParts.get(0));
		} catch (Exception e) {
			return null;
		}
	}

	private URI correctUri(URI uri) {
		String schemeSpecificPart = uri.getSchemeSpecificPart();
		List<String> subUriParts = JAR_URI_SPLITTER.splitToList(schemeSpecificPart);
		if (subUriParts.size() != 2) {
			// We don't know how to handle this.
			return uri;
		}
		String rawSchemeSpecificPart = uri.getRawSchemeSpecificPart();
		List<String> rawSubUriParts = JAR_URI_SPLITTER.splitToList(rawSchemeSpecificPart);
		if (rawSubUriParts.size() != 2) {
			// We don't know how to handle this.
			return uri;
		}
		String correctedUriString = uri.getScheme() + ":"
			+ subUriParts.get(0)	// decoded once because encoded twice due to a bug in some Java versions
			+ JAR_SEPARATOR
			+ rawSubUriParts.get(1);
		try {
			URI correctedUri = new URI(correctedUriString);
			return correctedUri;
		} catch (Exception e) {
			// corrected URI had an invalid syntax
			return uri;
		}
	}

	/**
	 * Essentially checks whether {@code Paths.get(uri).toURI()} yields
	 * {@code expectedUri}.
	 */
	private boolean checkUriToPathToUri(URI uri, URI expectedUri) {
		try (PathContainer pathContainer = toPath(uri)) {
			URI resultUri = pathContainer.getPath().toUri();
			return Objects.equals(resultUri, expectedUri);
		} catch (Exception e) {
			return false;
		}
	}

	private WorkaroundState workaroundState = WorkaroundState.UNDEFINED;

	private enum WorkaroundState
	{
		UNDEFINED,
		NOT_REQUIRED,
		REQUIRED
	}
}
