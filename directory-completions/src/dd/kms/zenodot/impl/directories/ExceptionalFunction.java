package dd.kms.zenodot.impl.directories;

import java.io.IOException;

@FunctionalInterface
interface ExceptionalFunction<K, V> {
	V apply(K key) throws IOException;
}
