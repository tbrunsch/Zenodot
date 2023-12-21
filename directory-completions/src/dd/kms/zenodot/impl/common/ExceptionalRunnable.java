package dd.kms.zenodot.impl.common;

import java.io.IOException;

@FunctionalInterface
public interface ExceptionalRunnable
{
	void run() throws IOException;
}
