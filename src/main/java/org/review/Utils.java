package org.review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Utils {

	/**
	 * Sorts map by value.
	 * 
	 * https://stackoverflow.com/a/2581754
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean reverse) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue(new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return reverse ? o2.compareTo(o1) : o1.compareTo(o2);
			}
		}));

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
