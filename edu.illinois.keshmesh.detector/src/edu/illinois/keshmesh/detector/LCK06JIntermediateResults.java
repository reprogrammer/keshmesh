/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Arrays;
import java.util.Set;

import com.ibm.wala.classLoader.IField;

import edu.illinois.keshmesh.detector.util.CollectionUtils;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JIntermediateResults implements IntermediateResults {

	private String staticFields;

	public void setStaticFields(Set<IField> staticFields) {
		if (!Modes.isInProductionMode() && this.staticFields == null) {
			this.staticFields = Arrays.toString(CollectionUtils.collectionToSortedArray(staticFields));
		}
	}

	public String getStaticFields() {
		return staticFields;
	}

}
