/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import edu.illinois.keshmesh.detector.util.CollectionUtils;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public abstract class IntermediateResults {

	protected boolean canSaveIntermediateResult(Object intermediateResult) {
		if (!Modes.isInProductionMode()) {
			if (intermediateResult != null) {
				throw new RuntimeException("Saved the same intermediate result more than once.");
			}
			return true;
		}
		return false;
	}

	protected String getIntermediateResult(String currentIntermediateResult, Collection<? extends Object> newIntermediateResult) {
		if (canSaveIntermediateResult(currentIntermediateResult)) {
			return Arrays.toString(CollectionUtils.collectionToSortedArray(newIntermediateResult));
		} else {
			return currentIntermediateResult;
		}
	}

	protected String getIntermediateResult(String currentIntermediateResult, Map<? extends Object, ? extends Collection<? extends Object>> newIntermediateResult) {
		if (canSaveIntermediateResult(currentIntermediateResult)) {
			TreeMap<Object, Object> orderedMap = new TreeMap<Object, Object>(new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			for (Map.Entry<? extends Object, ? extends Collection<? extends Object>> entry : newIntermediateResult.entrySet()) {
				orderedMap.put(entry.getKey(), Arrays.toString(CollectionUtils.collectionToSortedArray(entry.getValue())));
			}
			return orderedMap.toString();
		} else {
			return currentIntermediateResult;
		}
	}
}
