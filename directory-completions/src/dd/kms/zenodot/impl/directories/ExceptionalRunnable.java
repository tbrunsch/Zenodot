package dd.kms.zenodot.impl.directories;

import java.io.IOException;

@FunctionalInterface
public interface ExceptionalRunnable
{
	void run() throws IOException;
}
