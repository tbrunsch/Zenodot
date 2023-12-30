package dd.kms.zenodot.impl.common;

import java.io.IOException;

@FunctionalInterface
public interface ExceptionalFunction<K, V> {
	V apply(K key) throws IOException;
}
