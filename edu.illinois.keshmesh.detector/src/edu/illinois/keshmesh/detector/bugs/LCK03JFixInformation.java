/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.LinkedHashSet;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Samira Tasharofi
 */
public class LCK03JFixInformation implements FixInformation {

	/* FIXME: Remove instanceTypes */
	Set<IClass> instanceTypes;

	Set<String> instanceTypesString;
	boolean isLock;

	/* FIXME: Remove instanceTypes from the arguments list */
	public LCK03JFixInformation(Set<IClass> instanceTypes, boolean isLock) {
		super();
		this.instanceTypes = instanceTypes;
		this.isLock = isLock;
		this.instanceTypesString = new LinkedHashSet<String>();
		for (IClass type : instanceTypes) {
			instanceTypesString.add(AnalysisUtils.walaTypeNameToJavaName(type.getName()));
		}
	}

	public LCK03JFixInformation(Set<String> instanceTypesString) {
		super();
		this.instanceTypesString = instanceTypesString;
	}

	public Set<IClass> getInstanceTypes() {
		return instanceTypes;
	}

	public boolean isLock() {
		return isLock;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceTypesString == null) ? 0 : instanceTypesString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LCK03JFixInformation other = (LCK03JFixInformation) obj;
		if (instanceTypesString == null) {
			if (other.instanceTypesString != null)
				return false;
		} else if (!instanceTypesString.equals(other.instanceTypesString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LCK03JFixInformation [typeNames=" + instanceTypesString + "]";
	}

}
