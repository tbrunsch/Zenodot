package dd.kms.zenodot.api.directories.common;

import java.io.Closeable;

public interface OptionalCloseable extends Closeable
{
	boolean mustBeClosed();
}
