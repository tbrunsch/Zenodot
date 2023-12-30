package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.directories.PathContainer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;

public class PathContainerImpl implements PathContainer
{
	private final Path	path;
	@Nullable
	private final ExceptionalRunnable	onClose;

	public PathContainerImpl(Path path, @Nullable ExceptionalRunnable onClose) {
		this.path = path;
		this.onClose = onClose;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public boolean mustBeClosed() {
		return onClose != null;
	}

	@Override
	public void close() throws IOException {
		if (onClose != null) {
			onClose.run();
		}
	}
}
