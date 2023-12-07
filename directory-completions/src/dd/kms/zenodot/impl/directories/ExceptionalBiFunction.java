package dd.kms.zenodot.impl.directories;

import java.io.IOException;

@FunctionalInterface
interface ExceptionalBiFunction<K1, K2, V> {
	V apply(K1 key1, K2 key2) throws IOException;
}
