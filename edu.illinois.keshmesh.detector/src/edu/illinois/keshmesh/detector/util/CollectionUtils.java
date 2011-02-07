/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.classLoader.IField;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class CollectionUtils {

	public static String getTheOnlyElementOf(Set<String> set) {
		if (set.size() != 1) {
			throw new RuntimeException("Expected a set of one element"); //$NON-NLS-1$
		}
		return set.toArray(new String[1])[0];
	}

	public static Set<String> asSet(String... strs) {
		return new HashSet<String>(Arrays.asList(strs));
	}

	public static Object[] collectionToSortedArray(Collection<? extends Object> objects) {
		Object[] sortedArray = objects.toArray(new Object[] {});
		Arrays.sort(sortedArray, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		return sortedArray;
	}

}
