/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Arrays;
import java.util.Collection;

import com.ibm.wala.classLoader.IClass;

import edu.illinois.keshmesh.detector.util.CollectionUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JIntermediateResults extends IntermediateResults {

	private String threadSafeClasses;

	public void setThreadSafeClasses(Collection<IClass> threadSafeClasses) {
		if (canSaveIntermediateResult(this.threadSafeClasses)) {
			this.threadSafeClasses = Arrays.toString(CollectionUtils.collectionToSortedArray(threadSafeClasses));
		}
	}

	public String getThreadSafeClasses() {
		return threadSafeClasses;
	}

}
