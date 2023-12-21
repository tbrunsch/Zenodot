package dd.kms.zenodot.impl.common;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.directories.PathContainer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;

/**
 * This class encapsulates two workarounds that are required for transforming jar {@link URI}s, i.e., URIs like
 * {@code "jar:file://sample.zip!/zipped.txt"}, to {@link Path}s and back:
 * <ul>
 *     <li>
 *         <b>{@code URI} to {@code Path}:</b> The call {@link Paths#get(URI)} does only succeed for a jar URI
 *         {@code uri} if the {@link FileSystem} that represents the zip file has already been opened. Since the caller
 *         should not be bothered with opening that file system, the method {@link #toPath(URI)} opens the file system
 *         if it is not already open. In that case, the caller is responsible for closing the file system. To make this
 *         as simple as possible for the caller, the method returns a {@link PathContainer}, which extends
 *         {@link java.io.Closeable}. The method {@link PathContainer#close()} will only close the file system if
 *         {@code toPath()} has opened it. Otherwise, {@code close()} won't do anything with the file system.
 *     </li>
 *     <li>
 *         <b>{@code Path} to {@code URI}:</b> The call {@link Path#toUri()} does not return a correct {@code URI}
 *         for {@code Path} instances that reference an entry in a zip file in all Java versions (see
 *         <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8131067">this bug report</a>). The method
 *         encodes parts of the URI twice in the affected version. The method {@link #correctJarUri(URI)} checks whether
 *         the result of {@code Path.toUri()} returns a valid result. If not, then it tries to construct a valid URI
 *         be decode the affected part of the URI.
 *     </li>
 * </ul>
 */
public class JarUriWorkaround
{
	private static final Map<String, Object> OPEN_ZIP_FILE_SYSTEM_PARAMETERS	= ImmutableMap.of("create", "true");

	public boolean isApplicable(String scheme) {
		return "jar".equalsIgnoreCase(scheme) || "zip".equalsIgnoreCase(scheme);
	}

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

	public PathContainer toPath(URI uri) {
		if (!isApplicable(uri.getScheme())) {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		try {
			Path path = Paths.get(uri);
			return new PathContainerImpl(path, null);
		} catch (FileSystemNotFoundException e) {
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

	private URI correctUri(URI uri) {
		String schemeSpecificPart = uri.getSchemeSpecificPart();
		String[] subUriParts = schemeSpecificPart.split("!/");
		if (subUriParts.length != 2) {
			// We don't know how to handle this.
			return uri;
		}
		String rawSchemeSpecificPart = uri.getRawSchemeSpecificPart();
		String[] rawSubUriParts = rawSchemeSpecificPart.split("!/");
		if (rawSubUriParts.length != 2) {
			// We don't know how to handle this.
			return uri;
		}
		String correctedUriString = uri.getScheme() + ":"
			+ subUriParts[0]	// decoded once because encoded twice due to a bug in some Java versions
			+ "!/"
			+ rawSubUriParts[1];
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
		boolean success = false;
		try (PathContainer pathContainer = toPath(uri)) {
			URI resultUri = pathContainer.getPath().toUri();
			success = Objects.equals(resultUri, expectedUri);
		} catch (Exception ignored) {}
		return success;
	}

	private WorkaroundState workaroundState = WorkaroundState.UNDEFINED;

	private enum WorkaroundState
	{
		UNDEFINED,
		NOT_REQUIRED,
		REQUIRED
	}
}
