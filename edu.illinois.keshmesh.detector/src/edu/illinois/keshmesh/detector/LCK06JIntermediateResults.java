/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;

import com.ibm.wala.classLoader.IField;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JIntermediateResults extends IntermediateResults {

	private String staticFields;

	public void setStaticFields(Collection<IField> staticFields) {
		this.staticFields = getIntermediateResult(this.staticFields, staticFields);
	}

	public String getStaticFields() {
		return staticFields;
	}

}
