package dd.kms.zenodot.common;

import java.util.Arrays;
import java.util.function.Predicate;

class Filters
{
	static <T> Predicate<T> combine(Predicate<T>... filters) {
		Predicate<T> combinedFilter = null;
		for (Predicate<T> filter : Arrays.asList(filters)) {
			if (filter == null) {
				continue;
			}
			combinedFilter = combine(combinedFilter, filter);
		}
		return nullToFilter(combinedFilter);
	}

	static <T> Predicate<T> combine(Predicate<T> filter1, Predicate<T> filter2) {
		return	filter1 == null	? nullToFilter(filter2) :
				filter2 == null ? filter1
								: filter1.and(filter2);
	}

	static <T> Predicate<T> nullToFilter(Predicate<T> filter) {
		return filter == null ? o -> true : filter;
	}
}
